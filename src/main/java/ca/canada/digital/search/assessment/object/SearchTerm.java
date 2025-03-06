package ca.canada.digital.search.assessment.object;

import java.util.Date;

public class SearchTerm {
	
	private int rank;
	private String term;
	private long count;
	private String targetUrl;
	private Date lastOccurance;
	
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public String getTargetUrl() {
		return targetUrl;
	}
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	public Date getLastOccurance() {
		return lastOccurance;
	}
	public void setLastOccurance(Date lastOccurance) {
		this.lastOccurance = lastOccurance;
	}
	
}
