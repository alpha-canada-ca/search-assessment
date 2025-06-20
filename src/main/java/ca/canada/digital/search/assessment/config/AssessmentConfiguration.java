package ca.canada.digital.search.assessment.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
    Reads the configuration from `configuration.yml`.
 */
public class AssessmentConfiguration extends Configuration {

    private final AirtableServer airtableServer = new AirtableServer();
    private final SearchPage searchPage = new SearchPage();
    @Valid
    @NotNull
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();


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

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }
}
