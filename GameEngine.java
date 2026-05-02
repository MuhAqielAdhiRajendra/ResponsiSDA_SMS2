import java.util.*;

public class GameEngine {
    
    // Struktur Data: MAP
    private Map<String, CaseData> caseDatabase;
    
    // Status Kasus: "ACTIVE", "COMPLETE", "WRONG PERSON"
    private Map<String, String> caseStatus;
    
    // Struktur Data: QUEUE
    private Queue<CaseData> caseTemplateQueue;
    
    // Struktur Data: SET
    private Set<String> suspectRegistry;
    
    // Struktur Data: STACK
    private Stack<String> actionHistory;
    
    // Struktur Data: TREE
    private CustomTree<String> fileSystemTree;

    // Game State
    private int caseCounter = 0;
    private int wrongCount = 0;
    private int rejectCount = 0;
    private int maxRejects = 3;
    private Random random = new Random();

    // Urgent Case System
    private Queue<CaseData> urgentCaseQueue;
    private int urgentFailCount = 0;
    private int maxUrgentFails = 3;
    private boolean urgentCasesEnabled = true;
    private int urgentDifficulty = 1; // Semakin tinggi = semakin sulit (waktu lebih pendek, saksi lebih banyak)

    // Commander Chat Messages (Queue)
    private Queue<String[]> commanderMessages;

    public GameEngine() {
        caseDatabase = new HashMap<>();
        caseStatus = new HashMap<>();
        caseTemplateQueue = new LinkedList<>();
        suspectRegistry = new HashSet<>();
        actionHistory = new Stack<>();
        
        commanderMessages = new LinkedList<>();
        urgentCaseQueue = new LinkedList<>();
        
        fileSystemTree = new CustomTree<>("C:\\CORE_OS");
        
        initAllTemplates();
        initUrgentTemplates();
        initCommanderMessages();
        activateInitialCases(2);
    }

    // =========================================================
    // INISIALISASI SEMUA TEMPLATE KASUS
    // =========================================================
    private void initAllTemplates() {
        // --- TEMPLATE 1: TRAGEDI PISAU BERDARAH ---
        CaseData t1 = createTemplate(
            "Tragedi Pisau Berdarah", "Budi Santoso", "Rumah Korban, Jl. Merpati No 12", "12 Mei 2026",
            "Telah terjadi pembunuhan di rumah korban. Ditemukan pisau berlumuran darah (bercak merah) di TKP. Agen harus mencocokkan DNA darah pada pisau dan sidik jari di gagang pisau dengan para saksi."
        );
        t1.addEvidence(new Evidence("Pisau Berdarah (TKP)", "img/FotoTkp/TKP1.png", "TKP"));
        t1.addEvidence(new Evidence("DNA Darah di Pisau", "img/DNA/DNA1.png", "DNA"));
        t1.addEvidence(new Evidence("Sidik Jari di Pisau", "img/FingerPrint/Fingger1.png", "FINGERPRINT"));
        t1.addWitness(new Witness("Siti (Tetangga)", "img/orang/Cwe.png",
            "Saya masuk rumahnya karena pintu terbuka, saya tidak sengaja memegang pisau itu!",
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger1.png"));
        t1.addWitness(new Witness("Agus (Adik Korban)", "img/orang/Cwo.png",
            "Saya baru datang dan kaget melihat banyak polisi.",
            false, false, "", "img/FingerPrint/Fingger3.png"));
        t1.addWitness(new Witness("Rina (Istri Korban)", "img/orang/Cwe.png",
            "Suami saya... siapa yang tega melakukan ini? Tadi malam saya di kamar tidur...",
            true, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png"));
        t1.addWitness(new Witness("Pak RT (Ketua RT)", "img/orang/Cwo.png",
            "Saya dengar ribut-ribut malam itu tapi saya pikir cuma pertengkaran biasa.",
            false, false, "", "img/FingerPrint/Fingger4.png"));
        t1.addWitness(new Witness("Mbak Nur (Pembantu)", "img/orang/Cwe.png",
            "Saya sudah pulang jam 5 sore. Saya tidak tahu apa yang terjadi setelahnya.",
            false, false, "img/DNA/DNA2.png", ""));
        caseTemplateQueue.add(t1);

        // --- TEMPLATE 2: SIANIDA DI KAFE ---
        CaseData t2 = createTemplate(
            "Sianida di Secangkir Espresso", "Diana P.", "Kafe Senja, Kemang", "20 Juni 2026",
            "Korban kolaps di kafe. Uji lab mendeteksi Sianida. Polisi mengamankan gelas kopi dan sedotan bekas di meja sebelah."
        );
        t2.addEvidence(new Evidence("Foto TKP Kafe", "img/FotoTkp/TKP2.png", "TKP"));
        t2.addEvidence(new Evidence("DNA Sedotan", "img/DNA/DNA2.png", "DNA"));
        t2.addEvidence(new Evidence("Sidik Jari Gelas", "img/FingerPrint/Fingger4.png", "FINGERPRINT"));
        t2.addWitness(new Witness("Anton (Barista)", "img/orang/Cwo.png",
            "Saya yang membuat kopi itu, tapi saya sempat meninggalkan bar.",
            false, true, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png"));
        t2.addWitness(new Witness("Rina (Mantan Kekasih)", "img/orang/Cwe.png",
            "Dia pantas mati. Tapi bukan saya pelakunya.",
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger4.png"));
        t2.addWitness(new Witness("Andi (Pengunjung)", "img/orang/Cwo.png",
            "Saya sedang fokus kerja di laptop, tak melihat apa-apa.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger5.png"));
        t2.addWitness(new Witness("Mbak Tari (Pengunjung)", "img/orang/Cwe.png",
            "Saya duduk di meja belakang. Saya lihat korban tiba-tiba batuk-batuk hebat.",
            false, false, "", ""));
        caseTemplateQueue.add(t2);

        // --- TEMPLATE 3: MAYAT DI SUNGAI ---
        CaseData t3 = createTemplate(
            "Mayat di Sungai Ciliwung", "Hendra Wijaya", "Bantaran Sungai Ciliwung", "05 Maret 2026",
            "Jenazah ditemukan mengapung di sungai dengan bekas cekikan di leher. Ditemukan tali tambang dengan sidik jari dan helai rambut mengandung DNA asing."
        );
        t3.addEvidence(new Evidence("Foto TKP Sungai", "img/FotoTkp/TKP1.png", "TKP"));
        t3.addEvidence(new Evidence("DNA Rambut di Tali", "img/DNA/DNA2.png", "DNA"));
        t3.addEvidence(new Evidence("Sidik Jari di Tali", "img/FingerPrint/Fingger5.png", "FINGERPRINT"));
        t3.addWitness(new Witness("Dedi (Pemancing)", "img/orang/Cwo.png",
            "Saya yang menemukan mayatnya pagi itu. Saya langsung hubungi polisi.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger3.png"));
        t3.addWitness(new Witness("Yanti (Mantan Istri)", "img/orang/Cwe.png",
            "Kami sudah cerai 2 tahun. Saya tidak punya urusan lagi dengannya!",
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger6.png"));
        t3.addWitness(new Witness("Riko (Rekan Kerja)", "img/orang/Cwo.png",
            "Hendra memang punya banyak hutang. Tapi saya selalu membantunya.",
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger5.png"));
        t3.addWitness(new Witness("Mega (Pacar Baru)", "img/orang/Cwe.png",
            "Kami baru jadian sebulan. Saya sangat terpukul mendengar kabar ini.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger2.png"));
        t3.addWitness(new Witness("Pak Lurah (Tokoh Masyarakat)", "img/orang/Cwo.png",
            "Saya sudah tua, tangan saya gemetar. Petugas bilang sidik jari saya tidak terbaca.",
            false, false, "img/DNA/DNA1.png", ""));
        caseTemplateQueue.add(t3);

        // --- TEMPLATE 4: PEMBUNUHAN DI HOTEL ---
        CaseData t4 = createTemplate(
            "Kamar 404: Check-Out Terakhir", "Prof. Surya A.", "Hotel Bintang Lima", "18 Januari 2026",
            "Profesor ditemukan tewas di kamar hotel dengan luka tembak. Ditemukan selongsong peluru, sidik jari di gagang pintu kamar mandi, dan sampel DNA dari gelas wine."
        );
        t4.addEvidence(new Evidence("Foto TKP Kamar Hotel", "img/FotoTkp/TKP2.png", "TKP"));
        t4.addEvidence(new Evidence("DNA Gelas Wine", "img/DNA/DNA1.png", "DNA"));
        t4.addEvidence(new Evidence("Sidik Jari Gagang Pintu", "img/FingerPrint/Fingger2.png", "FINGERPRINT"));
        t4.addWitness(new Witness("Bella (Resepsionis)", "img/orang/Cwe.png",
            "Tamu di kamar sebelah melaporkan suara keras sekitar jam 11 malam. Saya langsung naik.",
            false, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger6.png"));
        t4.addWitness(new Witness("Fajar (Mahasiswa Bimbingan)", "img/orang/Cwo.png",
            "Profesor sudah mengancam akan mendepak saya dari program magister!",
            false, true, "img/DNA/DNA1.png", "img/FingerPrint/Fingger3.png"));
        t4.addWitness(new Witness("Dewi (Istri Profesor)", "img/orang/Cwe.png",
            "Suami saya bilang ada pertemuan penting di hotel. Saya di rumah bersama anak-anak.",
            true, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger2.png"));
        t4.addWitness(new Witness("Pak Rahmat (Satpam Hotel)", "img/orang/Cwo.png",
            "CCTV lorong kamar 404 kebetulan mati malam itu. Aneh memang.",
            false, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger4.png"));
        t4.addWitness(new Witness("Lina (Room Service)", "img/orang/Cwe.png",
            "Saya mengantarkan wine ke kamar itu jam 9 malam. Pintunya dibuka oleh seorang wanita.",
            false, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger1.png"));
        t4.addWitness(new Witness("Tamu Anonim (Kamar 403)", "img/orang/Cwo.png",
            "Saya check-in pakai nama palsu. Maaf, saya tidak mau identitas saya diketahui.",
            false, false, "", ""));
        caseTemplateQueue.add(t4);

        // --- TEMPLATE 5: RACUN DI PESTA PERNIKAHAN ---
        CaseData t5 = createTemplate(
            "Brindisi Terakhir", "Tuan Hartono", "Ballroom Grand Palace", "14 Februari 2026",
            "Tuan Hartono kolaps setelah toast pernikahan. Uji toksikologi mendeteksi Arsenik. Sidik jari dan DNA ditemukan pada botol champagne yang telah di-tamper."
        );
        t5.addEvidence(new Evidence("Foto TKP Ballroom", "img/FotoTkp/TKP1.png", "TKP"));
        t5.addEvidence(new Evidence("DNA pada Botol", "img/DNA/DNA2.png", "DNA"));
        t5.addEvidence(new Evidence("Sidik Jari Botol", "img/FingerPrint/Fingger6.png", "FINGERPRINT"));
        t5.addWitness(new Witness("Kevin (Mempelai Pria)", "img/orang/Cwo.png",
            "Ayah saya... kenapa di hari paling bahagia saya? Ini pasti kesalahan!",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png"));
        t5.addWitness(new Witness("Maya (Mempelai Wanita)", "img/orang/Cwe.png",
            "Saya tidak pernah menyentuh botol itu! Pelayannya yang menuangkan.",
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger3.png"));
        t5.addWitness(new Witness("Pak Joko (Pelayan)", "img/orang/Cwo.png",
            "Saya hanya menuangkan. Botolnya sudah disiapkan oleh pihak keluarga mempelai wanita.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger4.png"));
        t5.addWitness(new Witness("Siska (Adik Mempelai Wanita)", "img/orang/Cwe.png",
            "Bapak Hartono itu orang jahat! Dia yang memaksa kakak saya menikah demi bisnis keluarga!",
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger6.png"));
        t5.addWitness(new Witness("Budi (Wedding Organizer)", "img/orang/Cwo.png",
            "Saya melihat seorang wanita muda mendekati meja champagne sebelum acara dimulai.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger5.png"));
        caseTemplateQueue.add(t5);

        // --- TEMPLATE 6: TENGGELAM MISTERIUS ---
        CaseData t6 = createTemplate(
            "Kolam Renang Maut", "Ir. Bambang", "Kolam Renang Villa Indah", "03 Juli 2026",
            "Korban ditemukan tenggelam di kolam renang pribadi. Namun autopsi menunjukkan korban sudah pingsan sebelum masuk air. Ditemukan gelas jus di pinggir kolam."
        );
        t6.addEvidence(new Evidence("Foto TKP Kolam", "img/FotoTkp/TKP2.png", "TKP"));
        t6.addEvidence(new Evidence("DNA Gelas Jus", "img/DNA/DNA1.png", "DNA"));
        t6.addEvidence(new Evidence("Sidik Jari Gelas Jus", "img/FingerPrint/Fingger3.png", "FINGERPRINT"));
        t6.addWitness(new Witness("Ningsih (Istri Kedua)", "img/orang/Cwe.png",
            "Suami saya bilang mau berenang sore. Saya sedang di salon.",
            true, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger3.png"));
        t6.addWitness(new Witness("Dimas (Anak Kandung)", "img/orang/Cwo.png",
            "Papa sering ribut sama Mama Ningsih soal warisan...",
            false, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger5.png"));
        t6.addWitness(new Witness("Wati (Pembantu)", "img/orang/Cwe.png",
            "Saya yang membuat jus itu. Tapi Nyonya yang minta saya tinggalkan di meja.",
            false, true, "img/DNA/DNA1.png", "img/FingerPrint/Fingger6.png"));
        t6.addWitness(new Witness("Sopir Pribadi", "img/orang/Cwo.png",
            "Saya mengantar Nyonya ke salon jam 3 sore. Kami pulang jam 5.",
            false, false, "", "img/FingerPrint/Fingger2.png"));
        caseTemplateQueue.add(t6);

        // --- TEMPLATE 7: KERACUNAN DI KANTOR ---
        CaseData t7 = createTemplate(
            "Meeting Terakhir", "Direktur Wahyu", "Ruang Rapat PT. Sentosa", "22 Agustus 2026",
            "Direktur mendadak kejang saat rapat dan meninggal di rumah sakit. Botol air minum pribadinya mengandung racun tikus. Sidik jari ditemukan di tutup botol."
        );
        t7.addEvidence(new Evidence("Foto TKP Ruang Rapat", "img/FotoTkp/TKP1.png", "TKP"));
        t7.addEvidence(new Evidence("DNA Tutup Botol", "img/DNA/DNA2.png", "DNA"));
        t7.addEvidence(new Evidence("Sidik Jari Tutup Botol", "img/FingerPrint/Fingger2.png", "FINGERPRINT"));
        t7.addWitness(new Witness("Ira (Sekretaris)", "img/orang/Cwe.png",
            "Saya yang menyiapkan air minum untuk rapat. Tapi botolnya dari gudang!",
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger5.png"));
        t7.addWitness(new Witness("Hadi (Wakil Direktur)", "img/orang/Cwo.png",
            "Kalau Pak Wahyu meninggal, yang naik pangkat ya saya. Tapi saya bukan pembunuh!",
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger2.png"));
        t7.addWitness(new Witness("Sari (HRD)", "img/orang/Cwe.png",
            "Pak Wahyu baru saja memecat 3 orang minggu lalu. Banyak yang dendam.",
            false, false, "img/DNA/DNA1.png", ""));
        t7.addWitness(new Witness("OB Kantor", "img/orang/Cwo.png",
            "Saya cuma bersih-bersih. Saya tidak pegang botol siapapun.",
            false, false, "", "img/FingerPrint/Fingger4.png"));
        caseTemplateQueue.add(t7);

        // Populate Tree
        CustomTree.TreeNode<String> root = fileSystemTree.getRoot();
        for (CaseData c : caseTemplateQueue) {
            CustomTree.TreeNode<String> caseNode = new CustomTree.TreeNode<>(c.getTitle());
            root.addChild(caseNode);
            for (Witness w : c.getWitnesses()) {
                suspectRegistry.add(w.getName());
            }
        }
    }

    private CaseData createTemplate(String title, String victim, String location, String date, String desc) {
        caseCounter++;
        String id = String.format("CASE-%03d", caseCounter);
        return new CaseData(id, title, victim, location, date, desc);
    }

    // =========================================================
    // AKTIVASI KASUS (Queue -> Map)
    // =========================================================
    private void activateInitialCases(int count) {
        for (int i = 0; i < count && !caseTemplateQueue.isEmpty(); i++) {
            CaseData c = caseTemplateQueue.poll();
            caseDatabase.put(c.getCaseId(), c);
            caseStatus.put(c.getCaseId(), "ACTIVE");
        }
    }

    // Generate kasus baru dari Queue
    public CaseData generateNewCase() {
        if (caseTemplateQueue.isEmpty()) {
            // Recycle: re-shuffle existing templates (infinite play)
            return null; 
        }
        CaseData c = caseTemplateQueue.poll();
        caseDatabase.put(c.getCaseId(), c);
        caseStatus.put(c.getCaseId(), "ACTIVE");
        logAction("Kasus baru masuk: " + c.getCaseId() + " - " + c.getTitle());
        return c;
    }

    // =========================================================
    // GAME STATE MANAGEMENT
    // =========================================================
    public void addWrongAnswer() {
        wrongCount++;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public boolean isGameOver() {
        // Game over jika semua kasus aktif sudah WRONG dan tidak ada yang COMPLETE
        int activeCount = 0;
        for (String status : caseStatus.values()) {
            if (status.equals("ACTIVE")) activeCount++;
        }
        return activeCount == 0 && wrongCount > 0 && !hasAnySolved();
    }

    private boolean hasAnySolved() {
        for (String status : caseStatus.values()) {
            if (status.equals("COMPLETE")) return true;
        }
        return false;
    }

    public boolean allCasesResolved() {
        for (String status : caseStatus.values()) {
            if (status.equals("ACTIVE")) return false;
        }
        return true;
    }

    // =========================================================
    // URGENT CASE SYSTEM
    // =========================================================
    private void initUrgentTemplates() {
        // Template kasus urgensi tinggi
        CaseData u1 = createTemplate(
            "[URGENT] Penembakan di Pasar Malam", "Warga Sipil", "Pasar Malam Kota", "Hari Ini",
            "DARURAT! Ada penembakan di pasar malam. Pelaku masih di area! Identifikasi dari sidik jari pada senjata yang ditemukan."
        );
        u1.addEvidence(new Evidence("Pistol TKP", "img/FotoTkp/TKP1.png", "TKP"));
        u1.addEvidence(new Evidence("DNA Pada Pistol", "img/DNA/DNA1.png", "DNA"));
        u1.addEvidence(new Evidence("Sidik Jari Pistol", "img/FingerPrint/Fingger2.png", "FINGERPRINT"));
        u1.addWitness(new Witness("Pedagang Sate", "img/orang/Cwo.png",
            "Ada orang berlari setelah suara tembakan! Tingginya sekitar 170cm.",
            false, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger4.png"));
        u1.addWitness(new Witness("Ibu Hamil (Saksi Mata)", "img/orang/Cwe.png",
            "Saya lihat dia! Dia pakai jaket hitam dan lari ke arah parkiran!",
            false, true, "img/DNA/DNA1.png", "img/FingerPrint/Fingger5.png"));
        u1.addWitness(new Witness("Preman Pasar", "img/orang/Cwo.png",
            "Bukan urusan gue! Gue cuma nongkrong di sini!",
            true, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger2.png"));
        urgentCaseQueue.add(u1);

        CaseData u2 = createTemplate(
            "[URGENT] Perampokan Bank BRI", "Security Bank", "Bank BRI Cabang Utama", "Hari Ini",
            "DARURAT! Perampok meninggalkan masker di TKP. DNA dan sidik jari ditemukan. Pelaku masih dalam pelarian!"
        );
        u2.addEvidence(new Evidence("Masker Pelaku", "img/FotoTkp/TKP2.png", "TKP"));
        u2.addEvidence(new Evidence("DNA pada Masker", "img/DNA/DNA2.png", "DNA"));
        u2.addEvidence(new Evidence("Sidik Jari Konter", "img/FingerPrint/Fingger4.png", "FINGERPRINT"));
        u2.addWitness(new Witness("Teller Bank", "img/orang/Cwe.png",
            "Dia menodongkan senjata ke saya! Suaranya berat dan dia kidal.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png"));
        u2.addWitness(new Witness("Nasabah Tua", "img/orang/Cwo.png",
            "Saya jatuh saat dia mendorong saya. Dia mencekik security!",
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger6.png"));
        u2.addWitness(new Witness("Ojol di Parkiran", "img/orang/Cwo.png",
            "Ada orang buru-buru naik motor tanpa helm. Platnya BK 1234 AB.",
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger4.png"));
        urgentCaseQueue.add(u2);

        CaseData u3 = createTemplate(
            "[URGENT] Ledakan di Gudang", "Penjaga Gudang", "Gudang Pelabuhan Tanjung Priok", "Hari Ini",
            "DARURAT! Ledakan di gudang pelabuhan. Sisa bahan peledak ditemukan. Ada jejak tangan di detonator."
        );
        u3.addEvidence(new Evidence("Detonator TKP", "img/FotoTkp/TKP1.png", "TKP"));
        u3.addEvidence(new Evidence("DNA di Kabel Detonator", "img/DNA/DNA1.png", "DNA"));
        u3.addEvidence(new Evidence("Sidik Jari Detonator", "img/FingerPrint/Fingger5.png", "FINGERPRINT"));
        u3.addWitness(new Witness("Buruh Pelabuhan", "img/orang/Cwo.png",
            "Ada orang asing masuk gudang tadi pagi. Saya pikir dia petugas.",
            false, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger3.png"));
        u3.addWitness(new Witness("Sopir Truk", "img/orang/Cwo.png",
            "Saya antar barang tadi siang. Ada bau aneh dari gudang sebelah.",
            true, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger5.png"));
        u3.addWitness(new Witness("Wanita Misterius", "img/orang/Cwe.png",
            "Saya hanya lewat. Kenapa saya ditanya-tanya?",
            false, true, "img/DNA/DNA1.png", "img/FingerPrint/Fingger1.png"));
        urgentCaseQueue.add(u3);

        CaseData u4 = createTemplate(
            "[URGENT] Penculikan Anak Pejabat", "Anak Walikota", "Sekolah Internasional", "Hari Ini",
            "DARURAT! Anak walikota diculik dari sekolah. Van penculik ditemukan ditinggalkan. DNA rambut dan sidik jari di kursi penumpang."
        );
        u4.addEvidence(new Evidence("Van Penculik", "img/FotoTkp/TKP2.png", "TKP"));
        u4.addEvidence(new Evidence("DNA Rambut di Van", "img/DNA/DNA2.png", "DNA"));
        u4.addEvidence(new Evidence("Sidik Jari Stir Van", "img/FingerPrint/Fingger6.png", "FINGERPRINT"));
        u4.addWitness(new Witness("Guru Sekolah", "img/orang/Cwe.png",
            "Ada pria berpakaian seragam mengaku sopir baru. Saya izinkan masuk.",
            false, false, "img/DNA/DNA1.png", "img/FingerPrint/Fingger3.png"));
        u4.addWitness(new Witness("Tukang Kebun", "img/orang/Cwo.png",
            "Van putih itu parkir di belakang. Saya lihat 2 orang di dalamnya.",
            false, true, "img/DNA/DNA2.png", "img/FingerPrint/Fingger1.png"));
        u4.addWitness(new Witness("Sopir Palsu", "img/orang/Cwo.png",
            "Saya dibayar orang untuk jemput anak itu. Saya tidak tahu ini penculikan!",
            true, false, "img/DNA/DNA2.png", "img/FingerPrint/Fingger6.png"));
        u4.addWitness(new Witness("CCTV Operator", "img/orang/Cwe.png",
            "Rekaman CCTV terhapus dari jam 10-12 siang. Ada yang hack sistem.",
            false, false, "", ""));
        urgentCaseQueue.add(u4);
    }

    // Generate kasus urgent dengan time limit
    public CaseData generateUrgentCase() {
        if (!urgentCasesEnabled || urgentCaseQueue.isEmpty()) return null;

        CaseData c = urgentCaseQueue.poll();
        c.setUrgent(true);

        // Difficulty scaling: semakin tinggi difficulty, semakin pendek waktunya
        int baseTime;
        switch (urgentDifficulty) {
            case 1: baseTime = 300; break;  // 5 menit
            case 2: baseTime = 240; break;  // 4 menit
            case 3: baseTime = 180; break;  // 3 menit
            case 4: baseTime = 120; break;  // 2 menit
            default: baseTime = 90; break;  // 1.5 menit (extreme)
        }
        // Random variation: +/- 30 detik
        int timeLimit = baseTime + random.nextInt(61) - 30;
        c.setTimeLimitSeconds(Math.max(60, timeLimit)); // Minimal 60 detik

        caseDatabase.put(c.getCaseId(), c);
        caseStatus.put(c.getCaseId(), "ACTIVE");
        logAction("[URGENT] Kasus darurat masuk: " + c.getCaseId() + " | Batas waktu: " + c.getTimeLimitSeconds() + " detik");
        return c;
    }

    public void addUrgentFail() {
        urgentFailCount++;
        logAction("Kasus urgent GAGAL! (Gagal ke-" + urgentFailCount + "/" + maxUrgentFails + ")");
        if (urgentFailCount >= maxUrgentFails) {
            urgentCasesEnabled = false;
            logAction("Komandan kecewa. Kasus urgent tidak akan diberikan lagi.");
        }
    }

    public void urgentCaseSuccess() {
        urgentDifficulty++;
        logAction("Kasus urgent berhasil! Difficulty naik ke level " + urgentDifficulty);
    }

    public int getUrgentFailCount() { return urgentFailCount; }
    public int getMaxUrgentFails() { return maxUrgentFails; }
    public boolean isUrgentCasesEnabled() { return urgentCasesEnabled; }
    public int getUrgentDifficulty() { return urgentDifficulty; }

    // =========================================================
    // GETTERS & UTILITY
    // =========================================================
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

    // =========================================================
    // ANALISIS AI (Binary Tree)
    // =========================================================
    public AnalysisResult analyzeSuspect(String caseId, String suspectName) {
        CaseData targetCase = caseDatabase.get(caseId);
        if (targetCase == null) return new AnalysisResult("Kasus " + caseId + " tidak ditemukan.", 0, false);
        
        logAction("Menganalisis tersangka '" + suspectName + "' pada " + caseId);

        // Struktur Data: BINARY TREE
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
                if (w.getSuspectDnaImage() != null && !w.getSuspectDnaImage().isEmpty() 
                    && w.getSuspectDnaImage().equals(tkpDna)) score += 50;
                if (w.getSuspectFingerprintImage() != null && !w.getSuspectFingerprintImage().isEmpty()
                    && w.getSuspectFingerprintImage().equals(tkpFingerprint)) score += 40;
                if (w.isRedHerring()) score += 20; 
                
                bst.insert(w, score);
            }
        }

        if (targetWitness == null) {
            return new AnalysisResult("Individu '" + suspectName + "' tidak ditemukan dalam daftar saksi/tersangka kasus ini.", 0, false);
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

        boolean confirmed = targetWitness.isCulprit();
        return new AnalysisResult(msg, finalScore, confirmed);
    }
    
    // Menandai status kasus
    public void setCaseStatus(String caseId, String status) {
        caseStatus.put(caseId, status);
    }
    
    public String getCaseStatus(String caseId) {
        return caseStatus.getOrDefault(caseId, "ACTIVE");
    }

    // =========================================================
    // COMMANDER CHAT SYSTEM
    // =========================================================
    private void initCommanderMessages() {
        commanderMessages.add(new String[]{"Misi Tambahan: Penyelidikan Ulang", 
            "Agen, ada laporan baru dari forensik. Salah satu kasus lama perlu ditinjau ulang. Segera periksa ulang barang bukti!"});
        commanderMessages.add(new String[]{"Perintah: Analisis Mendalam", 
            "Agen, kami menemukan saksi baru yang bersedia bersaksi. Pergi ke lapangan dan kumpulkan keterangan tambahan."});
        commanderMessages.add(new String[]{"Tugas Darurat: Kasus Prioritas", 
            "Agen, kasus baru dengan tingkat urgensi tinggi telah masuk. Korban adalah pejabat publik. Tangani segera!"});
        commanderMessages.add(new String[]{"Perintah: Verifikasi DNA", 
            "Lab forensik menemukan kecocokan DNA parsial. Lakukan analisis ulang terhadap semua tersangka."});
        commanderMessages.add(new String[]{"Misi: Pengawasan Tersangka", 
            "Salah satu tersangka terlihat berusaha kabur. Lakukan pengawasan ketat dan laporkan pergerakannya."});
        commanderMessages.add(new String[]{"Perintah Langsung", 
            "Agen, kinerja divisimu sedang diawasi oleh pusat. Pastikan semua kasus diselesaikan dengan benar."});
    }

    public String[] getNextCommanderMessage() {
        if (commanderMessages.isEmpty()) return null;
        return commanderMessages.poll();
    }

    public boolean hasCommanderMessages() {
        return !commanderMessages.isEmpty();
    }

    public void addRejectCount() {
        rejectCount++;
        logAction("Menolak tugas dari Komandan. (Tolak ke-" + rejectCount + ")");
    }

    public int getRejectCount() {
        return rejectCount;
    }

    public boolean isFiredForInsubordination() {
        return rejectCount >= maxRejects;
    }

    public void acceptMission() {
        logAction("Menerima tugas tambahan dari Komandan.");
        // Reward: generate kasus baru
        generateNewCase();
    }

    public static class AnalysisResult {
        public String message;
        public int score;
        public boolean isCulprit;
        public AnalysisResult(String m, int s, boolean c) { this.message = m; this.score = s; this.isCulprit = c; }
    }
}
