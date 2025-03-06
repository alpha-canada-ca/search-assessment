package ca.canada.digital.search.assessment.process;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
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

import ca.canada.digital.search.assessment.object.Department;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.object.SearchResult;
import ca.canada.digital.search.assessment.object.SearchTerm;
import ca.canada.digital.search.assessment.object.SearchType;
import ca.canada.digital.search.assessment.object.TermEvaluation;

public class TermEvaluationProcess {
	private static Logger LOG = LoggerFactory.getLogger(TermEvaluationProcess.class);
	private static final int NUM_OF_FIRST_URLS_TO_TEST = 10;
	private static final int NUM_OF_FIRST_RESULTS_TO_PASS = 3;
	private static final int DELAY_BETWEEN_PAGE_HITS = 500; // in milliseconds
	public static final String USER_AGENT = "Canada Search Assessment Tool";
	private static final String GOOGLE_URL_PATTERN = "^(https:\\/\\/www.google.\\w{2,3}?\\/url?.*q=)?(http.*)(&|\\?)sa.*$";
	private static final String GOOGLE_XPATH = "//*[@id=\"main\"]/div/div/div[1]/a";
	private static final String CANADA_CSS_PATH = "section h3 > a";
	
	private List<SearchTerm> searchTerms;
	private List<TermEvaluation> evaluatedTerms;
	private Department department;
	private SearchType type;
	private Language lang;
	private List<SearchResult> searchResults = new ArrayList<>();
	WebDriver driver = null;
	
	
	public TermEvaluationProcess(List<SearchTerm> searchTerms, Department department, SearchType type, Language lang, WebDriver driver) {
		this.searchTerms = searchTerms;
		this.department = department;
		this.type = type;
		this.lang = lang;
		this.driver = driver;

	}
	
	public List<TermEvaluation> execute() {
		
		try {
			// Fetch the search page and get the top search results
			fetchSearchResults();
			// Evaluate the top NUM_OF_FIRST_URLS_TO_TEST URLs
			evaluateSearchResults();
			// Populate target URLs metadata
			setTargerUrlsMetadata();

		} catch (URISyntaxException e) {
			LOG.error("The search page URL is not properly formatted.", e);
		}
		
		return evaluatedTerms;
		
	}
	
	private void setTargerUrlsMetadata() throws URISyntaxException {
		MetadataProcess metaProcess;
		
		for (TermEvaluation evaluatedTerm : evaluatedTerms) {
			if (!StringUtils.isEmpty(evaluatedTerm.getSearchTerm().getTargetUrl())) {
				URI uri = new URI(evaluatedTerm.getSearchTerm().getTargetUrl().trim());

				metaProcess = new MetadataProcess(uri);
				evaluatedTerm.setTargetUrlMetadata(metaProcess.execute());
			} else {
				LOG.warn("The term {} has no target URL.", evaluatedTerm.getSearchTerm().getTerm());
			}
		}
	}

	private void evaluateSearchResults() {
		evaluatedTerms = new ArrayList<>();
		for (SearchResult searchResult : searchResults) {
			int count = Math.min(searchResult.getReturnedUrls().size(), NUM_OF_FIRST_URLS_TO_TEST);
			TermEvaluation te = new TermEvaluation();
			te.setSearchTerm(searchResult.getSearchTerm());

			for (int i = 0; i < count; i++) {
				// The evaluation logic
				if (searchResult.getSearchTerm() != null
						&& !StringUtils.isEmpty(searchResult.getSearchTerm().getTargetUrl())
						&& searchResult.getSearchTerm().getTargetUrl().trim().equals(
						searchResult.getReturnedUrls().get(i).trim())) {
					te.setPassUrlPosition(i + 1);
					if (i < NUM_OF_FIRST_RESULTS_TO_PASS) {
						te.setPass(true);;
                    }
                    break;
                }
			}
			evaluatedTerms.add(te);
		}
	}

	private boolean fetchSearchResults() throws URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(department.getSearchPage(type, lang));
		int errorCount = 0;
		
		for (SearchTerm searchTerm : searchTerms) {
			SearchResult searchResult = new SearchResult();
			searchResult.setSearchTerm(searchTerm);
								
			searchResults.add(searchResult);
		
			
			try {
				uriBuilder.setParameter("q", searchTerm.getTerm());
				uriBuilder.setParameter("lang", lang.getCode());
				
				URI uri = uriBuilder.build();
				LOG.info("{}: {}", "Fetching URL", uri.toString());
				
				Document doc = null;
				
				if (type == SearchType.GOOGLE) {
					
					doc = Jsoup.connect(uri.toString()).userAgent(USER_AGENT).get();	
					

				} else {
					
			
					// Using Selenium to get the dynamically loaded content on a webpage. Search results are now loaded on the client-side on Canada.ca			        
			        driver.get(uri.toString());
			        
			        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(5));
			        
			        By resultItem = By.cssSelector(CANADA_CSS_PATH);

			        // wait to get the results html elements
			        wait.until(ExpectedConditions.presenceOfElementLocated(resultItem));
			        			        			        	        
			        doc =  Jsoup.parse(driver.getPageSource());
					
				}
				
				if (doc != null && !StringUtils.isEmpty(doc.html())) {

					Elements urlElements;
					
					if (type == SearchType.GOOGLE) {
						urlElements = doc.selectXpath(GOOGLE_XPATH); // This changes often due to Google's UI changes
					} else {
						urlElements = doc.select(CANADA_CSS_PATH);
					}

					List<String> urls = new ArrayList<>();
					
					for (Element e : urlElements) {
						if (type == SearchType.GOOGLE) {
							String googleUrl = e.attr("abs:href");
							Pattern pattern = Pattern.compile(GOOGLE_URL_PATTERN);
							Matcher m = pattern.matcher(googleUrl);
							
							if (m.find()) {
								urls.add(m.group(2));
							} else {
								LOG.warn("It seems like Google has made a structure change to their search results page. Fix Google XPath and URL pattern.");
								LOG.warn(googleUrl);
							}

						} else {
							urls.add(e.attr("abs:href"));
						}
					}
					
					searchResult.setReturnedUrls(urls);
					
				} else {
					errorCount++;
					if (errorCount >= 3) {
						LOG.error("The search page returned multiple errors and the processed was aborted.");
						return false;
					}
				}
				
				TimeUnit.MILLISECONDS.sleep(DELAY_BETWEEN_PAGE_HITS);

			} catch (Exception e) {
				LOG.error("Could not fetch results set for the search term {}", searchTerm.getTerm(), e);
			}
		}
		return true;
			
	}		
}