package ca.canada.digital.search.assessment.process;

import ca.canada.digital.search.assessment.model.*;
import ca.canada.digital.search.assessment.object.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TermEvaluationProcess {
    public static final String USER_AGENT = "Canada Search Assessment Tool";
    private static final int NUM_OF_FIRST_URLS_TO_TEST = 10;
    private static final int NUM_OF_FIRST_RESULTS_TO_PASS = 3;
    private static final int DELAY_BETWEEN_PAGE_HITS = 500; // in milliseconds
    private static final String GOOGLE_URL_PATTERN = "^(https://www.google.\\w{2,3}?/url?.*q=)?(http.*)([&?])sa.*$";
    private static final String GOOGLE_XPATH = "//*[@id=\"main\"]/div/div/div[1]/a";
    private static final String CANADA_CSS_PATH = "section h3 > a";
    private static final Logger LOG = LoggerFactory.getLogger(TermEvaluationProcess.class);
    private final List<Term> terms;
    private final List<TermAssessment> assessmentTerms = new ArrayList<>();
    private final Department department;
    private final Language lang;
    private final List<SearchResult> searchResults = new ArrayList<>();
    WebDriver driver;

    public TermEvaluationProcess(List<Term> terms, Department department, Language lang, WebDriver driver) {
        this.terms = terms;
        this.department = department;
        this.lang = lang;
        this.driver = driver;

    }

    public List<TermAssessment> execute() {

        try {
            // Fetch the all search pages and get the top search results
            for (TermAssessment.SearchType type : TermAssessment.SearchType.values()) {
                fetchSearchResults(type);
            }
            // Evaluate the top NUM_OF_FIRST_URLS_TO_TEST URLs
            evaluateSearchResults();
            // Populate target URLs metadata
            setMetadata();

        } catch (URISyntaxException e) {
            LOG.error("The search page URL is not properly formatted.", e);
        }

        return assessmentTerms;

    }

    private void setMetadata() throws URISyntaxException {
        MetadataProcess metaProcess;

        for (TermAssessment assessmentsTerm : assessmentTerms) {
            if (!StringUtils.isEmpty(assessmentsTerm.getTargetUrl())) {
                URI uri = new URI(assessmentsTerm.getTargetUrl().trim());

                metaProcess = new MetadataProcess(uri);
                assessmentsTerm.setMetadata(metaProcess.execute());
            } else {
                LOG.warn("The term {} has no target URL.", assessmentsTerm.getTerm());
            }
        }
    }

    private void evaluateSearchResults() {
        for (SearchResult searchResult : searchResults) {
            int count = Math.min(searchResult.getReturnedUrls().size(), NUM_OF_FIRST_URLS_TO_TEST);
            TermAssessment ta = new TermAssessment();
            Term term = searchResult.getTerm();
            ta.setTerm(term.getTerm());
            ta.setSearchType(searchResult.getSearchType());
            // default values
            ta.setPass(false);
            ta.setPosition(0);
            ta.setSequence(term.getSequence());
            ta.setTargetUrl(term.getTargetUrls().get(0).getUrl()); // We will have the first target URL as the default for metadata

            for (int i = 0; i < count; i++) { // results to be processed
                // The evaluation logic
                for (TargetUrl url : term.getTargetUrls()) { // Check for each URL and stop if any is found
                    if (!StringUtils.isEmpty(url.getUrl())
                            && url.getUrl().trim().equals(
                            searchResult.getReturnedUrls().get(i).trim())) {
                        ta.setPosition(i + 1);
                        ta.setTargetUrl(url.getUrl());
                        if (i < NUM_OF_FIRST_RESULTS_TO_PASS) {
                            ta.setPass(true);
                        }
                        break;
                    }
                }
            }
            assessmentTerms.add(ta);
        }
    }

    private void fetchSearchResults(TermAssessment.SearchType type) throws URISyntaxException {
        String searchUrl = department.getSearchUrl(type, lang);
        if (StringUtils.isEmpty(searchUrl)) {
            return;
        }
        URIBuilder uriBuilder = new URIBuilder(searchUrl);
        int errorCount = 0;

        boolean abort = false;

        for (Term term : terms) {

            if (abort) {
                break;
            }
            SearchResult searchResult = new SearchResult();

            try {
                uriBuilder.setParameter("q", term.getTerm());
                uriBuilder.setParameter("lang", lang.getCode());

                URI uri = uriBuilder.build();
                LOG.info("{}: {}", "Fetching URL", uri.toString());

                Document doc;

                if (type == TermAssessment.SearchType.GOOGLE) {

                    doc = Jsoup.connect(uri.toString()).userAgent(USER_AGENT).get();


                } else {

                    // Using Selenium to get the dynamically loaded content on a webpage. Search results are now loaded on the client-side on Canada.ca
                    driver.get(uri.toString());

                    Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(5));

                    By resultItem = By.cssSelector(CANADA_CSS_PATH);

                    // wait to get the results html elements
                    wait.until(ExpectedConditions.presenceOfElementLocated(resultItem));

                    doc = Jsoup.parse(Objects.requireNonNull(driver.getPageSource()));

                }

                if (!StringUtils.isEmpty(doc.html())) {

                    Elements urlElements;

                    if (type == TermAssessment.SearchType.GOOGLE) {
                        urlElements = doc.selectXpath(GOOGLE_XPATH); // This changes often due to Google's UI changes
                    } else {
                        urlElements = doc.select(CANADA_CSS_PATH);
                    }

                    List<String> urls = new ArrayList<>();

                    for (Element e : urlElements) {
                        if (type == TermAssessment.SearchType.GOOGLE) {
                            String googleUrl = e.attr("abs:href");
                            Pattern pattern = Pattern.compile(GOOGLE_URL_PATTERN);
                            Matcher m = pattern.matcher(googleUrl);

                            if (m.find()) {
                                urls.add(m.group(2));
                            } else {
                                LOG.warn("It seems like Google has made a structure change to their search results page. Fix Google XPath and URL pattern.");
                                LOG.warn(googleUrl);
                                abort = true;
                                break;
                            }

                        } else {
                            urls.add(e.attr("abs:href"));
                        }
                    }
                    searchResult.setTerm(term);
                    searchResult.setReturnedUrls(urls);
                    searchResult.setSearchType(type);
                    searchResults.add(searchResult);

                } else {
                    errorCount++;
                    if (errorCount >= 3) {
                        LOG.error("The search page returned multiple errors and the process was aborted.");
                        return;
                    }
                }

                TimeUnit.MILLISECONDS.sleep(DELAY_BETWEEN_PAGE_HITS);

            } catch (IOException e) {
                LOG.warn("It seems like Google has blocked us!", e);
                abort = true;
            } catch (InterruptedException e) {
                LOG.error("Could not fetch results set for the search term {}", term.getTerm(), e);
            }
        }

    }
}