@echo off
java --module-path "lib/javafx-sdk-25.0.4/lib" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -jar ui-javafx.jar
pause
