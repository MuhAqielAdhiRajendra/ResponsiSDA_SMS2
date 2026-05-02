import java.util.*;

public class GameEngine {
    
    // 1. MAP: Database Kasus
    private Map<String, CaseData> caseDatabase;
    
    // 2. QUEUE: Antrean Kasus (Incoming)
    private Queue<CaseData> incomingCases;
    
    // 3. SET: Registry Tersangka Unik
    private Set<String> suspectRegistry;
    
    // 4. STACK: Action History
    private Stack<String> actionHistory;
    
    // 5. TREE: File System 
    private CustomTree<String> fileSystemTree;

    public GameEngine() {
        caseDatabase = new HashMap<>();
        incomingCases = new LinkedList<>();
        suspectRegistry = new HashSet<>();
        actionHistory = new Stack<>();
        
        fileSystemTree = new CustomTree<>("C:\\CORE_OS");
        
        initMurderCases();
        activateAllCasesForDemo();
    }

    private void initMurderCases() {
        // --- KASUS 1: TRAGEDI PISAU BERDARAH ---
        CaseData case1 = new CaseData(
            "CASE-001", "Tragedi Pisau Berdarah", "Budi Santoso", "Rumah Korban", "12 Mei 2026", 
            "Telah terjadi pembunuhan di rumah korban. Ditemukan pisau berlumuran darah (bercak merah) di TKP. Agen harus mencocokkan DNA darah pada pisau dan sidik jari di gagang pisau dengan para saksi."
        );
        case1.addEvidence(new Evidence("Pisau Berdarah (TKP)", "img/FotoTkp/TKP1.png", "TKP"));
        case1.addEvidence(new Evidence("DNA Darah di Pisau", "img/DNA/DNA1.png", "DNA"));
        case1.addEvidence(new Evidence("Sidik Jari di Pisau", "img/FingerPrint/Fingger1.png", "FINGERPRINT"));
        
        // 1. Saksi Red Herring (Cewek yang memegang pisau)
        case1.addWitness(new Witness("Siti (Tetangga)", "img/orang/Cwe.png", 
            "Saya masuk rumahnya karena pintu terbuka, saya tidak sengaja memegang pisau itu! Bercak merah itu mungkin darah, tapi SAYA BUKAN PELAKUNYA!", 
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger1.png")); // Sidik jari cocok (karena memegang), tapi DNA darah BEDA
            
        // 2. Saksi Biasa
        case1.addWitness(new Witness("Agus (Adik Korban)", "img/orang/Cwo.png", 
            "Saya baru datang dan kaget melihat banyak polisi.", 
            false, false, "img/DNA/DNA3.png", "img/FingerPrint/Fingger3.png")); 
            
        // 3. PELAKU ASLI (Cewek yang memiliki DNA yang sama dengan darah di TKP dan sidik jarinya juga ada)
        case1.addWitness(new Witness("Rina (Istri Korban)", "img/orang/Cwe.png", 
            "Suami saya... siapa yang tega melakukan ini? Tadi malam saya di kamar tidur...", 
            true, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png")); // DNA Darah MATCH, Sidik Jari MATCH

        incomingCases.add(case1);

        // --- KASUS 2: RACUN DI KAFE ---
        CaseData case2 = new CaseData(
            "CASE-002", "Sianida di Secangkir Espresso", "Diana P.", "Kafe Senja", "20 Juni 2026", 
            "Korban kolaps di kafe. Uji lab mendeteksi Sianida. Polisi mengamankan gelas kopi dan sedotan bekas di meja sebelah."
        );
        case2.addEvidence(new Evidence("Foto TKP Kafe", "img/FotoTkp/TKP2.png", "TKP"));
        case2.addEvidence(new Evidence("DNA Sedotan", "img/DNA/DNA2.png", "DNA"));
        case2.addEvidence(new Evidence("Sidik Jari Gelas", "img/FingerPrint/Fingger4.png", "FINGERPRINT"));
        
        case2.addWitness(new Witness("Anton (Barista)", "img/orang/Cwo.png", 
            "Saya yang membuat kopi itu, tapi saya sempat meninggalkan bar.", 
            false, true, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png")); // Red Herring
            
        case2.addWitness(new Witness("Rina (Mantan Kekasih)", "img/orang/Cwe.png", 
            "Dia pantas mati. Tapi bukan saya pelakunya.", 
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger4.png")); // Pelaku
            
        case2.addWitness(new Witness("Andi (Pengunjung)", "img/orang/Cwo.png", 
            "Saya sedang fokus kerja di laptop, tak melihat apa-apa.", 
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger5.png"));
            
        incomingCases.add(case2);
        
        // Populate Tree & Set
        CustomTree.TreeNode<String> root = fileSystemTree.getRoot();
        for (CaseData c : incomingCases) {
            CustomTree.TreeNode<String> caseNode = new CustomTree.TreeNode<>(c.getCaseId());
            root.addChild(caseNode);
            
            for (Witness w : c.getWitnesses()) {
                suspectRegistry.add(w.getName());
            }
        }
    }

    private void activateAllCasesForDemo() {
        while (!incomingCases.isEmpty()) {
            CaseData c = incomingCases.poll();
            caseDatabase.put(c.getCaseId(), c);
        }
    }

    public Map<String, CaseData> getActiveCases() {
        return caseDatabase;
    }

    public void logAction(String action) {
        actionHistory.push(action);
        System.out.println("[ACTION LOG]: " + action);
    }
    
    public String getLastAction() {
        return actionHistory.isEmpty() ? "Tidak ada aktivitas." : actionHistory.peek();
    }

    // Fungsi Analisis AI menggunakan Binary Tree
    public AnalysisResult analyzeSuspect(String caseId, String suspectName) {
        CaseData targetCase = caseDatabase.get(caseId);
        if (targetCase == null) return new AnalysisResult("Kasus " + caseId + " tidak ditemukan.", 0);
        
        logAction("Menganalisis tersangka '" + suspectName + "' pada " + caseId);

        // 6. BINARY TREE: Bangun pohon probabilitas untuk saksi/tersangka yang dicari
        CustomBinaryTree bst = new CustomBinaryTree();
        
        String tkpDna = "";
        String tkpFingerprint = "";
        for (Evidence e : targetCase.getEvidences()) {
            if (e.getType().equals("DNA")) tkpDna = e.getImagePath();
            if (e.getType().equals("FINGERPRINT")) tkpFingerprint = e.getImagePath();
        }

        Witness targetWitness = null;

        for (Witness w : targetCase.getWitnesses()) {
            if (w.getName().toLowerCase().contains(suspectName.toLowerCase())) {
                targetWitness = w;
                
                int score = 0;
                if (w.getSuspectDnaImage().equals(tkpDna)) score += 50;
                if (w.getSuspectFingerprintImage().equals(tkpFingerprint)) score += 40;
                if (w.isRedHerring()) score += 20; 
                
                bst.insert(w, score);
            }
        }

        if (targetWitness == null) {
            return new AnalysisResult("Individu '" + suspectName + "' tidak ditemukan dalam daftar saksi/tersangka kasus ini.", 0);
        }

        int finalScore = bst.getHighestScore();
        
        String msg = ">> Menganalisis Biometrik & Alibi...\n";
        msg += ">> Tingkat Kecocokan " + targetWitness.getName() + " : " + finalScore + "%\n\n";
        
        if (finalScore >= 90) {
            msg += "[WARNING] BUKTI SANGAT KUAT!\n";
            msg += "DNA dan Sidik Jari identik dengan TKP. Pelaku telah dikonfirmasi!";
        } else if (finalScore >= 20 && finalScore < 90) {
            msg += "[INFO] MENCURIGAKAN.\n";
            msg += "Ada perilaku aneh (Red Herring) atau kecocokan parsial. Tetap awasi pergerakannya.";
        } else {
            msg += "[CLEAR] KEMUNGKINAN KECIL.\n";
            msg += "Tidak ada kecocokan bukti fisik. Kemungkinan besar bukan pelaku.";
        }

        return new AnalysisResult(msg, finalScore);
    }
    
    public static class AnalysisResult {
        public String message;
        public int score;
        public AnalysisResult(String m, int s) { this.message = m; this.score = s; }
    }
}
