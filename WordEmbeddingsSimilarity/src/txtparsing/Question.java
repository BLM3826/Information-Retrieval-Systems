package txtparsing;

public class Question {
	private int id;
	private String query;
	
	public Question(int id, String query) {
		super();
		this.id = id;
		this.query = query;
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
	
}
