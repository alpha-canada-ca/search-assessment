// AssessmentJob.java
package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.api.CreateAssessmentRequest;
import ca.canada.digital.search.assessment.model.Term;
import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.model.TermList;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.object.SeleniumDriver;
import ca.canada.digital.search.assessment.process.TermEvaluationProcess;
import io.dropwizard.hibernate.UnitOfWork;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AssessmentJob implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AssessmentJob.class);

    private final AssessmentService assessmentService;
    private final TermListService termListService;
    private final CreateAssessmentRequest request;
    private final UserEntity requester;

    public AssessmentJob(AssessmentService assessmentService,
                         TermListService termListService,
                         CreateAssessmentRequest request,
                         UserEntity requester) {
        this.assessmentService = assessmentService;
        this.termListService = termListService;
        this.request = request;
        this.requester = requester;
    }

    /**
     * Run the whole job inside a Hibernate session/transaction.
     */
    @Override
    @UnitOfWork
    public void run() {
        WebDriver driver = null;
        try {
            LOG.info("Starting assessment '{}' for list {} requested by {}",
                    request.getName(), request.getListId(), requester.getEmail());

            TermList list = termListService.getTermList(request.getListId());
            List<Term> searchTerms = termListService.listTerms(requester, request.getListId());

            driver = SeleniumDriver.INSTANCE.getDriver();
            driver.manage().window().setSize(new Dimension(1920, 1080));

            TermEvaluationProcess proc =
                    new TermEvaluationProcess(searchTerms, list.getDepartment(), list.getLanguage(), driver);

            List<TermAssessment> termAssessments = proc.execute();

            LOG.info("Persisting assessment '{}' for list {}", request.getName(), request.getListId());
            assessmentService.create(requester, request, termAssessments);

            LOG.info("Assessment '{}' finished successfully.", request.getName());
        } catch (Exception e) {
            LOG.error("Assessment '{}' for list {} failed.", request.getName(), request.getListId(), e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignore) {
                }
            }
        }
    }
}