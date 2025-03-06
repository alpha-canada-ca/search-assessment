package ca.canada.digital.search.assessment;

import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.apache.http.client.HttpClient;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.canada.digital.search.assessment.config.AnalysisConfiguration;
import ca.canada.digital.search.assessment.service.QueryResource;

public class AssessmentApplication extends Application<AnalysisConfiguration> {

	private static Logger LOG = LoggerFactory.getLogger(AssessmentApplication.class);
	public static final String RESOURCES_DIR = "src/main/resources/";
	public static AnalysisConfiguration config;

	public static void main(String[] args) throws Exception {
		new AssessmentApplication().run(args);
	}

	/*
	 * Called on application startup.
	 */
	@Override
	public void run(AnalysisConfiguration analysisConfiguration, Environment environment) {
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
		
		setConfig(analysisConfiguration);
	    final HttpClient httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration())
                .build(getName());
		environment.jersey().register(new QueryResource(httpClient));
		LOG.info("Started.");
	}

	private static void setConfig(AnalysisConfiguration config) {
		AssessmentApplication.config = config;
	}
	
	public static AnalysisConfiguration getConfig() {
		return config;
	}

}
