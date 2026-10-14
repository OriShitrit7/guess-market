@echo off
cd /d "%~dp0"
java --module-path "lib/javafx-sdk-25.0.4/lib" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -jar javafx-client.jar
pause
