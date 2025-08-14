package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.AssessmentResponse;
import ca.canada.digital.search.assessment.api.CreateAssessmentRequest;
import ca.canada.digital.search.assessment.api.UrlAssessmentResponse;
import ca.canada.digital.search.assessment.config.AssessmentConfiguration;
import ca.canada.digital.search.assessment.model.*;
import ca.canada.digital.search.assessment.object.Format;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.process.CsvProcess;
import ca.canada.digital.search.assessment.process.TermEvaluationProcess;
import ca.canada.digital.search.assessment.service.AssessmentService;
import ca.canada.digital.search.assessment.service.TermAssessmentService;
import ca.canada.digital.search.assessment.service.TermListService;
import ca.canada.digital.search.assessment.util.ResponseUtil;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Path("/assessments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssessmentResource {

    private static final Logger LOG = LoggerFactory.getLogger(AssessmentResource.class);
    private final AssessmentService assessmentService;
    private final TermListService termListService;
    private final TermAssessmentService termAssessmentService;
    private AssessmentConfiguration config;
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    public AssessmentResource(AssessmentService assessmentService, TermListService termListService, TermAssessmentService termAssessmentService, AssessmentConfiguration config) {
        this.assessmentService = assessmentService;
        this.termListService = termListService;
        this.termAssessmentService = termAssessmentService;
        this.config = config;
    }

    /**
     * PUBLIC endpoint – no authentication required.
     */
    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response getAssessment(@PathParam("id") Integer id, @QueryParam("format") @DefaultValue("json") String format) {
        Format fileFormat = Format.CSV.getFormat().equalsIgnoreCase(format) ? Format.CSV : Format.JSON;

        Map<String, List<Map<String, Object>>> allTerms = new HashMap<>();

        if (fileFormat == Format.CSV) {
            Assessment assessment = assessmentService.get(id);

            StreamingOutput entity = out -> {
                CsvProcess csvProcess = new CsvProcess(out, assessment);
                csvProcess.execute();
            };

            return Response.ok(entity)
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s-%s.csv\"",
                            assessment.getTermList().getDepartment().getAcronymEn(), assessment.getName()))
                    .build();

        } else {
            AssessmentResponse assessmentResponse = assessmentService.getAssessmentResponse(id);

            return Response.ok(assessmentResponse).build();
        }
    }

    /**
     * Create – requester must be a member of the list's department.
     */
    @POST
    @UnitOfWork
    public Response createAssessment(@Auth UserEntity requester,
                                     @Valid CreateAssessmentRequest req) {

        try {
        TermList list = termListService.getTermList(req.getListId());

        threadPool.submit(new Runnable() {

            @Override
            public void run() {
                // Check in case this analysis was already requested and completed
                LOG.info("Running a new assessment for list {} named {} by {}...", list.getName(), req.getName(), requester.getEmail());

                WebDriver driver = SeleniumDriver.INSTANCE.getDriver();
                List<Term> searchTerms = termListService.listTerms(requester, req.getListId());

                TermEvaluationProcess evaluationProcess = new TermEvaluationProcess(searchTerms, list.getDepartment(), list.getLanguage(), driver);

                List<TermAssessment> termAssessments = evaluationProcess.execute();

                LOG.info("Persisting the assessment for list {} named {}...", list.getName(), req.getName());
                Assessment a = assessmentService.create(
                        requester,
                        req,
                        termAssessments
                );

                if (a == null) {
                    LOG.error("Persisting the new assessment for list {} named {} has failed.",
                            list.getName(), req.getName());
                } else {
                    LOG.info("Persisting the new assessment for list {} named {} was successful.", list.getName(), req.getName());
                }
                LOG.info("Closing the Selenium driver.");
                driver.close();

            }
        });

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("message", "The assessment request was added to the queue.");
            resultMap.put("queued", threadPool.getPoolSize());
            return ResponseUtil.successResponse(resultMap);

        } catch (Exception e) {
            LOG.error("Could not complete the REST request.", e);
        }
        return ResponseUtil.errorResponse("Something went wrong while processing the data.");
    }

    /**
     * Delete – requester must be an admin or the owner of the list.
     */
    @DELETE
    @UnitOfWork
    public Response deleteAssessment(@Auth UserEntity requester,
                                     @Valid CreateAssessmentRequest req) {

        Assessment a = assessmentService.create(
                requester,
                req,
                null
        );

        return Response.created(
                UriBuilder.fromResource(AssessmentResource.class)
                        .path(a.getId().toString())
                        .build()
        ).entity(a).build();
    }

    @GET
    @Path("/url")
    @UnitOfWork
    public Response getAssessment(@QueryParam("url") String url, @QueryParam("lang") @DefaultValue("en") String lang) {
        Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;
        UrlAssessmentResponse urlAssessmentResponse = termAssessmentService.getUrlAssessmentResponse(url, language);
        return Response.ok(urlAssessmentResponse).build();
    }

        private enum SeleniumDriver {
        INSTANCE;

        SeleniumDriver() {
        }

        public WebDriver getDriver() {
            return getSeleniumDriver();
        }

        private FirefoxDriver getSeleniumDriver() {
            System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");
            FirefoxOptions options = new FirefoxOptions();
            options.addPreference("general.useragent.override", TermEvaluationProcess.USER_AGENT);
            options.addArguments("-headless");
            options.addArguments("--start-maximized");
            options.addArguments("--single-process");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-extensions");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-application-cache");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-dev-tools");
            options.addArguments("--no-zygote");

            return new FirefoxDriver(options);
        }
    }

}