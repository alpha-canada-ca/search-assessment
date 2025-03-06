package ca.canada.digital.search.assessment.io;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ca.canada.digital.search.assessment.object.Department;
import ca.canada.digital.search.assessment.object.SearchTerm;
import ca.canada.digital.search.assessment.object.SearchType;
import ca.canada.digital.search.assessment.object.TermEvaluation;

public interface DataTable {
	
	List<Department> getDepartments();
	
	boolean addDepartment(Department department);
	
	boolean isAvailable(Department department);
	
	Date getLastDate(Department department);
	
	List<Date> getAllDates(Department department);
	
	List<Date> getAllArchivedDates(Department department);
	
	Date getLastArchivedDate(Department department);
	
	Boolean isDateArchived(Date date, Department department);
		
	List<SearchTerm> getSearchTerms(Date month, Department department);
		
	boolean archiveEvaluatedTerms(List<TermEvaluation> terms, Department department);
	
	Map<SearchType, List<TermEvaluation>> getArchivedEvaluatedTerms(Date date, Department department);
	
	List<TermEvaluation> getArchivedEvaluatedTerms(String targetUrl, Department department);
		
	boolean createTable(Department department, boolean isArchive);
	
	boolean validateUser(String username, String password);

}
