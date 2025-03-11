package ca.canada.digital.search.assessment.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.canada.digital.search.assessment.io.impl.Airtable;
import ca.canada.digital.search.assessment.object.CsvHeader;
import ca.canada.digital.search.assessment.object.Department;
import ca.canada.digital.search.assessment.object.Format;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.object.Metadata;
import ca.canada.digital.search.assessment.object.SearchTerm;
import ca.canada.digital.search.assessment.object.SearchType;
import ca.canada.digital.search.assessment.object.TermEvaluation;
import ca.canada.digital.search.assessment.process.LanguageProcess;
import ca.canada.digital.search.assessment.process.MetadataProcess;
import ca.canada.digital.search.assessment.process.TermEvaluationProcess;
import ca.canada.digital.search.assessment.util.DateUtil;
import ca.canada.digital.search.assessment.util.ResponseUtil;
import io.swagger.annotations.Api;

@Api(value = "QueryResource")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResource {

	private static final Logger LOG = LoggerFactory.getLogger(QueryResource.class);

	private HttpClient httpClient;
	private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>());

	public QueryResource(HttpClient httpClient) {
		this.httpClient = httpClient;

	}

	@GET
	@Path("/lastUpdate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lastUpdate(@QueryParam("source") String source,
			@QueryParam("lang") @DefaultValue("en") String lang) {
		LOG.info("lang: {} | source: {}", lang, source);
		try {
			Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;
			Airtable airtable = new Airtable(httpClient, SearchType.CONTEXTUAL, language);

			List<Department> departments = airtable.getDepartments();

			Department department = stringToDepartment(source, departments);
			if (department == null) {
				return ResponseUtil.errorResponse("Provide a proper source parameter. e.g. source=CRA");
			}

			Date lastUpdate = airtable.getLastDate(department);
			// Calendar cal = Calendar.getInstance();
			// cal.setTime(lastUpdate);

			if (lastUpdate != null) {
				Map<String, Object> results = new HashMap<>();
				// results.put("year", cal.get(Calendar.YEAR));
				// results.put("month", cal.get(Calendar.MONTH) + 1);
				results.put("date", DateUtil.dateToString(lastUpdate));
				Map<String, Object> lastUpdateMap = new HashMap<>();
				lastUpdateMap.put("lastUpdate", results);
				return ResponseUtil.successResponse(lastUpdateMap);
			}

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while fetching the data.");
	}

	@GET
	@Path("/availableDates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response availableDates(@QueryParam("source") String source,
			@QueryParam("lang") @DefaultValue("en") String lang,
			@QueryParam("type") @DefaultValue("global") String type) {
		LOG.info("lang: {} | source: {}", lang, source);
		try {

			SearchType searchType = Airtable.stringToSearchType(type, null);
			if (searchType == null) {
				return ResponseUtil
						.errorResponse("Provide a proper search type parameter. e.g. type=contextual (default)");
			}
			Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;

			Airtable airtable = new Airtable(httpClient, null, language);

			List<Department> departments = airtable.getDepartments();

			Department department = stringToDepartment(source, departments);
			if (department == null) {
				return ResponseUtil.errorResponse("Provide a proper source parameter. e.g. source=CRA");
			}

			boolean hasContextual = !department.getSearchPage(SearchType.GLOBAL, language)
					.equalsIgnoreCase(department.getSearchPage(SearchType.CONTEXTUAL, language));

			searchType = (searchType == SearchType.CONTEXTUAL) && !hasContextual ? SearchType.GLOBAL : searchType;

			airtable = new Airtable(httpClient, searchType, language);

			List<Date> dates = airtable.getAllArchivedDates(department);
			// Calendar cal = Calendar.getInstance();
			// cal.setTime(lastUpdate);

			Map<String, Object> results = new HashMap<>();
			List<String> availableDates = new ArrayList<>();

			if (dates != null && !dates.isEmpty()) {
				for (Date date : dates) {
					availableDates.add(DateUtil.dateToString(date));
				}

			} else {
				dates = new ArrayList<>();
			}
			results.put("dates", availableDates);

			return ResponseUtil.successResponse(results);

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while fetching the data.");
	}

	@POST
	@Path("/departments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addDepartment(@QueryParam("nameEn") String nameEn, @QueryParam("nameFr") String nameFr,
			@QueryParam("acronymEn") String acronymEn, @QueryParam("acronymFr") String acronymFr,
			@QueryParam("urlEn") String urlEn, @QueryParam("urlFr") String urlFr) {

		LOG.info("nameEn: {} | acronymEn: {}", nameEn, acronymEn);

		if (StringUtils.isEmpty(nameEn) || StringUtils.isEmpty(nameFr) || StringUtils.isEmpty(acronymEn)
				|| StringUtils.isEmpty(acronymFr)) {
			return ResponseUtil.errorResponse("Provide the name and the acronym of the department in both languages.");
		}

		if ((StringUtils.isEmpty(urlEn) && !StringUtils.isEmpty(urlFr))
				|| (!StringUtils.isEmpty(urlEn) && StringUtils.isEmpty(urlFr))) {
			return ResponseUtil.errorResponse(
					"If you provide a contextual search URL, you have to provide it in English and French.");
		}

		try {
			Airtable airtable = new Airtable(httpClient, null, null);

			Department department = new Department();
			department.setNameEn(nameEn);
			department.setNameFr(nameFr);
			department.setAcronymEn(acronymEn);
			department.setAcronymFr(acronymFr);

			if (!StringUtils.isEmpty(urlFr) && !StringUtils.isEmpty(urlEn)) {
				department.setUrlEn(urlEn);
				department.setUrlFr(urlFr);
			}

			// Step 1: Add the department to the Departments table
			if (!airtable.addDepartment(department)) {
				return ResponseUtil.errorResponse("Could not add department to Departments table.");
			}

			Language[] langs = { Language.ENGLISH, Language.FRENCH };

			for (Language lang : langs) {

				airtable = new Airtable(httpClient, null, lang);

				// Step 2: Create tables in the Search Terms base
				if (!airtable.createTable(department, false)) {
					return ResponseUtil.errorResponse("Could not create tables in the Search Terms base.");
				}

				// Step 3: Create tables in the Archived base
				if (!airtable.createTable(department, true)) {
					return ResponseUtil.errorResponse("Could not create tables in the data Airtable base.");
				}

			}

			Map<String, Object> results = new HashMap<>();
			results.put("department", department);
			return ResponseUtil.successResponse(results);

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while fetching the data.");
	}

	@GET
	@Path("/departments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDepartments() {
		try {
			Airtable airtable = new Airtable(httpClient, null, null);

			List<Department> departments = airtable.getDepartments();

			if (departments != null && !departments.isEmpty()) {
				Map<String, Object> results = new HashMap<>();
				results.put("departments", departments);

				return ResponseUtil.successResponse(results);
			}

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while fetching the data.");
	}

	@GET
	@Path("/assessment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessment(@QueryParam("source") String source,
			@QueryParam("lang") @DefaultValue("en") String lang, @QueryParam("date") String dateString,
			@QueryParam("type") String type,
			@QueryParam("format") @DefaultValue("json") String format) {
		LOG.info("lang: {} | source: {}", lang, source);
		
		try {
			SearchType searchType = Airtable.stringToSearchType(type, null);
			
			Format fileFormat = Format.CSV.getFormat().equalsIgnoreCase(format) ? Format.CSV : Format.JSON;
			Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;
			Airtable airtable = new Airtable(httpClient, searchType, language);

			List<Department> departments = airtable.getDepartments();

			Department department = stringToDepartment(source, departments);
			if (department == null) {
				return ResponseUtil.errorResponse("Provide a proper source parameter. e.g. source=CRA");
			}

			boolean hasContextual = !department.getSearchPage(SearchType.GLOBAL, language)
					.equalsIgnoreCase(department.getSearchPage(SearchType.CONTEXTUAL, language));

			searchType = (searchType == SearchType.CONTEXTUAL) && !hasContextual ? SearchType.GLOBAL : searchType;

			Date date = null;
			Date archiveDate = null;

			if (StringUtils.isEmpty(dateString)) { // latest assessment
				archiveDate = airtable.getLastArchivedDate(department);
			} else if (DateUtil.stringToDate(dateString) == null) { // wrong format
				return ResponseUtil.errorResponse("Provide a proper date in the format of yyyy-MM-dd");
			} else { // specific date analysis
				date = DateUtil.stringToDate(dateString);
				Boolean isArchived = airtable.isDateArchived(date, department);
				if (isArchived == null) {
					return ResponseUtil.errorResponse("Could not reach the archive Airtable");
				} else if (isArchived) {
					archiveDate = date;
				}
			}

			Map<SearchType, List<TermEvaluation>> evaluatedTerms = null;

			if (archiveDate != null) {
				evaluatedTerms = airtable.getArchivedEvaluatedTerms(archiveDate, department);

			} else if (date == null) {
				evaluatedTerms = new HashMap<>();
			} else {
				return ResponseUtil.errorResponse("The provided date does not exist.");
			}



			float globalPasses = 0;
			float googlePasses = 0;
			float contextualPasses = 0;
			int numTerms = 0;
			
			Map<String, List<Map<String, Object>>> allTerms = new HashMap<>();

			if (fileFormat == Format.CSV) {
				
				final Map<SearchType, List<TermEvaluation>> et = evaluatedTerms;

				StreamingOutput entity = new StreamingOutput() {
					@Override
					public void write(OutputStream out) {
						writeCsv(out, language, et);
					}
				};

				return Response.ok(entity)
						.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s-%s-%s.csv\"",
								department.getAcronymEn(), searchType != null ? searchType.toString() : "all", DateUtil.dateToString(archiveDate)))
						.build();

			} else {

				if (evaluatedTerms != null && !evaluatedTerms.isEmpty()) {

					for (Entry<SearchType, List<TermEvaluation>> entry : evaluatedTerms.entrySet()) {
						List<Map<String, Object>> terms = new ArrayList<>();
						SearchType st = entry.getKey();
						if (numTerms < 1) {
							numTerms = entry.getValue().size();
						}
						for (TermEvaluation te : entry.getValue()) {
							Map<String, Object> term = new HashMap<>();
							if (te.isPass()) {
								if (st == SearchType.CONTEXTUAL) {
									contextualPasses++;
								} else if (st == SearchType.GLOBAL) {
									globalPasses++;
								} else if (st == SearchType.GOOGLE) {
									googlePasses++;
								}
							}
							term.put("term", te.getSearchTerm().getTerm());
							term.put("isPass", te.isPass());
							term.put("passingUrl", te.getSearchTerm().getTargetUrl());
							term.put("passingUrlPosition", te.getPassUrlPosition());
	
							LanguageProcess lp = new LanguageProcess(te.getSearchTerm().getTerm(), language);
	
							term.put("passingUrlMetadata", getMetadata(lp, te.getTargetUrlMetadata()));
							terms.add(term);
						}
						if (st != SearchType.GOOGLE || googlePasses > 0) {
							allTerms.put(st.toString().toLowerCase(), terms);
						}
					}

				}

				Map<String, Object> resultMap = new HashMap<>();
				if (!StringUtils.isEmpty(type)) {
					resultMap.put("currentSearchUrl", department.getSearchPage(searchType, language));
				}
				if (hasContextual) {
					resultMap.put("contextualUrl", department.getSearchPage(SearchType.CONTEXTUAL, language));
				}
				resultMap.put("globalUrl", department.getSearchPage(SearchType.GLOBAL, language));
				resultMap.put("googleUrl", department.getSearchPage(SearchType.GOOGLE, language));
				resultMap.put("lang", language.getCode());
				resultMap.put("hasContextual", hasContextual);
				resultMap.put("date", DateUtil.dateToString(archiveDate));
				if (searchType != null) {
					resultMap.put("type", searchType.toString());
				}
				resultMap.put("department", department);
				resultMap.put("evaluatedTerms", allTerms);
				if (evaluatedTerms.containsKey(SearchType.CONTEXTUAL)) {
					resultMap.put("contextualSuccessRate",
							contextualPasses > 0 ? String.format("%.01f", (contextualPasses / numTerms) * 100) + "%" : "0%");
				}
				if (evaluatedTerms.containsKey(SearchType.GLOBAL)) {
					resultMap.put("globalSuccessRate",
							globalPasses > 0 ? String.format("%.01f", (globalPasses / numTerms) * 100) + "%" : "0%");
				}
				if (evaluatedTerms.containsKey(SearchType.GOOGLE)) {
					resultMap.put("googleSuccessRate",
							googlePasses > 0 ? String.format("%.01f", (googlePasses / numTerms) * 100) + "%" : "0%");
				}
				return ResponseUtil.successResponse(resultMap);

			}

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while processing the data.");
	}

	@GET
	@Path("/urlAssessment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUrlAssessment(@QueryParam("url") String url, @QueryParam("source") String source,
			@QueryParam("lang") @DefaultValue("en") String lang) {
		LOG.info("lang: {} | source: {}", lang, source);

		try {
			Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;
			Airtable airtable = new Airtable(httpClient, null, language);

			List<Department> departments = airtable.getDepartments();

			Department department = stringToDepartment(source, departments);
			if (department == null) {
				return ResponseUtil.errorResponse("Provide a proper source parameter. e.g. source=CRA");
			}

			boolean hasContextual = !department.getSearchPage(SearchType.GLOBAL, language)
					.equalsIgnoreCase(department.getSearchPage(SearchType.CONTEXTUAL, language));

			List<Map<String, Object>> globalTerms = new ArrayList<>();
			List<Map<String, Object>> googleTerms = new ArrayList<>();
			List<Map<String, Object>> contextualTerms = new ArrayList<>();

			float globalPasses = 0;
			float googlePasses = 0;
			float contextualPasses = 0;

			List<TermEvaluation> evaluatedTerms = null;

			evaluatedTerms = airtable.getArchivedEvaluatedTerms(url, department);

			if (evaluatedTerms != null && !evaluatedTerms.isEmpty()) {

				for (TermEvaluation te : evaluatedTerms) {
					SearchType type = te.getSearchType();

					Map<String, Object> term = new HashMap<>();
					term.put("date", DateUtil.dateToString(te.getSearchTerm().getLastOccurance()));
					term.put("term", te.getSearchTerm().getTerm());
					term.put("isPass", te.isPass());
					term.put("passingUrl", te.getSearchTerm().getTargetUrl());
					term.put("passingUrlPosition", te.getPassUrlPosition());

					LanguageProcess lp = new LanguageProcess(te.getSearchTerm().getTerm(), language);

					term.put("passingUrlMetadata", getMetadata(lp, te.getTargetUrlMetadata()));

					if (type == SearchType.GLOBAL) {
						if (te.isPass()) {
							globalPasses++;
						}
						globalTerms.add(term);
					} else if (type == SearchType.GOOGLE) {
						if (te.isPass()) {
							googlePasses++;
						}
						googleTerms.add(term);
					} else {
						if (te.isPass()) {
							contextualPasses++;
						}
						contextualTerms.add(term);
					}

				}

			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("lang", language.getCode());
			resultMap.put("hasContextual", hasContextual);
			resultMap.put("department", department);
			resultMap.put("globalTerms", globalTerms);
			resultMap.put("globalSuccessRate",
					globalPasses > 0 ? String.format("%.01f", (globalPasses / globalTerms.size()) * 100) + "%" : "0%");
			resultMap.put("googleTerms", googleTerms);
			resultMap.put("googleSuccessRate",
					googlePasses > 0 ? String.format("%.01f", (googlePasses / googleTerms.size()) * 100) + "%" : "0%");
			if (hasContextual) {
				resultMap.put("contextualTerms", contextualTerms);
				resultMap.put("contextualSuccessRate",
						contextualPasses > 0
								? String.format("%.01f", (contextualPasses / contextualTerms.size()) * 100) + "%"
								: "0%");
			}
			resultMap.put("globalSuccessRate",
					globalPasses > 0 ? String.format("%.01f", (globalPasses / globalTerms.size()) * 100) + "%" : "0%");
			return ResponseUtil.successResponse(resultMap);

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while processing the data.");
	}

	@GET
	@Path("/analyze")
	@Produces(MediaType.APPLICATION_JSON)
	public Response analyze(@QueryParam("source") String source, @QueryParam("lang") @DefaultValue("en") String lang) {
		LOG.info("lang: {} | source: {}", lang, source);

		try {
			Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;

			List<Department> departments = new Airtable(httpClient, null, language).getDepartments();
			if (departments == null || departments.isEmpty()) {
				return ResponseUtil.errorResponse("Could not fetch the data from. Please try again.");
			}

			Department department = stringToDepartment(source, departments);
			if (department == null) {
				return ResponseUtil.errorResponse("Provide a proper source parameter. e.g. source=CRA");
			}

			boolean hasContextual = !department.getSearchPage(SearchType.GLOBAL, language)
					.equalsIgnoreCase(department.getSearchPage(SearchType.CONTEXTUAL, language));

			int count = 0;
			Map<String, Object> queues = new HashMap<>();
			// Run the analysis for all search types and dates
			for (SearchType type : SearchType.values()) {
				if (type != SearchType.CONTEXTUAL || hasContextual) {
					Airtable airtable = new Airtable(httpClient, type, language);
					// Get current and archived dates
					List<Date> dates = airtable.getAllDates(department);
					List<Date> archivedDates = airtable.getAllArchivedDates(department);
					// Get the delta dates then run the analysis against them
					List<Date> delta = getDelta(dates, archivedDates);

					List<String> queue = new ArrayList<>();
					for (Date date : delta) {
						queue.add(DateUtil.dateToString(date));
						// Run the analysis for all search types and dates
						threadPool.submit(new Runnable() {

							@Override
							public void run() {
								// Check in case this analysis was already requested and completed
								if (!airtable.isDateArchived(date, department)) {
									LOG.info("Running a new assessment for {} dated {} for {}...", department.getAcronymEn(), DateUtil.dateToString(date), type);

									WebDriver driver = SeleniumDriver.INSTANCE.getDriver();
									List<SearchTerm> searchTerms = airtable.getSearchTerms(date, department);

									TermEvaluationProcess tep = new TermEvaluationProcess(searchTerms, department, type,
											language, driver);

									List<TermEvaluation> evaluatedTerms = tep.execute();

									LOG.info("Archiving the assessment for {} dated {}...", department.getAcronymEn(), DateUtil.dateToString(date));
									boolean archived = airtable.archiveEvaluatedTerms(evaluatedTerms, department);
									if (!archived) {
										LOG.error("Archiving the new assessment for {} dated {} has failed.",
												department.getAcronymEn(), DateUtil.dateToString(date));
									} else {
										LOG.info("Archiving the new assessment for {} dated {} was successful.", department.getAcronymEn(), DateUtil.dateToString(date));
									}
									LOG.info("Closing the Selenium driver.");
									driver.close();
								}
							}
						});

					}
					count = count + queue.size();
					queues.put(type.toString().toLowerCase(), queue);
				}
			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("numAnalysis", count);
			resultMap.put("toAnalyze", queues);
			resultMap.put("queued", threadPool.getPoolSize());
			return ResponseUtil.successResponse(resultMap);

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while processing the data.");
	}

	@GET
	@Path("/analyzeTerms")
	@Produces(MediaType.APPLICATION_JSON)
	public Response analyzeTerms(@QueryParam("term") String term, @QueryParam("text") String text,
			@QueryParam("lang") @DefaultValue("en") String lang) {
		LOG.info("lang: {} | terms: {} | text: {}", lang, term, text);
		Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;

		LanguageProcess lp = new LanguageProcess(term, language);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("term", term);
		resultMap.put("lang", language);
		resultMap.put("processedTerm", lp.getProcessedTerm());
		if (StringUtils.isEmpty(text)) {
			resultMap.put("matches", lp.getMatches(text));
			resultMap.put("highlights", lp.getHighlights(text));
		}
		return ResponseUtil.successResponse(resultMap);

	}

	@GET
	@Path("/analyzeMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public Response analyzeMetadata(@QueryParam("term") String term, @QueryParam("url") String url,
			@QueryParam("lang") @DefaultValue("en") String lang) {
		LOG.info("lang: {} | term: {} | url: {}", lang, term, url);

		if (term == null || term.isEmpty()) {
			return ResponseUtil.errorResponse("Please provide a proper \"term\" parameter.");
		}

		if (url == null || url.isEmpty()) {
			return ResponseUtil.errorResponse("Please provide a proper \"url\" parameter.");
		}

		Language language = Language.FRENCH.getCode().equalsIgnoreCase(lang) ? Language.FRENCH : Language.ENGLISH;

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("term", term);
		resultMap.put("lang", language);
		resultMap.put("url", url);
		LanguageProcess lp = new LanguageProcess(term, language);

		URI uri;
		try {
			uri = new URI(url.trim());

			MetadataProcess metaProcess = new MetadataProcess(uri);

			resultMap.put("metadata", getMetadata(lp, metaProcess.execute()));

			return ResponseUtil.successResponse(resultMap);

		} catch (URISyntaxException e) {
			return ResponseUtil.errorResponse("Something went wrong. Check the URL.");
		}

	}

	private Map<String, Object> getMetadata(LanguageProcess lp, Metadata md) {

		Map<String, Object> metadata = new HashMap<>();

		Map<String, Object> title = new HashMap<>();
		title.put("text", md.getTitle());
		title.put("highlightedText", lp.getHighlights(md.getTitle()));
		title.put("matches", lp.getMatches(md.getTitle()));
		metadata.put("title", title);

		Map<String, Object> description = new HashMap<>();
		description.put("text", md.getDescription());
		description.put("highlightedText", lp.getHighlights(md.getDescription()));
		description.put("matches", lp.getMatches(md.getDescription()));
		metadata.put("description", description);

		Map<String, Object> keywords = new HashMap<>();
		keywords.put("text", md.getKeywords());
		keywords.put("highlightedText", lp.getHighlights(md.getKeywords()));
		keywords.put("matches", lp.getMatches(md.getKeywords()));
		metadata.put("keywords", keywords);

		metadata.put("lastUpdate", DateUtil.dateToString(md.getLastUpdate()));

		return metadata;

	}

	private Department stringToDepartment(String department, List<Department> departments) {
		if (!StringUtils.isEmpty(department) && departments != null && !department.isEmpty()) {
			for (Department dept : departments) {
				if (dept.getAcronymEn().equalsIgnoreCase(department)) {
					return dept;
				}
			}
		}
		return null;
	}

	@GET
	@Path("/validateUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateUser(@QueryParam("username") String username, @QueryParam("password") String password) {

		try {
			Airtable airtable = new Airtable(httpClient, null, null);

			boolean isValid = airtable.validateUser(username, password);

			Map<String, Object> results = new HashMap<>();
			results.put("isValid", isValid);

			return ResponseUtil.successResponse(results);

		} catch (Exception e) {
			LOG.error("Could not complete the REST request.", e);
		}
		return ResponseUtil.errorResponse("Something went wrong while validating.");

	}

	private List<Date> getDelta(List<Date> firstDates, List<Date> secondDates) {
		if (firstDates == null) {
			return new ArrayList<Date>();
		}
		Set<Date> set1 = new HashSet<>(firstDates);
		if (secondDates != null) {
			Set<Date> set2 = new HashSet<>(secondDates);
			set1.removeAll(set2);
		}
		return new ArrayList<Date>(set1);
	}

	@SuppressWarnings("unchecked")
	private void writeCsv(OutputStream out, Language language, Map<SearchType, List<TermEvaluation>> evaluatedTerms) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

			Builder builder = CSVFormat.Builder.create();
			CSVFormat csvFormat = builder
					.setHeader(Arrays.stream(CsvHeader.values()).map(CsvHeader::getHeader).toArray(String[]::new))
					.build();
			CSVPrinter printer = csvFormat.print(writer);

			int count = 0;

			if (evaluatedTerms != null && !evaluatedTerms.isEmpty()) {
				for (Entry<SearchType, List<TermEvaluation>> entry : evaluatedTerms.entrySet()) {
					SearchType st = entry.getKey();
						for (TermEvaluation te : entry.getValue()) {
							count++;
							String searchTerms = te.getSearchTerm().getTerm();
							boolean isPass = te.isPass();
							String targetUrl = te.getSearchTerm().getTargetUrl();
							String pageTitle = null;
							String descriptions = null;
							String keywords = null;
							String type = st.toString();
							LanguageProcess lp = new LanguageProcess(te.getSearchTerm().getTerm(), language);
		
							Map<String, Object> metadata = getMetadata(lp, te.getTargetUrlMetadata());
							if (metadata.get("title") != null) {
								pageTitle = (String) ((Map<String, Object>) metadata.get("title")).get("text");
							}
							if (metadata.get("description") != null) {
								descriptions = (String) ((Map<String, Object>) metadata.get("description")).get("text");
							}
							if (metadata.get("keywords") != null) {
								keywords = (String) ((Map<String, Object>) metadata.get("keywords")).get("text");
							}
							String lastUpdate = (String) metadata.get("lastUpdate");
							int position = te.getPassUrlPosition();
		
							printer.printRecord(count, searchTerms, isPass ? "Pass" : "Fail", targetUrl, pageTitle,
									descriptions, keywords, lastUpdate, position, type);
	
					}
				}
			}
			printer.flush();
			printer.close();

			writer.close();

			out.flush();

		} catch (IOException e) {
			LOG.error("Could not complete the REST request.", e);
		}

	}

	private enum SeleniumDriver {
		INSTANCE;

		private SeleniumDriver() {
		}

		public WebDriver getDriver() {
			return getSeleniumDriver();
		}

		private FirefoxDriver getSeleniumDriver() {
			System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");
			FirefoxOptions options = new FirefoxOptions();
			options.addPreference("general.useragent.override", TermEvaluationProcess.USER_AGENT);
			options.addArguments("-headless");
			options.addArguments("--start-maximized");
			options.addArguments("--single-process");
			options.addArguments("--disable-infobars");
			options.addArguments("--disable-extensions");
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-application-cache");
			options.addArguments("--disable-gpu");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--disable-dev-tools");
			options.addArguments("--no-zygote");

			return new FirefoxDriver(options);
		}
	}

}
