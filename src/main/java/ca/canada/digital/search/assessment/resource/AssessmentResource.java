package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.AssessmentResponse;
import ca.canada.digital.search.assessment.api.CreateAssessmentRequest;
import ca.canada.digital.search.assessment.api.UrlAssessmentResponse;
import ca.canada.digital.search.assessment.model.Assessment;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.object.Format;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.process.CsvProcess;
import ca.canada.digital.search.assessment.service.*;
import ca.canada.digital.search.assessment.util.ResponseUtil;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Path("/assessments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssessmentResource {

    private static final Logger LOG = LoggerFactory.getLogger(AssessmentResource.class);
    private final AssessmentService assessmentService;
    private final TermListService termListService;
    private final TermAssessmentService termAssessmentService;
    private final UserService userService;
    private final ExecutorService threadPool;
    private final UnitOfWorkAwareProxyFactory uowFactory;

    public AssessmentResource(ExecutorService threadPool,
                              HibernateBundle<?> hibernateBundle,
                              AssessmentService assessmentService,
                              TermListService termListService,
                              TermAssessmentService termAssessmentService,
                              UserService userService) {
        this.threadPool = threadPool;
        this.uowFactory = new UnitOfWorkAwareProxyFactory(hibernateBundle);
        this.assessmentService = assessmentService;
        this.termListService = termListService;
        this.termAssessmentService = termAssessmentService;
        this.userService = userService;
    }

    /**
     * PUBLIC endpoint – no authentication required.
     */
    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response getAssessment(@PathParam("id") Integer id, @QueryParam("format") @DefaultValue("json") String format) {
        Format fileFormat = Format.CSV.getFormat().equalsIgnoreCase(format) ? Format.CSV : Format.JSON;

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
        UserEntity user = userService.getUser(requester.getId());
        try {
            AssessmentJob job = uowFactory.create(
                    AssessmentJob.class,
                    new Class<?>[]{
                            AssessmentService.class, TermListService.class,
                            CreateAssessmentRequest.class, UserEntity.class
                    },
                    new Object[]{
                            assessmentService, termListService,
                            req, user
                    }
            );

            threadPool.submit(job);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("message", "The assessment request was added to the queue.");
            resultMap.put("queued", ((ThreadPoolExecutor) threadPool).getQueue().size());
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

}