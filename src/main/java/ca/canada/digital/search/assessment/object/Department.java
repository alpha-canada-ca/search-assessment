package ca.canada.digital.search.assessment.object;

import org.apache.commons.lang3.StringUtils;

import ca.canada.digital.search.assessment.AssessmentApplication;

public class Department {
	
	private String nameEn;
	private String nameFr;
	private String acronymEn;
	private String acronymFr;
	private String urlEn;
	private String urlFr;
	
	
	public String getNameEn() {
		return nameEn;
	}


	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}


	public String getNameFr() {
		return nameFr;
	}


	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}


	public String getAcronymEn() {
		return acronymEn;
	}


	public void setAcronymEn(String acronymEn) {
		this.acronymEn = acronymEn;
	}


	public String getAcronymFr() {
		return acronymFr;
	}


	public void setAcronymFr(String acronymFr) {
		this.acronymFr = acronymFr;
	}


	public String getUrlEn() {
		return urlEn;
	}


	public void setUrlEn(String urlEn) {
		this.urlEn = urlEn;
	}


	public String getUrlFr() {
		return urlFr;
	}


	public void setUrlFr(String urlFr) {
		this.urlFr = urlFr;
	}
	

	public String getSearchPage(SearchType type, Language lang) {
		if (type == SearchType.GOOGLE) {
			return AssessmentApplication.getConfig().getSearchPage().getGoogle();
		} else if (type == SearchType.CONTEXTUAL && !StringUtils.isEmpty(getUrlEn()) && !StringUtils.isEmpty(getUrlFr())) {
			if (lang == Language.FRENCH) {
				return getUrlFr();
			} else {
				return getUrlEn();
			}
		} else {
			if (lang == Language.FRENCH) {
				return AssessmentApplication.getConfig().getSearchPage().getGlobalFr();
			} else {
				return AssessmentApplication.getConfig().getSearchPage().getGlobalEn();
			}
		}
	}

}
