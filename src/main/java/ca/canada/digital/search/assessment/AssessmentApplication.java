package ca.canada.digital.search.assessment;

import ca.canada.digital.search.assessment.auth.BasicUserAuthenticator;
import ca.canada.digital.search.assessment.auth.SessionTokenAuthenticator;
import ca.canada.digital.search.assessment.config.AssessmentConfiguration;
import ca.canada.digital.search.assessment.dao.*;
import ca.canada.digital.search.assessment.exception.DatabaseConstraintViolationMapper;
import ca.canada.digital.search.assessment.model.*;
import ca.canada.digital.search.assessment.resource.*;
import ca.canada.digital.search.assessment.service.*;
import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import org.apache.hc.client5.http.classic.HttpClient;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AssessmentApplication extends Application<AssessmentConfiguration> {

    public static final String RESOURCES_DIR = "src/main/resources/";
    private static final Logger LOG = LoggerFactory.getLogger(AssessmentApplication.class);
    public static AssessmentConfiguration config;
    private final HibernateBundle<AssessmentConfiguration> hibernate = new HibernateBundle<AssessmentConfiguration>(
            Assessment.class, Department.class, GenericTerm.class, Language.class, Metadata.class,
            TargetUrl.class, Term.class, TermAssessment.class, TermList.class, UserEntity.class, UserSession.class) {
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

        setConfig(assessmentConfiguration);

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
        final AssessmentService assessmentService = new AssessmentService(assessmentDao, termListDao, langDao, config);
        final AuthService authService = new AuthService(userDao, sessionDao);
        final DepartmentService deptService = new DepartmentService(departmentDao, userDao);
        final TermAssessmentService termAssessmentService = new TermAssessmentService(termAssessmentDao, langDao);
        final TermListService termService = new TermListService(termListDao, termDao, langDao, userDao, targetUrlDao);
        final UserService userService = new UserService(userDao, departmentDao);
        final LanguageService languageService = new LanguageService(langDao);

        final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());


        final HttpClient httpClient = new HttpClientBuilder(environment).using(getConfig().getHttpClientConfiguration())
                .build(getName());

        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*"); // allow all origins
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                "X-Requested-With,Content-Type,Accept,Origin,Authorization");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
                "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        // Wrap authenticators so that any @UnitOfWork on authenticate() is applied
        final UnitOfWorkAwareProxyFactory uwpf = new UnitOfWorkAwareProxyFactory(hibernate);

        Authenticator<BasicCredentials, UserEntity> basicAuth =
                uwpf.create(
                        BasicUserAuthenticator.class,
                        new Class[]{UserEntityDao.class},
                        new Object[]{userDao}
                );

        Authenticator<String, UserEntity> tokenAuth =
                uwpf.create(
                        SessionTokenAuthenticator.class,
                        new Class[]{UserSessionDao.class},
                        new Object[]{sessionDao}
                );


        // 1) The BasicAuth filter (username+password)
        BasicCredentialAuthFilter<UserEntity> basicFilter =
                new BasicCredentialAuthFilter.Builder<UserEntity>()
                        .setAuthenticator(basicAuth)      // Authenticator<BasicCredentials,UserEntity>
                        .setRealm("Search Assessment Tool Login")
                        .buildAuthFilter();

        // 2) The Bearer/OAuth filter (session-token)
        OAuthCredentialAuthFilter<UserEntity> bearerFilter =
                new OAuthCredentialAuthFilter.Builder<UserEntity>()
                        .setAuthenticator(tokenAuth)      // Authenticator<String,UserEntity>
                        .setPrefix("Bearer")
                        .buildAuthFilter();

        List<AuthFilter> filters = Lists.newArrayList(
                basicFilter,
                bearerFilter
        );

        ChainedAuthFilter chained = new ChainedAuthFilter<>(filters);
        environment.jersey().register(new AuthDynamicFeature(chained));

        // Enable @Auth injection and @RolesAllowed support
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserEntity.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        environment.jersey().register(new AssessmentResource(threadPool, hibernate, assessmentService, termService, termAssessmentService, userService));
        environment.jersey().register(new AuthResource(authService));
        environment.jersey().register(new DepartmentResource(deptService));
        environment.jersey().register(new TermListResource(termService));
        environment.jersey().register(new UserResource(userDao, tokenDao, userService));
        environment.jersey().register(new LanguageResource(languageService));

        environment.jersey().register(new DatabaseConstraintViolationMapper());

        LOG.info("Started.");
    }

    @Override
    public void initialize(Bootstrap<AssessmentConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

}
