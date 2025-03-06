package ca.canada.digital.search.assessment.io.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.canada.digital.search.assessment.AssessmentApplication;
import ca.canada.digital.search.assessment.io.DataTable;
import ca.canada.digital.search.assessment.object.Department;
import ca.canada.digital.search.assessment.object.Language;
import ca.canada.digital.search.assessment.object.Metadata;
import ca.canada.digital.search.assessment.object.SearchTerm;
import ca.canada.digital.search.assessment.object.SearchType;
import ca.canada.digital.search.assessment.object.TermEvaluation;
import ca.canada.digital.search.assessment.util.DateUtil;

public class Airtable implements DataTable {
	private static final Logger LOG = LoggerFactory.getLogger(Airtable.class);
	private static final String ENCODING = "UTF-8";
	private static final String API_VERSION = "v0";
	private static final String API_BASES_PATH = "meta/bases";
	private static final String API_TABLES_PATH = "tables";
	private static final int MAX_RESULTS = 100; // It's happens to also be Airtable's limit per page
	private static final int API_POST_LIMIT_PER_CALL = 10;
	private static final String GLOBAL_TABLE = "Global";
	private static final String RESULTS_SET = "records";
	private static final String FIELDS_SET = "fields";
	private static final String MAX_RESULTS_PARAM = "maxRecords";
	private static final String RESULTS_PER_PAGE_PARAM = "pageSize";
	private static final String FILTER_PARAM = "filterByFormula";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_TYPE_TEXT = "singleLineText";
	private static final String FIELD_TYPE_URL = "url";
	private static final String FIELD_TYPE_DATE = "date";
	private static final String FIELD_TYPE_DATE_NAME = "iso";
	private static final String FIELD_TYPE_DATE_FORMAT = "YYYY-MM-DD";
	private static final String FIELD_TYPE_NUMBER = "number";
	private static final String FIELD_TYPE_CHECKBOX = "checkbox";
	private static final String FIELD_TYPE_SINGLE_SELECT = "singleSelect";
	private static final String FIELD_OPTIONS = "options";
	private static final String FIELD_SINGLE_SELECT_CHOICES = "choices";
	private static final String FIELD_NUMBER_PRECISION = "precision";
	private static final String FIELD_CHECKBOX_COLOR = "color";
	private static final String FIELD_CHECKBOX_COLOR_GREEN = "greenBright";
	private static final String FIELD_CHECKBOX_ICON = "icon";
	private static final String FIELD_CHECKBOX_ICON_CHECK = "check";
	private static final String FIELD_DATE_FORMAT = "dateFormat";
	private static final String FIELD_DATE_FORMAT_NAME = "name";
	private static final String FIELD_DATE_FORMAT_FORMAT = "format";
	private static final String QUERY_FIELD = "Search Terms";
	private static final String TARGET_URL_FIELD = "Target URL";
	private static final String TARGET_URL_TITLE = "Expected Result - Title";
	private static final String TARGET_URL_DESC = "Expected Result - Description";
	private static final String TARGET_URL_KEYWORDS = "Expected Result - Keywords";
	private static final String TARGET_URL_LAST_UPDATE_DATE = "Expected Result - Last Update";
	private static final String DATE_FIELD = "Date";
	private static final String ORDER_FIELD = "Order";
	private static final String SOURCE_FIELD = "Source";
	private static final String RESULT_FIELD = "Pass";
	private static final String URL_POSITION_FIELD = "Expected URL Position";
	private static final String USER_TABLE = "Users";
	private static final String USER_USERNAME = "User ID";
	private static final String USER_PASSWORD = "Password";
	private static final String DEPARTMENT_TABLE = "Departments";
	private static final String DEPARTMENT_NAME_EN = "Department/Agency - English";
	private static final String DEPARTMENT_NAME_FR = "Department/Agency - French";
	private static final String DEPARTMENT_ACRONYM_EN = "Acronym - English";
	private static final String DEPARTMENT_ACRONYM_FR = "Acronym - French";
	private static final String DEPARTMENT_SEARCH_URL_EN = "Contextual Search Page - English";
	private static final String DEPARTMENT_SEARCH_URL_FR = "Contextual Search Page - French";

	private HttpClient httpClient;
	private Language lang;
	private String serverUrl;
	private SearchType type;
	private String authHeader = "Bearer " + AssessmentApplication.getConfig().getAirtableServer().getToken();

	public Airtable(HttpClient httpClient, SearchType type, Language lang) {
		this.httpClient = httpClient;
		this.type = type;
		this.lang = lang;
		this.serverUrl = String.format("%s://%s:%s/%s",
				AssessmentApplication.getConfig().getAirtableServer().getProtocol(),
				AssessmentApplication.getConfig().getAirtableServer().getHost(),
				AssessmentApplication.getConfig().getAirtableServer().getPort(), API_VERSION);
	}

	@Override
	public List<Department> getDepartments() {
		try {
			URIBuilder uriBuilder = getDeptsUriBuilder();

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					if (records != null) {
						List<Department> depts = new ArrayList<>();
						for (int i = 0; i < records.length(); i++) {
							Department dept = getDepartmentFromRecord(records.getJSONObject(i));
							depts.add(dept);
						}
						return depts;
					} else {
						LOG.warn("Could not get the departments from Airtable. Check the table using the UI.");
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not fetch departments from Airtable.", e);
		}
		return null;
	}

	@Override
	public boolean addDepartment(Department department) {
		URIBuilder uriBuilder = getDeptsUriBuilder();

		try {
			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpPost post = new HttpPost(uri);
			post.setHeader("Accept", MediaType.APPLICATION_JSON);
			post.setHeader("Content-type", MediaType.APPLICATION_JSON);
			post.setHeader("Authorization", authHeader);

			JSONArray records = new JSONArray();

			JSONObject record = new JSONObject();
			record.put(DEPARTMENT_NAME_EN, department.getNameEn());
			record.put(DEPARTMENT_NAME_FR, department.getNameFr());
			record.put(DEPARTMENT_ACRONYM_EN, department.getAcronymEn());
			record.put(DEPARTMENT_ACRONYM_FR, department.getAcronymFr());
			record.put(DEPARTMENT_SEARCH_URL_EN, department.getUrlEn());
			record.put(DEPARTMENT_SEARCH_URL_FR, department.getUrlFr());

			records.put(new JSONObject().put(FIELDS_SET, record));

			post.setEntity(new StringEntity(new JSONObject().put(RESULTS_SET, records).toString(), ENCODING));
			HttpResponse httpResponse = httpClient.execute(post);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				records = new JSONArray();
				return true;
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
				return false;
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not add department to Airtable.", e);
		}

		return false;
	}

	@Override
	public boolean isAvailable(Department department) {
		try {
			HttpGet get = new HttpGet(getTermsGetUriBuilder(department).build());
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return true;
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("There is something wrong while reaching Airtable.", e);
		}

		return false;
	}

	@Override
	public Date getLastDate(Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(1));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(1));

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					SearchTerm searchTerm = getSearchTermFromRecord(records.getJSONObject(0));
					if (searchTerm != null) {
						return searchTerm.getLastOccurance();
					} else {
						LOG.warn(
								"Could not get the last update date from the current Airtable. Check the table using the UI.");
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not get the last update date from the current Airtable.", e);
		}
		return null;
	}

	@Override
	public List<Date> getAllDates(Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(FILTER_PARAM, String.format("%s=%s", ORDER_FIELD, 1));

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					if (records != null) {
						List<Date> dates = new ArrayList<>();
						for (int i = 0; i < records.length(); i++) {
							SearchTerm searchTerm = getSearchTermFromRecord(records.getJSONObject(i));
							dates.add(searchTerm.getLastOccurance());
						}
						return dates;
					} else {
						LOG.warn(
								"Could not get the available dates from the current Airtable. Check the table using the UI. Attempt");
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not get the last update date from the current Airtable.", e);
		}
		return null;
	}

	@Override
	public List<Date> getAllArchivedDates(Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department, true);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(FILTER_PARAM, String.format("AND(%s=%s, REGEX_MATCH({%s},\"^%s$\"))", ORDER_FIELD,
					1, SOURCE_FIELD, type == SearchType.CONTEXTUAL ? department.getAcronymEn() : type.toString()));

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					if (records != null) {
						List<Date> dates = new ArrayList<>();
						for (int i = 0; i < records.length(); i++) {
							SearchTerm searchTerm = getSearchTermFromRecord(records.getJSONObject(i));
							dates.add(searchTerm.getLastOccurance());
						}
						return dates;
					} else {
						LOG.warn(
								"Could not get the available dates from the current Airtable. Check the table using the UI. Attempt");
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not get the last update date from the current Airtable.", e);
		}
		return null;
	}

	@Override
	public Date getLastArchivedDate(Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department, true);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(1));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(1));
			
			if (type != null)  {
				uriBuilder.addParameter(FILTER_PARAM, String.format("REGEX_MATCH({%s},\"^%s$\")", SOURCE_FIELD,
						type == SearchType.CONTEXTUAL ? department.getAcronymEn() : type.toString()));
				}
			
			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					TermEvaluation termEvaluation = getEvaluatedTermFromRecord(records.getJSONObject(0), department);
					if (termEvaluation != null) {
						return termEvaluation.getSearchTerm().getLastOccurance();
					} else {
						LOG.info(
								"Could not get the last update date from the archived Airtable. Check the table using the UI.");
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not get the last update date from the archived Airtable.", e);
		}
		return null;
	}

	@Override
	public Boolean isDateArchived(Date date, Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department, true);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(1));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(1));
			if (type != null) {
				uriBuilder.addParameter(FILTER_PARAM,
						String.format("AND(IS_SAME({%s},\"%s\"),REGEX_MATCH({%s},\"^%s$\"))", DATE_FIELD,
								DateUtil.dateToString(date), SOURCE_FIELD,
								type == SearchType.CONTEXTUAL ? department.getAcronymEn() : type.toString()));
			} else {
				uriBuilder.addParameter(FILTER_PARAM,
						String.format("AND(IS_SAME({%s},\"%s\"),REGEX_MATCH({%s},\"^%s$\"))", DATE_FIELD,
								DateUtil.dateToString(date), SOURCE_FIELD, SearchType.GLOBAL.toString()));
			}

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					if (records != null && !records.isEmpty()) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not get the last update date from the current Airtable.", e);
			return null;
		}
		return false;
	}

	@Override
	public List<SearchTerm> getSearchTerms(Date date, Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(FILTER_PARAM,
					String.format("IS_SAME({%s}, \"%s\")", DATE_FIELD, DateUtil.dateToString(date)));

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					List<SearchTerm> searchTerms = new ArrayList<>();
					for (int i = 0; i < records.length(); i++) {
						searchTerms.add(getSearchTermFromRecord(records.getJSONObject(i)));
					}

					if (type != SearchType.CONTEXTUAL) {
						uriBuilder = getTermsGetUriBuilder(null);
						uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(MAX_RESULTS));
						uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(MAX_RESULTS));
						uri = uriBuilder.build();
						get = new HttpGet(uri);
						get.setHeader("Authorization", authHeader);

						httpResponse = httpClient.execute(get);
						if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));
							if (!results.isEmpty() && results.has(RESULTS_SET)
									&& !results.getJSONArray(RESULTS_SET).isEmpty()) {
								JSONArray globalRecords = results.getJSONArray(RESULTS_SET);
								List<SearchTerm> globalSearchTerms = new ArrayList<>();
								for (int i = 0; i < globalRecords.length(); i++) {
									globalSearchTerms.add(getSearchTermFromRecord(globalRecords.getJSONObject(i)));
								}
								if (type == SearchType.GOOGLE) {
									appendSourceToSearchTerms(globalSearchTerms, searchTerms, department);
								} else if (!StringUtils.isEmpty(department.getUrlEn())) {
									mergeSearchTerms(globalSearchTerms, searchTerms);
								}
							}
						}
					}

					return searchTerms;
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (Exception e) {
			LOG.error("Could not fetch the search terms from Airtable.", e);
		}

		return null;
	}

	@Override
	public boolean archiveEvaluatedTerms(List<TermEvaluation> terms, Department department) {
		try {
			URIBuilder uriBuilder = getTermsPostUriBuilder(department, true);

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpPost post = new HttpPost(uri);
			post.setHeader("Accept", MediaType.APPLICATION_JSON);
			post.setHeader("Content-type", MediaType.APPLICATION_JSON);
			post.setHeader("Authorization", authHeader);

			JSONArray records = new JSONArray();
			boolean done = false;

			for (int i = 0; i < terms.size(); i++) {
				done = false;
				JSONObject record = new JSONObject();
				record.put(QUERY_FIELD, terms.get(i).getSearchTerm().getTerm());
				record.put(TARGET_URL_FIELD, terms.get(i).getSearchTerm().getTargetUrl());
				record.put(TARGET_URL_TITLE, terms.get(i).getTargetUrlMetadata().getTitle());
				record.put(TARGET_URL_DESC, terms.get(i).getTargetUrlMetadata().getDescription());
				record.put(TARGET_URL_KEYWORDS, terms.get(i).getTargetUrlMetadata().getKeywords());
				record.put(TARGET_URL_LAST_UPDATE_DATE,
						DateUtil.dateToString(terms.get(i).getTargetUrlMetadata().getLastUpdate()));
				record.put(RESULT_FIELD, terms.get(i).isPass());
				record.put(URL_POSITION_FIELD, terms.get(i).getPassUrlPosition());
				record.put(DATE_FIELD, DateUtil.dateToString(terms.get(i).getSearchTerm().getLastOccurance()));
				record.put(ORDER_FIELD, terms.get(i).getSearchTerm().getRank());
				record.put(SOURCE_FIELD, type == SearchType.CONTEXTUAL ? department.getAcronymEn() : type.toString());
				records.put(new JSONObject().put(FIELDS_SET, record));

				if (i > 0 && i % API_POST_LIMIT_PER_CALL - 1 == 0) {
					post.setEntity(new StringEntity(new JSONObject().put(RESULTS_SET, records).toString(), ENCODING));
					HttpResponse httpResponse = httpClient.execute(post);
					if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						records = new JSONArray();
						done = true;
					} else {
						LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
						return false;
					}
				}
			}

			if (!done) {
				post.setEntity(new StringEntity(new JSONObject().put(RESULTS_SET, records).toString(), ENCODING));
				HttpResponse httpResponse = httpClient.execute(post);
				if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					return false;
				}
			}

			return true;

		} catch (Exception e) {
			LOG.error("Could not create record on Airtable.", e);
		}

		return false;
	}

	@Override
	public Map<SearchType, List<TermEvaluation>> getArchivedEvaluatedTerms(Date date, Department department) {
		try {
			List<SearchType> types = new ArrayList<>();

			if (type != null) {
				types.add(type);
			} else {
				types.add(SearchType.GLOBAL);
				types.add(SearchType.GOOGLE);
				if (!StringUtils.isEmpty(department.getUrlEn()) && !StringUtils.isEmpty(department.getUrlFr())) {
					types.add(SearchType.CONTEXTUAL);
				}
			}
			Map<SearchType, List<TermEvaluation>> allEvaluatedTerms = new HashMap<>();
			for (SearchType t : types) {
				URIBuilder uriBuilder = getTermsGetUriBuilder(department, true);
				uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(MAX_RESULTS));
				uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(MAX_RESULTS));
				uriBuilder.addParameter(FILTER_PARAM,
						String.format("AND(IS_SAME({%s},\"%s\"),REGEX_MATCH({%s},\"^%s$\"))", DATE_FIELD,
								DateUtil.dateToString(date), SOURCE_FIELD,
								t == SearchType.CONTEXTUAL ? department.getAcronymEn() : t.toString()));

				URI uri = uriBuilder.build();
				LOG.debug("Airtable call: {}", uri.toString());

				HttpGet get = new HttpGet(uri);
				get.setHeader("Authorization", authHeader);

				HttpResponse httpResponse = httpClient.execute(get);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

					if (!results.isEmpty() && results.has(RESULTS_SET)
							&& !results.getJSONArray(RESULTS_SET).isEmpty()) {
						JSONArray records = results.getJSONArray(RESULTS_SET);
						List<TermEvaluation> evaluatedTerms = new ArrayList<>();
						for (int i = 0; i < records.length(); i++) {
							evaluatedTerms.add(getEvaluatedTermFromRecord(records.getJSONObject(i), department));
						}
						allEvaluatedTerms.put(t, evaluatedTerms);
					}

				} else {
					LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
				}
			}
			return allEvaluatedTerms;

		} catch (Exception e) {
			LOG.error("Could not fetch the search terms from Airtable.", e);
		}

		return null;
	}

	@Override
	public List<TermEvaluation> getArchivedEvaluatedTerms(String targetUrl, Department department) {
		try {
			URIBuilder uriBuilder = getTermsGetUriBuilder(department, true);
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(MAX_RESULTS));
			uriBuilder.addParameter(FILTER_PARAM,
					String.format("REGEX_MATCH({%s},\"^%s$\")", TARGET_URL_FIELD, targetUrl));

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					List<TermEvaluation> evaluatedTerms = new ArrayList<>();
					for (int i = 0; i < records.length(); i++) {
						evaluatedTerms.add(getEvaluatedTermFromRecord(records.getJSONObject(i), department));
					}
					return evaluatedTerms;
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not fetch the search terms from Airtable.", e);
		}

		return null;
	}

	@Override
	public boolean createTable(Department department, boolean isArchive) {
		try {

			// Create Search Terms tables
			URIBuilder uriBuilder = getTablesPostUriBuilder(isArchive);

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpPost post = new HttpPost(uri);
			post.setHeader("Accept", MediaType.APPLICATION_JSON);
			post.setHeader("Content-type", MediaType.APPLICATION_JSON);
			post.setHeader("Authorization", authHeader);

			JSONArray fields = new JSONArray();

			// Query Field
			JSONObject query = new JSONObject();
			query.put(FIELD_NAME, QUERY_FIELD);
			query.put(FIELD_TYPE, FIELD_TYPE_TEXT);

			fields.put(query);

			// Expected Result Field
			JSONObject targetUrl = new JSONObject();
			targetUrl.put(FIELD_NAME, TARGET_URL_FIELD);
			targetUrl.put(FIELD_TYPE, FIELD_TYPE_URL);

			fields.put(targetUrl);

			JSONObject dateOptions = new JSONObject(); // Same options for all date fields
			JSONObject dateFormat = new JSONObject();
			dateFormat.put(FIELD_DATE_FORMAT_NAME, FIELD_TYPE_DATE_NAME);
			dateFormat.put(FIELD_DATE_FORMAT_FORMAT, FIELD_TYPE_DATE_FORMAT);
			dateOptions.put(FIELD_DATE_FORMAT, dateFormat);

			// Date Field
			JSONObject date = new JSONObject();
			date.put(FIELD_NAME, DATE_FIELD);
			date.put(FIELD_TYPE, FIELD_TYPE_DATE);
			date.put(FIELD_OPTIONS, dateOptions);

			fields.put(date);

			JSONObject numberOptions = new JSONObject(); // Same options for all number fields
			numberOptions.put(FIELD_NUMBER_PRECISION, 0);

			// Rank Field
			JSONObject rank = new JSONObject();
			rank.put(FIELD_NAME, ORDER_FIELD);
			rank.put(FIELD_TYPE, FIELD_TYPE_NUMBER);
			rank.put(FIELD_OPTIONS, numberOptions);

			fields.put(rank);

			if (isArchive) {
				// Expected Result - Title Field
				JSONObject targetUrlTitle = new JSONObject();
				targetUrlTitle.put(FIELD_NAME, TARGET_URL_TITLE);
				targetUrlTitle.put(FIELD_TYPE, FIELD_TYPE_TEXT);

				fields.put(targetUrlTitle);

				// Expected Result - Description Field
				JSONObject targetUrlDesc = new JSONObject();
				targetUrlDesc.put(FIELD_NAME, TARGET_URL_DESC);
				targetUrlDesc.put(FIELD_TYPE, FIELD_TYPE_TEXT);

				fields.put(targetUrlDesc);

				// Expected Result - Keywords Field
				JSONObject targetUrlKeywords = new JSONObject();
				targetUrlKeywords.put(FIELD_NAME, TARGET_URL_KEYWORDS);
				targetUrlKeywords.put(FIELD_TYPE, FIELD_TYPE_TEXT);

				fields.put(targetUrlKeywords);

				// Expected Result - Last Update Field
				JSONObject targetUrlDate = new JSONObject();
				targetUrlDate.put(FIELD_NAME, TARGET_URL_LAST_UPDATE_DATE);
				targetUrlDate.put(FIELD_TYPE, FIELD_TYPE_DATE);
				targetUrlDate.put(FIELD_OPTIONS, dateOptions);

				fields.put(targetUrlDate);

				JSONObject checkboxOptions = new JSONObject(); // Same options for checkbox fields
				checkboxOptions.put(FIELD_CHECKBOX_COLOR, FIELD_CHECKBOX_COLOR_GREEN);
				checkboxOptions.put(FIELD_CHECKBOX_ICON, FIELD_CHECKBOX_ICON_CHECK);

				// Pass Field
				JSONObject pass = new JSONObject();
				pass.put(FIELD_NAME, RESULT_FIELD);
				pass.put(FIELD_TYPE, FIELD_TYPE_CHECKBOX);
				pass.put(FIELD_OPTIONS, checkboxOptions);

				fields.put(pass);

				// Expected URL Position Field
				JSONObject pos = new JSONObject();
				pos.put(FIELD_NAME, URL_POSITION_FIELD);
				pos.put(FIELD_TYPE, FIELD_TYPE_NUMBER);
				pos.put(FIELD_OPTIONS, numberOptions);

				fields.put(pos);

				JSONObject singleSelectOptions = new JSONObject();
				JSONArray singleSelectChoices = new JSONArray();
				JSONObject contextualChoice = new JSONObject();
				contextualChoice.put(FIELD_NAME, department.getAcronymEn());
				singleSelectChoices.put(contextualChoice);
				JSONObject globalChoice = new JSONObject();
				globalChoice.put(FIELD_NAME, SearchType.GLOBAL.toString());
				singleSelectChoices.put(globalChoice);
				JSONObject googleChoice = new JSONObject();
				googleChoice.put(FIELD_NAME, SearchType.GOOGLE.toString());
				singleSelectChoices.put(googleChoice);
				singleSelectOptions.put(FIELD_SINGLE_SELECT_CHOICES, singleSelectChoices);

				// Source Field
				JSONObject src = new JSONObject();
				src.put(FIELD_NAME, SOURCE_FIELD);
				src.put(FIELD_TYPE, FIELD_TYPE_SINGLE_SELECT);
				src.put(FIELD_OPTIONS, singleSelectOptions);

				fields.put(src);

			}

			post.setEntity(new StringEntity(new JSONObject().put(FIELDS_SET, fields)
					.put(FIELD_NAME, getTableName(department, lang, false)).toString(), ENCODING));

			HttpResponse httpResponse = httpClient.execute(post);

			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				LOG.info("Airtable response: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
				return false;
			}

			return true;

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could create record on Airtable.", e);
		}

		return false;
	}

	@Override
	public boolean validateUser(String username, String password) {
		try {
			URIBuilder uriBuilder = getUsersUriBuilder();
			uriBuilder.addParameter(MAX_RESULTS_PARAM, String.valueOf(1));
			uriBuilder.addParameter(RESULTS_PER_PAGE_PARAM, String.valueOf(1));
			uriBuilder.addParameter(FILTER_PARAM,
					String.format("AND(REGEX_MATCH({%s},\"^%s$\"),REGEX_MATCH({%s},\"^%s$\"))", USER_USERNAME,
							escapeCharacters(username.trim()), USER_PASSWORD, escapeCharacters(password)));

			URI uri = uriBuilder.build();
			LOG.debug("Airtable call: {}", uri.toString());

			HttpGet get = new HttpGet(uri);
			get.setHeader("Authorization", authHeader);

			HttpResponse httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				JSONObject results = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), ENCODING));
				LOG.debug("Airtable response: {}", results);

				if (!results.isEmpty() && results.has(RESULTS_SET) && !results.getJSONArray(RESULTS_SET).isEmpty()) {
					JSONArray records = results.getJSONArray(RESULTS_SET);
					if (records != null && !records.isEmpty()) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				LOG.error("Airtable Error: {}", EntityUtils.toString(httpResponse.getEntity(), ENCODING));
			}

		} catch (URISyntaxException | IOException e) {
			LOG.error("Could not validate the user credentials.", e);
			return false;
		}
		return false;
	}

	private void mergeSearchTerms(List<SearchTerm> globalSearchTerms, List<SearchTerm> searchTerms) {
		for (SearchTerm gTerm : globalSearchTerms) {
			for (SearchTerm term : searchTerms) {
				if (Arrays.stream(term.getTerm().toLowerCase().split(" "))
						.allMatch(gTerm.getTerm().toLowerCase()::contains)) {
					if (!StringUtils.isEmpty(gTerm.getTargetUrl())) {
						term.setTargetUrl(gTerm.getTargetUrl());
					}
				}
			}
		}
	}

	private void appendSourceToSearchTerms(List<SearchTerm> globalSearchTerms, List<SearchTerm> searchTerms,
			Department department) { // For
		// generic
		// terms
		// assessed
		// on
		// Google
		for (SearchTerm gTerm : globalSearchTerms) {
			for (SearchTerm term : searchTerms) {
				if (Arrays.stream(term.getTerm().split(" ")).allMatch(gTerm.getTerm()::contains)) {
					if (!term.getTerm().toLowerCase().contains(department.getAcronymEn().toLowerCase())) {
						term.setTerm(String.format("%s %s", department.getAcronymEn(), term.getTerm()));
					}
				}
			}
		}
	}

	private SearchTerm getSearchTermFromRecord(JSONObject json) {
		if (!json.isEmpty() && json.has(FIELDS_SET)) {
			JSONObject fields = json.getJSONObject(FIELDS_SET);
			if (!fields.isEmpty() && fields.has(QUERY_FIELD)) {
				SearchTerm searchTerm = new SearchTerm();
				searchTerm.setTerm(fields.getString(QUERY_FIELD));
				if (fields.has(TARGET_URL_FIELD)) {
					searchTerm.setTargetUrl(fields.getString(TARGET_URL_FIELD));
				}
				if (fields.has(ORDER_FIELD)) {
					searchTerm.setRank(fields.getInt(ORDER_FIELD));
				}
				if (fields.has(DATE_FIELD)) {
					searchTerm.setLastOccurance(DateUtil.stringToDate(fields.getString(DATE_FIELD)));
				}
				return searchTerm;
			}
		}
		return null;
	}

	private TermEvaluation getEvaluatedTermFromRecord(JSONObject json, Department department) {
		if (!json.isEmpty() && json.has(FIELDS_SET)) {
			JSONObject fields = json.getJSONObject(FIELDS_SET);
			if (!fields.isEmpty() && fields.has(QUERY_FIELD)) {
				SearchTerm searchTerm = new SearchTerm();
				TermEvaluation termEvaluation = new TermEvaluation();
				searchTerm.setTerm(fields.getString(QUERY_FIELD));
				if (fields.has(TARGET_URL_FIELD)) {
					searchTerm.setTargetUrl(fields.getString(TARGET_URL_FIELD));
				}
				Metadata metadata = new Metadata();
				if (fields.has(TARGET_URL_TITLE)) {
					metadata.setTitle(fields.getString(TARGET_URL_TITLE));
				}
				if (fields.has(TARGET_URL_DESC)) {
					metadata.setDescription(fields.getString(TARGET_URL_DESC));
				}
				if (fields.has(TARGET_URL_KEYWORDS)) {
					metadata.setKeywords(fields.getString(TARGET_URL_KEYWORDS));
				}
				if (fields.has(TARGET_URL_LAST_UPDATE_DATE)) {
					metadata.setLastUpdate(DateUtil.stringToDate(fields.getString(TARGET_URL_LAST_UPDATE_DATE)));
				}
				termEvaluation.setTargetUrlMetadata(metadata);
				if (fields.has(ORDER_FIELD)) {
					searchTerm.setRank(fields.getInt(ORDER_FIELD));
				}
				if (fields.has(DATE_FIELD)) {
					searchTerm.setLastOccurance(DateUtil.stringToDate(fields.getString(DATE_FIELD)));
				}
				termEvaluation.setSearchTerm(searchTerm);
				if (fields.has(RESULT_FIELD)) {
					termEvaluation.setPass(fields.getBoolean(RESULT_FIELD));
				}
				if (fields.has(URL_POSITION_FIELD)) {
					termEvaluation.setPassUrlPosition(fields.getInt(URL_POSITION_FIELD));
				}
				if (fields.has(SOURCE_FIELD)) {
					termEvaluation.setSearchType(stringToSearchType(fields.getString(SOURCE_FIELD), department));
				}
				return termEvaluation;
			}
		}
		return null;
	}

	private Department getDepartmentFromRecord(JSONObject json) {
		if (!json.isEmpty() && json.has(FIELDS_SET)) {
			JSONObject fields = json.getJSONObject(FIELDS_SET);
			if (!fields.isEmpty() && fields.has(DEPARTMENT_ACRONYM_EN) && fields.has(DEPARTMENT_ACRONYM_FR)) {
				Department dept = new Department();
				dept.setAcronymEn(fields.getString(DEPARTMENT_ACRONYM_EN));
				dept.setAcronymFr(fields.getString(DEPARTMENT_ACRONYM_FR));
				if (fields.has(DEPARTMENT_NAME_EN)) {
					dept.setNameEn(fields.getString(DEPARTMENT_NAME_EN));
				}
				if (fields.has(DEPARTMENT_NAME_FR)) {
					dept.setNameFr(fields.getString(DEPARTMENT_NAME_FR));
				}
				if (fields.has(DEPARTMENT_SEARCH_URL_EN)) {
					dept.setUrlEn(fields.getString(DEPARTMENT_SEARCH_URL_EN));
				}
				if (fields.has(DEPARTMENT_SEARCH_URL_FR)) {
					dept.setUrlFr(fields.getString(DEPARTMENT_SEARCH_URL_FR));
				}
				return dept;
			}
		}
		return null;
	}

	private URIBuilder getTermsGetUriBuilder(Department department) {
		return getTermsGetUriBuilder(department, false);
	}

	private URIBuilder getTermsGetUriBuilder(Department department, boolean isArchive) {
		String base = isArchive ? AssessmentApplication.getConfig().getAirtableServer().getArchiveBaseId()
				: AssessmentApplication.getConfig().getAirtableServer().getTermsBaseId();
		try {
			URIBuilder uriBuilder = new URIBuilder(
					String.format("%s/%s/%s", serverUrl, base, getTableName(department, lang, true)));
			uriBuilder.addParameter("sort[0][field]", DATE_FIELD);
			uriBuilder.addParameter("sort[0][direction]", "desc");
			uriBuilder.addParameter("sort[1][field]", ORDER_FIELD);
			uriBuilder.addParameter("sort[1][direction]", "asc");

			return uriBuilder;
		} catch (URISyntaxException e) {
			LOG.error("The Airtable URL provided isn't structured properly.", e);
		}
		return null;
	}

	private URIBuilder getTermsPostUriBuilder(Department department, boolean isArchive) {
		String base = isArchive ? AssessmentApplication.getConfig().getAirtableServer().getArchiveBaseId()
				: AssessmentApplication.getConfig().getAirtableServer().getTermsBaseId();
		try {
			URIBuilder uriBuilder = new URIBuilder(
					String.format("%s/%s/%s", serverUrl, base, getTableName(department, lang, true)));

			return uriBuilder;
		} catch (URISyntaxException e) {
			LOG.error("The Airtable URL provided isn't structured properly.", e);
		}
		return null;
	}

	private URIBuilder getDeptsUriBuilder() {
		try {
			URIBuilder uriBuilder = new URIBuilder(String.format("%s/%s/%s", serverUrl,
					AssessmentApplication.getConfig().getAirtableServer().getArchiveBaseId(), DEPARTMENT_TABLE));

			return uriBuilder;
		} catch (URISyntaxException e) {
			LOG.error("The Airtable URL provided isn't structured properly.", e);
		}
		return null;
	}

	private URIBuilder getUsersUriBuilder() {
		try {
			URIBuilder uriBuilder = new URIBuilder(String.format("%s/%s/%s", serverUrl,
					AssessmentApplication.getConfig().getAirtableServer().getArchiveBaseId(), USER_TABLE));

			return uriBuilder;
		} catch (URISyntaxException e) {
			LOG.error("The Airtable URL provided isn't structured properly.", e);
		}
		return null;
	}

	private URIBuilder getTablesPostUriBuilder(boolean isArchive) {
		String base = isArchive ? AssessmentApplication.getConfig().getAirtableServer().getArchiveBaseId()
				: AssessmentApplication.getConfig().getAirtableServer().getTermsBaseId();
		try {
			URIBuilder uriBuilder = new URIBuilder(
					String.format("%s/%s/%s/%s", serverUrl, API_BASES_PATH, base, API_TABLES_PATH));

			return uriBuilder;
		} catch (URISyntaxException e) {
			LOG.error("The Airtable URL provided isn't structured properly.", e);
		}
		return null;
	}

	private String getTableName(Department department, Language lang, boolean forUrl) {
		String pattern = forUrl ? "%s%%20-%%20%s" : "%s - %s";
		String name = department == null ? GLOBAL_TABLE : department.getAcronymEn();
		return String.format(pattern, name, lang.getCode().toUpperCase());
	}

	public String escapeCharacters(String text) {
		final String[] metaCharacters = { "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<",
				">", "-", "&", "%" };

		for (int i = 0; i < metaCharacters.length; i++) {
			if (text.contains(metaCharacters[i])) {
				text = text.replace(metaCharacters[i], "\\" + metaCharacters[i]);
			}
		}
		return text;
	}

	public static SearchType stringToSearchType(String type, Department department) {
		SearchType searchType;

		if (SearchType.GLOBAL.toString().equalsIgnoreCase(type)) {
			searchType = SearchType.GLOBAL;
		} else if (SearchType.GOOGLE.toString().equalsIgnoreCase(type)) {
			searchType = SearchType.GOOGLE;
		} else if (SearchType.CONTEXTUAL.toString().equalsIgnoreCase(type)) {
			searchType = SearchType.CONTEXTUAL;
		} else if (department != null && department.getAcronymEn().equalsIgnoreCase(type)) {
			searchType = SearchType.CONTEXTUAL;
		} else {
			searchType = null;
		}

		return searchType;
	}

}
