package txtparsing;

public class DocSimilarity implements Comparable<DocSimilarity>{
	private int id;
	private double similarity;
	
	public DocSimilarity(int id, double similarity) {
		this.id = id;
		this.similarity = similarity;
	}
	
	public int getId() {
		return id;
	}
	
	public double getSimilarity() {
		return similarity;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	
	@Override
    public int compareTo(DocSimilarity b) {
        return (int)(b.getSimilarity() - this.getSimilarity()); //Descending order
    }

}
