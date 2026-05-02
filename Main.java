import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    // --- STRUKTUR DATA (5 POIN: MAP) ---
    private GameEngine gameEngine;

    // --- DEKLARASI JENDELA APLIKASI ---
    private AnchorPane desktop;
    private VBox explorerWindow;
    private VBox databaseWindow; // Jendela baru untuk pencarian pelaku
    private Image folderDisplayImg; // Cache gambar agar hemat memori
    private Image fileDisplayImg;   // Cache gambar File

    @Override
    public void start(Stage primaryStage) {
        // --- INISIALISASI GAME ENGINE (Data Structures Logic) ---
        gameEngine = new GameEngine();

        // =========================================================
        // LAYER 1: DESKTOP KANVAS (Tempat Jendela Ditaruh)
        // =========================================================
        desktop = new AnchorPane();
        desktop.getStyleClass().add("desktop");

        // Load gambar sekali saja agar hemat memori & rendering cepat
        try {
            File fileFolder = new File("img/FolderDisplay.png");
            if (fileFolder.exists()) {
                folderDisplayImg = new Image(fileFolder.toURI().toString());
            }
            File fileData = new File("img/File.png");
            if (fileData.exists()) {
                fileDisplayImg = new Image(fileData.toURI().toString());
            }
        } catch (Exception e) {
            System.out.println("Gagal load gambar background");
        }

        // --- IKON DESKTOP ---
        VBox folderBox = createIcon("Folder Kasus", "img/Folder.png");
        AnchorPane.setTopAnchor(folderBox, 30.0);
        AnchorPane.setLeftAnchor(folderBox, 30.0);

        VBox caseBox = createIcon("Database", "img/Case.png");
        AnchorPane.setTopAnchor(caseBox, 140.0);
        AnchorPane.setLeftAnchor(caseBox, 30.0);


        // =========================================================
        // JENDELA 1: EXPLORER (Daftar Kasus Aktif)
        // =========================================================
        explorerWindow = new VBox(0);
        explorerWindow.setPrefSize(450, 350);
        explorerWindow.getStyleClass().add("explorer-window");
        explorerWindow.setVisible(false); // Sembunyikan awal

        // Header Explorer (Ini yang bakal jadi area pegangan buat nge-drag)
        HBox expHeader = new HBox();
        expHeader.getStyleClass().add("exp-header");
        
        Label expTitle = new Label("📁 DAFTAR KASUS AKTIF");
        expTitle.getStyleClass().add("exp-title");
        
        Region expSpacer = new Region();
        HBox.setHgrow(expSpacer, Priority.ALWAYS);
        
        Button expCloseBtn = new Button("✕");
        expCloseBtn.getStyleClass().add("exp-close-btn");
        expCloseBtn.setOnAction(e -> explorerWindow.setVisible(false));
        
        expHeader.getChildren().addAll(expTitle, expSpacer, expCloseBtn);

        // Isi Explorer
        VBox caseListContainer = new VBox(10);
        caseListContainer.setPadding(new Insets(15));
        caseListContainer.getStyleClass().add("case-list-container");
        
        for (String caseId : gameEngine.getActiveCases().keySet()) {
            CaseData cData = gameEngine.getActiveCases().get(caseId);
            Button btnCase = new Button("📄 " + caseId + " - " + cData.getTitle());
            btnCase.setMaxWidth(Double.MAX_VALUE);
            btnCase.setAlignment(Pos.CENTER_LEFT);
            btnCase.getStyleClass().add("btn-case");
            
            // Klik item untuk buka FolderDisplay
            btnCase.setOnAction(e -> openCaseDetail(caseId));
            
            caseListContainer.getChildren().add(btnCase);
        }
        
        ScrollPane scrollPane = new ScrollPane(caseListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("exp-scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        explorerWindow.getChildren().addAll(expHeader, scrollPane);

        // Posisi awal muncul di tengah
        explorerWindow.setLayoutX(150);
        explorerWindow.setLayoutY(120);


        // Jadikan Explorer bisa digeser (Drag pegang Header-nya)
        makeDraggable(expHeader, explorerWindow, desktop);

        // =========================================================
        // JENDELA 2: DATABASE ANALISIS & PELAPORAN
        // =========================================================
        databaseWindow = new VBox(0);
        databaseWindow.setPrefSize(450, 420);
        databaseWindow.getStyleClass().add("explorer-window");
        databaseWindow.setVisible(false);

        HBox dbHeader = new HBox();
        dbHeader.getStyleClass().add("exp-header");
        
        Label dbTitle = new Label("⚙ SISTEM PELAPORAN & ANALISIS");
        dbTitle.getStyleClass().add("exp-title");
        
        Region dbSpacer = new Region();
        HBox.setHgrow(dbSpacer, Priority.ALWAYS);
        
        Button dbCloseBtn = new Button("✕");
        dbCloseBtn.getStyleClass().add("exp-close-btn");
        dbCloseBtn.setOnAction(e -> databaseWindow.setVisible(false));
        
        dbHeader.getChildren().addAll(dbTitle, dbSpacer, dbCloseBtn);

        VBox dbContent = new VBox(15);
        dbContent.setPadding(new Insets(20));
        
        Label lblDbDesc = new Label("Sistem Pencocokan Biometrik & Pelaporan Kejanggalan CORE. Masukkan parameter di bawah ini:");
        lblDbDesc.setWrapText(true);
        lblDbDesc.setStyle("-fx-text-fill: #c0c8d4; -fx-font-size: 13px;");

        TextField txtTargetCase = new TextField();
        txtTargetCase.setPromptText("ID Kasus (contoh: CASE-001)");
        txtTargetCase.getStyleClass().add("search-bar");

        TextField txtSuspect = new TextField();
        txtSuspect.setPromptText("Nama Tersangka / Ciri-ciri Fisik");
        txtSuspect.getStyleClass().add("search-bar");

        TextField txtAnomaly = new TextField();
        txtAnomaly.setPromptText("Laporan Kejanggalan / Bukti Baru");
        txtAnomaly.getStyleClass().add("search-bar");

        Button btnAnalyze = new Button("🔍 MULAI ANALISIS OTOMATIS");
        btnAnalyze.getStyleClass().add("btn-see-detail");
        btnAnalyze.setMaxWidth(Double.MAX_VALUE);

        Label lblResult = new Label("");
        lblResult.setWrapText(true);
        lblResult.setStyle("-fx-text-fill: #4da6ff; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");

        btnAnalyze.setOnAction(e -> {
            String suspect = txtSuspect.getText().isEmpty() ? "Unknown" : txtSuspect.getText();
            String caseT = txtTargetCase.getText().isEmpty() ? "KASUS" : txtTargetCase.getText();
            
            GameEngine.AnalysisResult result = gameEngine.analyzeSuspect(caseT, suspect);
            lblResult.setText(result.message);
        });

        dbContent.getChildren().addAll(lblDbDesc, txtTargetCase, txtSuspect, txtAnomaly, btnAnalyze, lblResult);
        databaseWindow.getChildren().addAll(dbHeader, dbContent);

        databaseWindow.setLayoutX(500);
        databaseWindow.setLayoutY(100);
        makeDraggable(dbHeader, databaseWindow, desktop);

        // =========================================================
        // GABUNGKAN KE DESKTOP & AKTIFKAN FITUR DRAG (GESER)
        // =========================================================
        desktop.getChildren().addAll(folderBox, caseBox, explorerWindow, databaseWindow);


        // =========================================================
        // TASKBAR BAWAH
        // =========================================================
        HBox taskbar = new HBox(15);
        taskbar.getStyleClass().add("taskbar");
        taskbar.setAlignment(Pos.CENTER_LEFT);

        Label startBtn = new Label("⊞");
        startBtn.getStyleClass().add("start-btn");
        
        TextField searchBar = new TextField();
        searchBar.setPromptText("Search Digital DNA OS...");
        searchBar.getStyleClass().add("search-bar");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm\ndd/MM/yyyy");
        Label clock = new Label(dtf.format(LocalDateTime.now()));
        clock.getStyleClass().add("clock");
        clock.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);

        taskbar.getChildren().addAll(startBtn, searchBar, spacer, clock);

        // --- LOGIKA KLIK DESKTOP ---
        // Folder Kasus -> Buka Explorer
        folderBox.setOnMouseClicked(e -> {
            explorerWindow.setVisible(true);
            explorerWindow.toFront();
        });
        
        // Database -> Buka Sistem Pelaporan & Analisis
        caseBox.setOnMouseClicked(e -> {
            databaseWindow.setVisible(true);
            databaseWindow.toFront(); 
        });


        // ROOT LAYER
        VBox mainRoot = new VBox();
        VBox.setVgrow(desktop, Priority.ALWAYS);
        mainRoot.getChildren().addAll(desktop, taskbar);

        Scene scene = new Scene(mainRoot, 1024, 720);
        try {
            File cssFile = new File("style.css");
            scene.getStylesheets().add(cssFile.toURI().toString());
        } catch (Exception e) {
            System.out.println("Gagal load style.css");
        }
        
        primaryStage.setTitle("CORE - Digital DNA OS Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- FUNGSI MENGISI DATA KASUS KE FOLDERDISPLAY ---
    private void openCaseDetail(String caseId) {
        CaseData data = gameEngine.getActiveCases().get(caseId);
        
        AnchorPane detailWindow = new AnchorPane();
        detailWindow.setMaxSize(622, 377);
        detailWindow.setMinSize(622, 377);
        detailWindow.getStyleClass().add("detail-window");
        
        // Gambar Background Folder (menggunakan cache)
        if (folderDisplayImg != null) {
            ImageView bgFolder = new ImageView(folderDisplayImg);
            bgFolder.setFitWidth(622);
            bgFolder.setFitHeight(377);
            detailWindow.getChildren().add(bgFolder);
        } else {
            Pane fallbackBg = new Pane();
            fallbackBg.setPrefSize(622, 377);
            fallbackBg.getStyleClass().add("detail-fallback-bg");
            detailWindow.getChildren().add(fallbackBg);
        }

        // Teks Detail
        VBox textDataBox = new VBox(15);
        textDataBox.getStyleClass().add("text-data-box");
        
        Label lblCaseData = new Label("Case   : " + data.getTitle());
        Label lblKorbanData = new Label("Korban : " + data.getVictim());
        Label lblLokasiData = new Label("Lokasi : " + data.getLocation());
        
        lblCaseData.getStyleClass().add("detail-text");
        lblKorbanData.getStyleClass().add("detail-text");
        lblLokasiData.getStyleClass().add("detail-text");

        textDataBox.getChildren().addAll(lblCaseData, lblKorbanData, lblLokasiData);
        AnchorPane.setTopAnchor(textDataBox, 100.0); 
        AnchorPane.setLeftAnchor(textDataBox, 50.0);

        // Tombol Close Detail
        Button closeDetailBtn = new Button("✕");
        closeDetailBtn.getStyleClass().add("detail-close-btn");
        closeDetailBtn.setOnAction(e -> desktop.getChildren().remove(detailWindow)); 
        AnchorPane.setTopAnchor(closeDetailBtn, 15.0); 
        AnchorPane.setRightAnchor(closeDetailBtn, 20.0);

        // Tombol See Detail
        Button btnSeeDetail = new Button("📄 See Detail");
        btnSeeDetail.getStyleClass().add("btn-see-detail");
        btnSeeDetail.setOnAction(e -> openFullDetail(caseId));
        AnchorPane.setBottomAnchor(btnSeeDetail, 25.0);
        AnchorPane.setRightAnchor(btnSeeDetail, 25.0);

        // Tombol Wawancara
        Button btnInterview = new Button("🎙 Wawancara");
        btnInterview.getStyleClass().add("btn-see-detail");
        btnInterview.setOnAction(e -> openInterviewDetail(caseId));
        AnchorPane.setBottomAnchor(btnInterview, 25.0);
        AnchorPane.setRightAnchor(btnInterview, 140.0);

        detailWindow.getChildren().addAll(textDataBox, closeDetailBtn, btnSeeDetail, btnInterview);

        // Posisi awal muncul sedikit acak agar jendela baru tidak menimpa persis di atas jendela lama
        double offsetX = 200 + (Math.random() * 50);
        double offsetY = 150 + (Math.random() * 50);
        detailWindow.setLayoutX(offsetX);
        detailWindow.setLayoutY(offsetY);

        // Jadikan bisa digeser
        makeDraggable(detailWindow, detailWindow, desktop);

        // Tambahkan ke desktop dan bawa ke depan
        desktop.getChildren().add(detailWindow);
        detailWindow.toFront();
    }

    // --- FUNGSI MEMBUKA JENDELA KERTAS LAPORAN LENGKAP ---
    private void openFullDetail(String caseId) {
        CaseData data = gameEngine.getActiveCases().get(caseId);
        
        AnchorPane fileWindow = new AnchorPane();
        fileWindow.setMaxSize(450, 600); // Ukuran proporsional kertas A4
        fileWindow.setMinSize(450, 600);
        fileWindow.getStyleClass().add("file-window");
        
        if (fileDisplayImg != null) {
            ImageView bgFile = new ImageView(fileDisplayImg);
            bgFile.setFitWidth(450);
            bgFile.setFitHeight(600);
            fileWindow.getChildren().add(bgFile);
        } else {
            Pane fallbackBg = new Pane();
            fallbackBg.setPrefSize(450, 600);
            fallbackBg.getStyleClass().add("file-fallback-bg");
            fileWindow.getChildren().add(fallbackBg);
        }

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(140, 40, 40, 40));
        
        VBox dataBox = new VBox(10);
        dataBox.getChildren().addAll(
            new Label("CASE ID          : " + caseId),
            new Label("NAMA KASUS       : " + data.getTitle()),
            new Label("NAMA KORBAN      : " + data.getVictim()),
            new Label("LOKASI TKP       : " + data.getLocation()),
            new Label("TANGGAL KEJADIAN : " + data.getDate())
        );
        for(Node n : dataBox.getChildren()) {
            n.getStyleClass().add("file-text-bold");
        }
        
        Label lblDescTitle = new Label("DESKRIPSI KEJADIAN:");
        lblDescTitle.getStyleClass().add("file-text-bold");
        lblDescTitle.setStyle("-fx-padding: 15 0 0 0;");
        
        Label lblDesc = new Label(data.getDescription());
        lblDesc.getStyleClass().add("file-text");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(370);
        
        // --- BAGIAN BARANG BUKTI (EVIDENCE) ---
        Label lblEvidenceTitle = new Label("LAMPIRAN BUKTI:");
        lblEvidenceTitle.getStyleClass().add("file-text-bold");
        lblEvidenceTitle.setStyle("-fx-padding: 10 0 0 0;");
        
        HBox evidenceBox = new HBox(10);
        if (data.getEvidences() != null && !data.getEvidences().isEmpty()) {
            for (Evidence e : data.getEvidences()) {
                try {
                    File imgFile = new File(e.getImagePath());
                    if (imgFile.exists()) {
                        Image evImg = new Image(imgFile.toURI().toString());
                        ImageView evView = new ImageView(evImg);
                        evView.setFitWidth(60);
                        evView.setFitHeight(60);
                        evView.setPreserveRatio(true);
                        
                        VBox polaroid = new VBox();
                        polaroid.setStyle("-fx-background-color: white; -fx-padding: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 3, 0, 0, 1);");
                        polaroid.getChildren().add(evView);
                        
                        evidenceBox.getChildren().add(polaroid);
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal load bukti: " + e.getImagePath());
                }
            }
        }

        contentBox.getChildren().addAll(dataBox, lblDescTitle, lblDesc, lblEvidenceTitle, evidenceBox);
        
        Button closeFileBtn = new Button("✕");
        closeFileBtn.getStyleClass().add("file-close-btn");
        closeFileBtn.setOnAction(e -> desktop.getChildren().remove(fileWindow));
        AnchorPane.setTopAnchor(closeFileBtn, 15.0);
        AnchorPane.setRightAnchor(closeFileBtn, 20.0);
        
        AnchorPane.setTopAnchor(contentBox, 0.0);
        AnchorPane.setLeftAnchor(contentBox, 0.0);

        fileWindow.getChildren().addAll(contentBox, closeFileBtn);

        double offsetX = 250 + (Math.random() * 80);
        double offsetY = 50 + (Math.random() * 50);
        fileWindow.setLayoutX(offsetX);
        fileWindow.setLayoutY(offsetY);

        makeDraggable(fileWindow, fileWindow, desktop);
        desktop.getChildren().add(fileWindow);
        fileWindow.toFront();
    }

    // --- FUNGSI MEMBUKA JENDELA WAWANCARA SAKSI ---
    private void openInterviewDetail(String caseId) {
        CaseData data = gameEngine.getActiveCases().get(caseId);
        
        VBox interviewWindow = new VBox(0);
        interviewWindow.setPrefSize(400, 450);
        interviewWindow.getStyleClass().add("explorer-window");
        
        HBox header = new HBox();
        header.getStyleClass().add("exp-header");
        
        Label title = new Label("🎙 CATATAN INTEROGASI SAKSI");
        title.getStyleClass().add("exp-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("exp-close-btn");
        closeBtn.setOnAction(e -> desktop.getChildren().remove(interviewWindow));
        
        header.getChildren().addAll(title, spacer, closeBtn);

        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        
        if (data.getWitnesses() != null && !data.getWitnesses().isEmpty()) {
            for (Witness w : data.getWitnesses()) {
                HBox recordBox = new HBox(15);
                recordBox.setStyle("-fx-background-color: rgba(31, 37, 55, 0.6); -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #3b4255; -fx-border-radius: 8;");
                
                try {
                    File imgFile = new File(w.getPhotoPath());
                    if (imgFile.exists()) {
                        Image img = new Image(imgFile.toURI().toString());
                        ImageView imgView = new ImageView(img);
                        imgView.setFitWidth(60);
                        imgView.setFitHeight(60);
                        imgView.setPreserveRatio(true);
                        imgView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 0);");
                        recordBox.getChildren().add(imgView);
                    }
                } catch (Exception e) {}
                
                VBox txtBox = new VBox(5);
                Label lblName = new Label(w.getName());
                lblName.setStyle("-fx-text-fill: #4da6ff; -fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label lblQuote = new Label("\"" + w.getTestimony() + "\"");
                lblQuote.setWrapText(true);
                lblQuote.setStyle("-fx-text-fill: #c0c8d4; -fx-font-style: italic; -fx-font-size: 13px;");
                
                HBox bioBox = new HBox(15);
                bioBox.setPadding(new Insets(5, 0, 0, 0));
                
                try {
                    File dnaFile = new File(w.getSuspectDnaImage());
                    if(dnaFile.exists()) {
                        ImageView dnaView = new ImageView(new Image(dnaFile.toURI().toString()));
                        dnaView.setFitWidth(40); dnaView.setFitHeight(40); dnaView.setPreserveRatio(true);
                        dnaView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 0);");
                        Label lDna = new Label("Sampel DNA:"); 
                        lDna.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 10px;");
                        Button btnDna = new Button("🔍 Detail");
                        btnDna.setStyle("-fx-font-size: 9px; -fx-padding: 2;");
                        btnDna.setOnAction(ev -> openImageViewer("Detail DNA", dnaFile.toURI().toString()));
                        VBox dnaContainer = new VBox(2, lDna, dnaView, btnDna);
                        dnaContainer.setAlignment(Pos.CENTER);
                        bioBox.getChildren().add(dnaContainer);
                    }
                    
                    File fpFile = new File(w.getSuspectFingerprintImage());
                    if(fpFile.exists()) {
                        ImageView fpView = new ImageView(new Image(fpFile.toURI().toString()));
                        fpView.setFitWidth(40); fpView.setFitHeight(40); fpView.setPreserveRatio(true);
                        fpView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 0);");
                        Label lFp = new Label("Sidik Jari:"); 
                        lFp.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 10px;");
                        Button btnFp = new Button("🔍 Detail");
                        btnFp.setStyle("-fx-font-size: 9px; -fx-padding: 2;");
                        btnFp.setOnAction(ev -> openImageViewer("Detail Sidik Jari", fpFile.toURI().toString()));
                        VBox fpContainer = new VBox(2, lFp, fpView, btnFp);
                        fpContainer.setAlignment(Pos.CENTER);
                        bioBox.getChildren().add(fpContainer);
                    }
                } catch(Exception e) {}

                txtBox.getChildren().addAll(lblName, lblQuote, bioBox);
                recordBox.getChildren().add(txtBox);
                
                contentBox.getChildren().add(recordBox);
            }
        } else {
            Label noData = new Label("Tidak ada catatan wawancara untuk kasus ini.");
            noData.setStyle("-fx-text-fill: #8591a5; -fx-font-style: italic;");
            contentBox.getChildren().add(noData);
        }

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("exp-scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        interviewWindow.getChildren().addAll(header, scroll);

        double offsetX = 300 + (Math.random() * 50);
        double offsetY = 120 + (Math.random() * 50);
        interviewWindow.setLayoutX(offsetX);
        interviewWindow.setLayoutY(offsetY);

        makeDraggable(header, interviewWindow, desktop);
        desktop.getChildren().add(interviewWindow);
        interviewWindow.toFront();
    }

    // --- FUNGSI IMAGE VIEWER (POPUP DETAIL GAMBAR) ---
    private void openImageViewer(String titleText, String imageUri) {
        VBox viewerWindow = new VBox(0);
        viewerWindow.setPrefSize(300, 350);
        viewerWindow.getStyleClass().add("explorer-window");
        
        HBox header = new HBox();
        header.getStyleClass().add("exp-header");
        
        Label title = new Label("🔍 " + titleText);
        title.getStyleClass().add("exp-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("exp-close-btn");
        closeBtn.setOnAction(e -> desktop.getChildren().remove(viewerWindow));
        
        header.getChildren().addAll(title, spacer, closeBtn);

        VBox contentBox = new VBox(10);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        
        try {
            ImageView imgView = new ImageView(new Image(imageUri));
            imgView.setFitWidth(250);
            imgView.setFitHeight(250);
            imgView.setPreserveRatio(true);
            imgView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");
            contentBox.getChildren().add(imgView);
        } catch (Exception e) {
            contentBox.getChildren().add(new Label("Gagal memuat gambar."));
        }

        viewerWindow.getChildren().addAll(header, contentBox);

        double offsetX = 400 + (Math.random() * 50);
        double offsetY = 150 + (Math.random() * 50);
        viewerWindow.setLayoutX(offsetX);
        viewerWindow.setLayoutY(offsetY);

        makeDraggable(header, viewerWindow, desktop);
        desktop.getChildren().add(viewerWindow);
        viewerWindow.toFront();
    }

    // --- KELAS BANTUAN UNTUK SIMPAN TITIK KOORDINAT MOUSE ---
    private static class DragContext { 
        double x, y; 
        double maxX, maxY;
    }

    // --- FUNGSI AJAIB UNTUK BIKIN JENDELA BISA DIGESER (DRAGGABLE + BOUNDARY) ---
    private void makeDraggable(Node handle, Node window, AnchorPane desktop) {
        final DragContext dragContext = new DragContext();

        handle.setOnMousePressed(event -> {
            window.toFront();
            dragContext.x = window.getLayoutX() - event.getSceneX();
            dragContext.y = window.getLayoutY() - event.getSceneY();
            
            // Hitung bounds sekali saja di awal klik agar tidak berat saat ditarik
            dragContext.maxX = desktop.getWidth() - window.getLayoutBounds().getWidth();
            dragContext.maxY = desktop.getHeight() - window.getLayoutBounds().getHeight();
            
            AnchorPane.clearConstraints(window); 
            
            // Optimasi ekstrem: Nonaktifkan drop shadow sementara dan hidupkan cache
            window.setStyle("-fx-effect: null;");
            window.setCache(true);
            window.setCacheHint(javafx.scene.CacheHint.SPEED);
            
            event.consume();
        });

        handle.setOnMouseDragged(event -> {
            double newX = event.getSceneX() + dragContext.x;
            double newY = event.getSceneY() + dragContext.y;

            if (newX < 0) newX = 0;
            if (newX > dragContext.maxX) newX = dragContext.maxX;
            if (newY < 0) newY = 0;
            if (newY > dragContext.maxY) newY = dragContext.maxY;

            window.setLayoutX(newX);
            window.setLayoutY(newY);
            
            event.consume();
        });
        
        handle.setOnMouseReleased(event -> {
            // Matikan cache dan kembalikan efek drop shadow (menghapus inline style)
            window.setStyle("");
            window.setCache(false);
            window.setCacheHint(javafx.scene.CacheHint.DEFAULT);
            event.consume();
        });

        if (handle != window) {
            window.setOnMousePressed(event -> {
                window.toFront();
                dragContext.x = window.getLayoutX() - event.getSceneX();
                dragContext.y = window.getLayoutY() - event.getSceneY();
                
                dragContext.maxX = desktop.getWidth() - window.getLayoutBounds().getWidth();
                dragContext.maxY = desktop.getHeight() - window.getLayoutBounds().getHeight();
                
                AnchorPane.clearConstraints(window);
            });
        }
    }

    // --- FUNGSI MEMBUAT IKON DESKTOP ---
    private VBox createIcon(String labelText, String imgPath) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.getStyleClass().add("desktop-icon-box");
        
        try {
            File file = new File(imgPath);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                ImageView icon = new ImageView(img);
                icon.setFitWidth(55);
                icon.setPreserveRatio(true);
                box.getChildren().add(icon);
            } else {
                Label fallbackIcon = new Label("📁");
                fallbackIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: #4da6ff;");
                box.getChildren().add(fallbackIcon);
            }
        } catch (Exception e) {
            Label fallbackIcon = new Label("📁");
            fallbackIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: #4da6ff;");
            box.getChildren().add(fallbackIcon);
        }
        
        Label label = new Label(labelText);
        label.getStyleClass().add("desktop-icon-label");
        box.getChildren().add(label);

        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}