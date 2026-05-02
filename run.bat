@echo off
:: Compile tetap pakai javac biasa
javac --module-path lib --add-modules javafx.controls,javafx.fxml Main.java

if %errorlevel% neq 0 (
    pause
    exit /b
)

:: Jalankan pakai JAVAW untuk hide terminal
start javaw --module-path lib --add-modules javafx.controls,javafx.fxml ^
     -Djava.library.path=. ^
     -Dprism.order=sw ^
     Main
exit