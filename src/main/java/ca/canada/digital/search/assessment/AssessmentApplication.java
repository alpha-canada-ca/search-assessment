package ca.canada.digital.search.assessment;

import ca.canada.digital.search.assessment.auth.BasicUserAuthenticator;
import ca.canada.digital.search.assessment.auth.SessionTokenAuthenticator;
import ca.canada.digital.search.assessment.config.AssessmentConfiguration;
import ca.canada.digital.search.assessment.dao.*;
import ca.canada.digital.search.assessment.model.*;
import ca.canada.digital.search.assessment.resource.DepartmentResource;
import ca.canada.digital.search.assessment.resource.LegacyAssessmentResource;
import ca.canada.digital.search.assessment.resource.UserResource;
import ca.canada.digital.search.assessment.service.DepartmentService;
import ca.canada.digital.search.assessment.service.UserService;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
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
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class AssessmentApplication extends Application<AssessmentConfiguration> {

    public static final String RESOURCES_DIR = "src/main/resources/";
    private static final Logger LOG = LoggerFactory.getLogger(AssessmentApplication.class);
    public static AssessmentConfiguration config;
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

        // DAO
        final AssessmentDao assessmentDao = new AssessmentDao(hibernate.getSessionFactory());
        final DepartmentDao departmentDao = new DepartmentDao(hibernate.getSessionFactory());
        final GenericTermDao genericTermDao = new GenericTermDao(hibernate.getSessionFactory());
        final LanguageDao langDao = new LanguageDao(hibernate.getSessionFactory());
        final MetadataDao metaDao = new MetadataDao(hibernate.getSessionFactory());
        final PasswordResetTokenDao tokenDao = new PasswordResetTokenDao(hibernate.getSessionFactory());
        final TargetUrlDao targetUrlDao = new TargetUrlDao(hibernate.getSessionFactory());
        final TermAssessmentDao termAssessmentDao = new TermAssessmentDao(hibernate.getSessionFactory());
        final TermDao termDao = new TermDao(hibernate.getSessionFactory());
        final TermListDao termListDao = new TermListDao(hibernate.getSessionFactory());
        final UserEntityDao userDao = new UserEntityDao(hibernate.getSessionFactory());
        final UserSessionDao sessionDao = new UserSessionDao(hibernate.getSessionFactory());

        // API Services
        final UserService userService = new UserService(userDao, departmentDao);
        final DepartmentService deptService = new DepartmentService(departmentDao);

        final HttpClient httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration())
                .build(getName());
        final Authenticator basicAuth = new BasicUserAuthenticator(userDao);
        final Authenticator tokenAuth = new SessionTokenAuthenticator(sessionDao);


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

        // Auth
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<UserEntity>()
                        .setAuthenticator(basicAuth)
                        .setRealm("Search Assessment Tool Login")
                        .buildAuthFilter()
        ));

        // OAuth
        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<UserEntity>()
                        .setAuthenticator(tokenAuth)
                        .setPrefix("Bearer")   // looks for `Authorization: Bearer <token>`
                        .buildAuthFilter()
        ));

        // Enable @Auth injection and @RolesAllowed support
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserEntity.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        environment.jersey().register(new LegacyAssessmentResource(httpClient));
        environment.jersey().register(new UserResource(userDao, tokenDao, userService));
        environment.jersey().register(new DepartmentResource(deptService));
        LOG.info("Started.");
    }

    @Override
    public void initialize(Bootstrap<AssessmentConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }
}
