package txtparsing;

public class MyDoc {

	private int id;
    private String title;
    private String author;
    private String content;

    public MyDoc(int id, String title, String author, String content) {
    	this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    @Override
    public String toString() {
        String ret = "MyDoc{"
        		+ "\n\tId: " + id
                + "\n\tTitle: " + title
                + "\n\tAuthor: " + author  
                + "\n\tContent: " + content;              
        return ret + "\n}";
    }

    //---- Getters & Setters definition ----
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
