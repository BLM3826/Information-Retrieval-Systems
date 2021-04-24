package txtparsing;

/**
 *
 * @author Tonia Kyriakopoulou
 */
public class MyDoc {

    private String title;
    private String caption;
    private String mesh;

    public MyDoc(String title, String caption, String mesh) {
        this.title = title;
        this.caption = caption;
        this.mesh = mesh;
    }

    @Override
    public String toString() {
        String ret = "MyDoc{"
                + "\n\tTitle: " + title
                + "\n\tCaption: " + caption
                + "\n\tMesh: " + mesh;                
        return ret + "\n}";
    }

    //---- Getters & Setters definition ----
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMesh() {
        return mesh;
    }

    public void setMesh(String mesh) {
        this.mesh = mesh;
    }
}
