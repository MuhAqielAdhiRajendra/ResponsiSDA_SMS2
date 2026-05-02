import java.util.ArrayList;
import java.util.List;

public class CaseData {
    private String caseId;
    private String title;
    private String victim;
    private String location;
    private String date;
    private String description;

    // IMPLEMENTASI LIST
    private List<Evidence> evidences;
    private List<Witness> witnesses;

    public CaseData(String caseId, String title, String victim, String location, String date, String description) {
        this.caseId = caseId;
        this.title = title;
        this.victim = victim;
        this.location = location;
        this.date = date;
        this.description = description;
        this.evidences = new ArrayList<>();
        this.witnesses = new ArrayList<>();
    }

    public void addEvidence(Evidence e) { this.evidences.add(e); }
    public void addWitness(Witness w) { this.witnesses.add(w); }

    public String getCaseId() { return caseId; }
    public String getTitle() { return title; }
    public String getVictim() { return victim; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public List<Evidence> getEvidences() { return evidences; }
    public List<Witness> getWitnesses() { return witnesses; }
}
