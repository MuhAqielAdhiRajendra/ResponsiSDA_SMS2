public class Evidence {
    private String name;
    private String imagePath;
    private String type;

    public Evidence(String name, String imagePath, String type) {
        this.name = name;
        this.imagePath = imagePath;
        this.type = type;
    }

    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public String getType() { return type; }
}
