package ca.canada.digital.search.assessment.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

/*
    Reads the configuration from `configuration.yml`.
 */
public class AnalysisConfiguration extends Configuration {

//    private AdobeAnalyticsServer adobeAnalyticsServer = new AdobeAnalyticsServer();
    private AirtableServer airtableServer = new AirtableServer();
    private SearchPage searchPage = new SearchPage();
    
    @Valid
    @NotNull
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    
//    @JsonProperty("adobeAnalyticsServer")
//	public AdobeAnalyticsServer getAdobeServer() {
//		return adobeAnalyticsServer;
//	}

    @JsonProperty("airtableServer")
	public AirtableServer getAirtableServer() {
		return airtableServer;
	}
    
    @JsonProperty("httpClient")
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    @JsonProperty("httpClient")
    public void setHttpClientConfiguration(HttpClientConfiguration httpClient) {
        this.httpClient = httpClient;
    }
    
    @JsonProperty("searchPage")
	public SearchPage getSearchPage() {
		return searchPage;
	}
    
}
