package txtparsing;

import java.util.*;

public class Question {
	private int id;
	private String query;
//	private String author, title, before;
	private List<String> queries;
	
	public Question(int id, String query) {
		super();
		this.id = id;
		this.query = query;
	}
	
	public Question(int id, List<String> queries) {
		super();
		this.id = id;
		this.queries = queries;
	}
	
	public void addQuery(String query) {
		queries.add(query);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public List<String> getQueries() {
		return queries;
	}
	
	public void setQueries(List<String> queries) {
		this.queries = queries;
	}

}