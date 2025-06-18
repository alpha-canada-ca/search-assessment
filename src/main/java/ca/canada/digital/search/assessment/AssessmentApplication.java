package ca.canada.digital.search.assessment;

import ca.canada.digital.search.assessment.config.AssessmentConfiguration;
import ca.canada.digital.search.assessment.model.*;
import ca.canada.digital.search.assessment.service.LegacyAssessmentResource;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import org.apache.hc.client5.http.classic.HttpClient;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class AssessmentApplication extends Application<AssessmentConfiguration> {

    public static final String RESOURCES_DIR = "src/main/resources/";
    public static AssessmentConfiguration config;
    private static Logger LOG = LoggerFactory.getLogger(AssessmentApplication.class);
    private final HibernateBundle<AssessmentConfiguration> hibernate = new HibernateBundle<AssessmentConfiguration>(Assessment.class, Department.class, GenericTerm.class, Language.class, Metadata.class, TargetUrl.class, Term.class, TermAssessment.class, TermList.class, UserEntity.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AssessmentConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    public static void main(String[] args) throws Exception {
        new AssessmentApplication().run(args);
    }

    public static AssessmentConfiguration getConfig() {
        return config;
    }

    private static void setConfig(AssessmentConfiguration config) {
        AssessmentApplication.config = config;
    }

    /*
     * Called on application startup.
     */
    @Override
    public void run(AssessmentConfiguration assessmentConfiguration, Environment environment) {
        LOG.info("Starting...");

        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        setConfig(assessmentConfiguration);
        final HttpClient httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration())
                .build(getName());
        environment.jersey().register(new LegacyAssessmentResource(httpClient));
        LOG.info("Started.");
    }

    @Override
    public void initialize(Bootstrap<AssessmentConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }
}
