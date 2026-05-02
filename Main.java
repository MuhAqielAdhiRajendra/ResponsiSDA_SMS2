import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
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
    private VBox databaseWindow;
    private VBox chatWindow;
    private VBox caseListContainer;
    private VBox chatContent; // Chat messages container
    private StackPane rootStack;
    private Image folderDisplayImg;
    private Image fileDisplayImg;

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

        VBox chatIconBox = createIcon("Chat Komandan", "img/Chat.png");
        AnchorPane.setTopAnchor(chatIconBox, 250.0);
        AnchorPane.setLeftAnchor(chatIconBox, 30.0);


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
        caseListContainer = new VBox(10);
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
            
            btnAnalyze.setDisable(true);
            lblResult.setStyle("-fx-text-fill: #ffcc00; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
            lblResult.setText(">> Menghubungkan ke server pusat...\n>> Mohon tunggu...");

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), ev -> {
                    lblResult.setText(">> Mencocokkan jejak DNA...\n>> Menganalisis sidik jari...");
                }),
                new KeyFrame(Duration.seconds(2), ev -> {
                    lblResult.setText(">> Menyusun profil biometrik...\n>> Menghitung probabilitas...");
                }),
                new KeyFrame(Duration.seconds(3), ev -> {
                    GameEngine.AnalysisResult result = gameEngine.analyzeSuspect(caseT, suspect);
                    
                    if (result.isCulprit) {
                        gameEngine.setCaseStatus(caseT, "COMPLETE");
                        lblResult.setStyle("-fx-text-fill: #00ff88; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
                        lblResult.setText(result.message + "\n\n\u2705 KASUS " + caseT + " DITUTUP \u2014 PELAKU DITEMUKAN!");
                        
                        // Generate kasus baru otomatis!
                        CaseData newCase = gameEngine.generateNewCase();
                        if (newCase != null) {
                            lblResult.setText(lblResult.getText() + "\n\n\ud83d\udce9 KASUS BARU MASUK: " + newCase.getCaseId() + " - " + newCase.getTitle());
                        }
                    } else {
                        gameEngine.setCaseStatus(caseT, "WRONG PERSON");
                        gameEngine.addWrongAnswer();
                        lblResult.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
                        lblResult.setText(result.message + "\n\n\u274c ORANG YANG SALAH! Tidak ada kasus baru.");
                    }
                    refreshCaseList();
                    btnAnalyze.setDisable(false);
                    
                    // Cek Bad Ending
                    if (gameEngine.isGameOver()) {
                        new Timeline(new KeyFrame(Duration.seconds(2), ev2 -> showBadEnding())).play();
                    }
                })
            );
            timeline.play();
        });

        dbContent.getChildren().addAll(lblDbDesc, txtTargetCase, txtSuspect, txtAnomaly, btnAnalyze, lblResult);
        databaseWindow.getChildren().addAll(dbHeader, dbContent);

        databaseWindow.setLayoutX(500);
        databaseWindow.setLayoutY(100);
        makeDraggable(dbHeader, databaseWindow, desktop);

        // =========================================================
        // JENDELA 3: CHAT KOMANDAN
        // =========================================================
        chatWindow = new VBox(0);
        chatWindow.setPrefSize(380, 400);
        chatWindow.getStyleClass().add("explorer-window");
        chatWindow.setVisible(false);

        HBox chatHeader = new HBox();
        chatHeader.getStyleClass().add("exp-header");
        Label chatTitle = new Label("💬 PESAN DARI KOMANDAN");
        chatTitle.getStyleClass().add("exp-title");
        Region chatSpacer = new Region();
        HBox.setHgrow(chatSpacer, Priority.ALWAYS);
        Button chatCloseBtn = new Button("✕");
        chatCloseBtn.getStyleClass().add("exp-close-btn");
        chatCloseBtn.setOnAction(e -> chatWindow.setVisible(false));
        chatHeader.getChildren().addAll(chatTitle, chatSpacer, chatCloseBtn);

        chatContent = new VBox(10);
        chatContent.setPadding(new Insets(15));

        Label chatWelcome = new Label("👮 Komandan: Selamat datang, Agen. Tunggu instruksi selanjutnya.");
        chatWelcome.setWrapText(true);
        chatWelcome.setStyle("-fx-text-fill: #8591a5; -fx-font-style: italic; -fx-font-size: 12px;");
        chatContent.getChildren().add(chatWelcome);

        ScrollPane chatScroll = new ScrollPane(chatContent);
        chatScroll.setFitToWidth(true);
        chatScroll.getStyleClass().add("exp-scroll-pane");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        chatWindow.getChildren().addAll(chatHeader, chatScroll);
        chatWindow.setLayoutX(550);
        chatWindow.setLayoutY(200);
        makeDraggable(chatHeader, chatWindow, desktop);

        // =========================================================
        // GABUNGKAN KE DESKTOP & AKTIFKAN FITUR DRAG (GESER)
        // =========================================================
        desktop.getChildren().addAll(folderBox, caseBox, chatIconBox, explorerWindow, databaseWindow, chatWindow);


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

        // Chat Komandan -> Buka Chat
        chatIconBox.setOnMouseClicked(e -> {
            chatWindow.setVisible(true);
            chatWindow.toFront();
        });


        // ROOT LAYER
        VBox mainRoot = new VBox();
        VBox.setVgrow(desktop, Priority.ALWAYS);
        mainRoot.getChildren().addAll(desktop, taskbar);
        mainRoot.setVisible(false); // Sembunyikan dulu

        // =========================================================
        // BOOT / LOADING SCREEN (Windows-style)
        // =========================================================
        VBox bootScreen = new VBox(20);
        bootScreen.setAlignment(Pos.CENTER);
        bootScreen.setStyle("-fx-background-color: linear-gradient(to bottom, #0a0e1a, #111827);");

        Label bootLogo = new Label("⬡ CORE");
        bootLogo.setStyle("-fx-text-fill: #4da6ff; -fx-font-size: 42px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");

        Label bootSubtitle = new Label("Digital DNA OS v3.2.1");
        bootSubtitle.setStyle("-fx-text-fill: #5a6a80; -fx-font-size: 14px; -fx-font-family: 'Consolas';");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(350);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: #4da6ff;");

        Label bootStatus = new Label("Initializing system...");
        bootStatus.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 12px; -fx-font-family: 'Consolas';");

        Label bootCopyright = new Label("© 2026 CORE Intelligence Agency. All rights reserved.");
        bootCopyright.setStyle("-fx-text-fill: #3b4255; -fx-font-size: 10px; -fx-padding: 40 0 0 0;");

        bootScreen.getChildren().addAll(bootLogo, bootSubtitle, progressBar, bootStatus, bootCopyright);

        rootStack = new StackPane(mainRoot, bootScreen);

        Scene scene = new Scene(rootStack, 1024, 720);
        try {
            File cssFile = new File("style.css");
            scene.getStylesheets().add(cssFile.toURI().toString());
        } catch (Exception e) {
            System.out.println("Gagal load style.css");
        }

        primaryStage.setTitle("CORE - Digital DNA OS Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // --- ANIMASI LOADING ---
        Timeline bootTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.5), ev -> {
                progressBar.setProgress(0.15);
                bootStatus.setText("Loading kernel modules...");
            }),
            new KeyFrame(Duration.seconds(1.0), ev -> {
                progressBar.setProgress(0.30);
                bootStatus.setText("Mounting file system...");
            }),
            new KeyFrame(Duration.seconds(1.5), ev -> {
                progressBar.setProgress(0.45);
                bootStatus.setText("Loading case database...");
            }),
            new KeyFrame(Duration.seconds(2.0), ev -> {
                progressBar.setProgress(0.60);
                bootStatus.setText("Initializing biometric scanner...");
            }),
            new KeyFrame(Duration.seconds(2.5), ev -> {
                progressBar.setProgress(0.75);
                bootStatus.setText("Connecting to CORE server...");
            }),
            new KeyFrame(Duration.seconds(3.0), ev -> {
                progressBar.setProgress(0.90);
                bootStatus.setText("Preparing desktop environment...");
            }),
            new KeyFrame(Duration.seconds(3.5), ev -> {
                progressBar.setProgress(1.0);
                bootStatus.setText("Welcome, Agent.");
                bootStatus.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 12px; -fx-font-family: 'Consolas';");
            }),
            new KeyFrame(Duration.seconds(4.2), ev -> {
                bootScreen.setVisible(false);
                mainRoot.setVisible(true);
            }),
            // Setelah boot, kirim pesan pertama dari Komandan
            new KeyFrame(Duration.seconds(6.0), ev -> {
                sendCommanderMessage();
            })
        );
        bootTimeline.play();
    }

    // --- KIRIM PESAN DARI KOMANDAN KE CHAT ---
    private void sendCommanderMessage() {
        if (!gameEngine.hasCommanderMessages()) return;

        String[] msg = gameEngine.getNextCommanderMessage();
        if (msg == null) return;

        VBox msgBubble = new VBox(8);
        msgBubble.setStyle("-fx-background-color: rgba(31, 37, 55, 0.8); -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #4da6ff; -fx-border-radius: 10; -fx-border-width: 1;");

        HBox senderBox = new HBox(8);
        senderBox.setAlignment(Pos.CENTER_LEFT);
        try {
            File komandanFile = new File("img/Orang/Komandan.png");
            if (komandanFile.exists()) {
                ImageView komandanImg = new ImageView(new Image(komandanFile.toURI().toString()));
                komandanImg.setFitWidth(30);
                komandanImg.setFitHeight(30);
                komandanImg.setPreserveRatio(true);
                komandanImg.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 0);");
                senderBox.getChildren().add(komandanImg);
            }
        } catch (Exception ex) {}
        Label lblSender = new Label("Komandan");
        lblSender.setStyle("-fx-text-fill: #4da6ff; -fx-font-weight: bold; -fx-font-size: 13px;");
        senderBox.getChildren().add(lblSender);

        Label lblTitle = new Label(msg[0]);
        lblTitle.setStyle("-fx-text-fill: #ffcc00; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label lblBody = new Label(msg[1]);
        lblBody.setWrapText(true);
        lblBody.setStyle("-fx-text-fill: #c0c8d4; -fx-font-size: 12px;");

        HBox btnBox = new HBox(10);
        btnBox.setPadding(new Insets(5, 0, 0, 0));

        Button btnAccept = new Button("\u2705 Terima");
        btnAccept.setStyle("-fx-background-color: #00aa55; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15; -fx-background-radius: 6; -fx-cursor: hand;");

        Button btnReject = new Button("\u274c Tolak");
        btnReject.setStyle("-fx-background-color: #aa3333; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15; -fx-background-radius: 6; -fx-cursor: hand;");

        btnAccept.setOnAction(e -> {
            gameEngine.acceptMission();
            btnAccept.setDisable(true);
            btnReject.setDisable(true);
            btnAccept.setText("\u2705 Diterima");
            btnReject.setVisible(false);

            Label response = new Label("\ud83d\udc64 Agen: Siap laksanakan, Komandan.");
            response.setWrapText(true);
            response.setStyle("-fx-text-fill: #00ff88; -fx-font-style: italic; -fx-font-size: 12px; -fx-padding: 5 0 0 10;");
            chatContent.getChildren().add(response);

            refreshCaseList();

            // Kirim pesan berikutnya setelah delay
            new Timeline(new KeyFrame(Duration.seconds(8), ev -> sendCommanderMessage())).play();
        });

        btnReject.setOnAction(e -> {
            gameEngine.addRejectCount();
            btnAccept.setDisable(true);
            btnReject.setDisable(true);
            btnReject.setText("\u274c Ditolak");
            btnAccept.setVisible(false);

            Label response = new Label("\ud83d\udc64 Agen: Maaf Komandan, saya tidak bisa saat ini.");
            response.setWrapText(true);
            response.setStyle("-fx-text-fill: #ff6644; -fx-font-style: italic; -fx-font-size: 12px; -fx-padding: 5 0 0 10;");
            chatContent.getChildren().add(response);

            Label warning = new Label("\u26a0 Peringatan: Penolakan ke-" + gameEngine.getRejectCount() + "/3");
            warning.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px; -fx-font-weight: bold;");
            chatContent.getChildren().add(warning);

            if (gameEngine.isFiredForInsubordination()) {
                new Timeline(new KeyFrame(Duration.seconds(2), ev -> showBadEndingInsubordination())).play();
            } else {
                // Kirim pesan berikutnya
                new Timeline(new KeyFrame(Duration.seconds(8), ev -> sendCommanderMessage())).play();
            }
        });

        btnBox.getChildren().addAll(btnAccept, btnReject);
        msgBubble.getChildren().addAll(senderBox, lblTitle, lblBody, btnBox);
        chatContent.getChildren().add(msgBubble);

        // Buka chat window otomatis dan flash
        chatWindow.setVisible(true);
        chatWindow.toFront();
    }

    // --- BAD ENDING: INSUBORDINASI (Terlalu banyak menolak) ---
    private void showBadEndingInsubordination() {
        VBox screen = new VBox(20);
        screen.setAlignment(Pos.CENTER);
        screen.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0a00, #0a0500);");

        Label icon = new Label("\ud83d\udeab");
        icon.setStyle("-fx-font-size: 80px;");

        Label title = new Label("PEMBANGKANGAN");
        title.setStyle("-fx-text-fill: #ff6622; -fx-font-size: 44px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");

        Label subtitle = new Label("Kamu menolak terlalu banyak perintah dari Komandan.\nSikap insubordinasimu tidak bisa ditoleransi.\n\nKamu resmi DIPECAT dari Divisi Forensik.");
        subtitle.setStyle("-fx-text-fill: #996644; -fx-font-size: 14px; -fx-font-family: 'Consolas'; -fx-text-alignment: center;");
        subtitle.setAlignment(Pos.CENTER);

        Label stats = new Label("Perintah Ditolak: " + gameEngine.getRejectCount() + "/3");
        stats.setStyle("-fx-text-fill: #ff6622; -fx-font-size: 16px; -fx-font-family: 'Consolas';");

        Button btnRestart = new Button("\u21bb MULAI ULANG");
        btnRestart.setStyle("-fx-background-color: #ff6622; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRestart.setOnAction(e -> {
            Stage stage = (Stage) rootStack.getScene().getWindow();
            stage.close();
            new Main().start(new Stage());
        });

        screen.getChildren().addAll(icon, title, subtitle, stats, btnRestart);
        rootStack.getChildren().add(screen);
    }

    // --- BAD ENDING: KAMU DIPECAT ---
    private void showBadEnding() {
        VBox badEndingScreen = new VBox(20);
        badEndingScreen.setAlignment(Pos.CENTER);
        badEndingScreen.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0000, #0a0000);");

        Label skull = new Label("\u2620");
        skull.setStyle("-fx-font-size: 80px;");

        Label title = new Label("KAMU DIPECAT");
        title.setStyle("-fx-text-fill: #ff2222; -fx-font-size: 48px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");

        Label subtitle = new Label("Semua kasus gagal diselesaikan.\nOrang yang tidak bersalah ditangkap atas kesalahanmu.\nKarirmu sebagai agen forensik telah berakhir.");
        subtitle.setStyle("-fx-text-fill: #994444; -fx-font-size: 14px; -fx-font-family: 'Consolas'; -fx-text-alignment: center;");
        subtitle.setAlignment(Pos.CENTER);

        Label stats = new Label("Kasus Salah: " + gameEngine.getWrongCount());
        stats.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 16px; -fx-font-family: 'Consolas';");

        Button btnRestart = new Button("\u21bb MULAI ULANG");
        btnRestart.setStyle("-fx-background-color: #ff2222; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRestart.setOnAction(e -> {
            // Restart game
            Stage stage = (Stage) rootStack.getScene().getWindow();
            stage.close();
            new Main().start(new Stage());
        });

        badEndingScreen.getChildren().addAll(skull, title, subtitle, stats, btnRestart);
        rootStack.getChildren().add(badEndingScreen);
    }

    // --- REFRESH DAFTAR KASUS DENGAN STATUS (COMPLETE / WRONG PERSON) ---
    private void refreshCaseList() {
        caseListContainer.getChildren().clear();
        for (String caseId : gameEngine.getActiveCases().keySet()) {
            CaseData cData = gameEngine.getActiveCases().get(caseId);
            String status = gameEngine.getCaseStatus(caseId);
            
            String label = "📄 " + caseId + " - " + cData.getTitle();
            if (status.equals("COMPLETE")) {
                label = "✅ " + caseId + " - " + cData.getTitle() + " [COMPLETE]";
            } else if (status.equals("WRONG PERSON")) {
                label = "❌ " + caseId + " - " + cData.getTitle() + " [WRONG PERSON]";
            }
            
            Button btnCase = new Button(label);
            btnCase.setMaxWidth(Double.MAX_VALUE);
            btnCase.setAlignment(Pos.CENTER_LEFT);
            btnCase.getStyleClass().add("btn-case");
            
            if (status.equals("COMPLETE")) {
                btnCase.setStyle("-fx-border-color: #00ff88; -fx-border-width: 0 0 0 3;");
            } else if (status.equals("WRONG PERSON")) {
                btnCase.setStyle("-fx-border-color: #ff4444; -fx-border-width: 0 0 0 3;");
            }
            
            btnCase.setOnAction(e -> openCaseDetail(caseId));
            caseListContainer.getChildren().add(btnCase);
        }
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
            for (Evidence ev : data.getEvidences()) {
                try {
                    File imgFile = new File(ev.getImagePath());
                    if (imgFile.exists()) {
                        Image evImg = new Image(imgFile.toURI().toString());
                        ImageView evView = new ImageView(evImg);
                        evView.setFitWidth(60);
                        evView.setFitHeight(60);
                        evView.setPreserveRatio(true);
                        
                        Button btnEvDetail = new Button("🔍");
                        btnEvDetail.setStyle("-fx-font-size: 8px; -fx-padding: 1 4;");
                        String evUri = imgFile.toURI().toString();
                        btnEvDetail.setOnAction(e -> openImageViewer(ev.getName(), evUri));

                        VBox polaroid = new VBox(2);
                        polaroid.setAlignment(Pos.CENTER);
                        polaroid.setStyle("-fx-background-color: white; -fx-padding: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 3, 0, 0, 1);");
                        polaroid.getChildren().addAll(evView, btnEvDetail);
                        
                        evidenceBox.getChildren().add(polaroid);
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal load bukti: " + ev.getImagePath());
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
                
                // --- DNA ---
                if (w.getSuspectDnaImage() != null && !w.getSuspectDnaImage().isEmpty()) {
                    try {
                        File dnaFile = new File(w.getSuspectDnaImage());
                        if(dnaFile.exists()) {
                            ImageView dnaView = new ImageView(new Image(dnaFile.toURI().toString()));
                            dnaView.setFitWidth(40); dnaView.setFitHeight(40); dnaView.setPreserveRatio(true);
                            dnaView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 0);");
                            Label lDna = new Label("Sampel DNA:"); 
                            lDna.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 10px;");
                            Button btnDna = new Button("\ud83d\udd0d Detail");
                            btnDna.setStyle("-fx-font-size: 9px; -fx-padding: 2;");
                            btnDna.setOnAction(ev -> openImageViewer("Detail DNA", dnaFile.toURI().toString()));
                            VBox dnaContainer = new VBox(2, lDna, dnaView, btnDna);
                            dnaContainer.setAlignment(Pos.CENTER);
                            bioBox.getChildren().add(dnaContainer);
                        }
                    } catch(Exception e) {}
                } else {
                    Label lDna = new Label("Sampel DNA:");
                    lDna.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 10px;");
                    Label lError = new Label("\u26a0 SAMPEL\nRUSAK");
                    lError.setStyle("-fx-text-fill: #ff6644; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-alignment: center; -fx-padding: 8 5;");
                    lError.setAlignment(Pos.CENTER);
                    VBox dnaContainer = new VBox(2, lDna, lError);
                    dnaContainer.setAlignment(Pos.CENTER);
                    dnaContainer.setStyle("-fx-background-color: rgba(255,50,50,0.1); -fx-background-radius: 5; -fx-padding: 3;");
                    bioBox.getChildren().add(dnaContainer);
                }

                // --- SIDIK JARI ---
                if (w.getSuspectFingerprintImage() != null && !w.getSuspectFingerprintImage().isEmpty()) {
                    try {
                        File fpFile = new File(w.getSuspectFingerprintImage());
                        if(fpFile.exists()) {
                            ImageView fpView = new ImageView(new Image(fpFile.toURI().toString()));
                            fpView.setFitWidth(40); fpView.setFitHeight(40); fpView.setPreserveRatio(true);
                            fpView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 0);");
                            Label lFp = new Label("Sidik Jari:"); 
                            lFp.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 10px;");
                            Button btnFp = new Button("\ud83d\udd0d Detail");
                            btnFp.setStyle("-fx-font-size: 9px; -fx-padding: 2;");
                            btnFp.setOnAction(ev -> openImageViewer("Detail Sidik Jari", fpFile.toURI().toString()));
                            VBox fpContainer = new VBox(2, lFp, fpView, btnFp);
                            fpContainer.setAlignment(Pos.CENTER);
                            bioBox.getChildren().add(fpContainer);
                        }
                    } catch(Exception e) {}
                } else {
                    Label lFp = new Label("Sidik Jari:");
                    lFp.setStyle("-fx-text-fill: #8591a5; -fx-font-size: 10px;");
                    Label lError = new Label("\u26a0 GAGAL\nDIPINDAI");
                    lError.setStyle("-fx-text-fill: #ff6644; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-alignment: center; -fx-padding: 8 5;");
                    lError.setAlignment(Pos.CENTER);
                    VBox fpContainer = new VBox(2, lFp, lError);
                    fpContainer.setAlignment(Pos.CENTER);
                    fpContainer.setStyle("-fx-background-color: rgba(255,50,50,0.1); -fx-background-radius: 5; -fx-padding: 3;");
                    bioBox.getChildren().add(fpContainer);
                }

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