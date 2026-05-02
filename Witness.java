public class Witness {
    private String name;
    private String photoPath;
    private String testimony;
    private boolean isCulprit;
    private boolean isRedHerring;
    private String suspectDnaImage; 
    private String suspectFingerprintImage; 

    public Witness(String name, String photoPath, String testimony, boolean isCulprit, boolean isRedHerring, String dna, String fingerprint) {
        this.name = name;
        this.photoPath = photoPath;
        this.testimony = testimony;
        this.isCulprit = isCulprit;
        this.isRedHerring = isRedHerring;
        this.suspectDnaImage = dna;
        this.suspectFingerprintImage = fingerprint;
    }

    public String getName() { return name; }
    public String getPhotoPath() { return photoPath; }
    public String getTestimony() { return testimony; }
    public boolean isCulprit() { return isCulprit; }
    public boolean isRedHerring() { return isRedHerring; }
    public String getSuspectDnaImage() { return suspectDnaImage; }
    public String getSuspectFingerprintImage() { return suspectFingerprintImage; }
}
