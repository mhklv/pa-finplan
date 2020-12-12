package org.mchklv.finplan.client;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.mchklv.finplan.client.documentGen.DocumentGenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class UICustomUtils {
    public static void loadControllerFXML(Object controller, String fxmlResourcePath) {
        URL fxmlURL = controller.getClass().getClassLoader().getResource(fxmlResourcePath);

        FXMLLoader fxmlLoader = new FXMLLoader();
        
        fxmlLoader.setLocation(fxmlURL);
        fxmlLoader.setRoot(controller);
        fxmlLoader.setController(controller);

        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    

    public static void showError(String errorTitle, String errorMessage) {
        Alert networkErrorAlert = new Alert(Alert.AlertType.INFORMATION);
        double primaryFontSizePx = DPICustomUtils
            .fromPtToPx(LocalStorageManager.getSettingsManager().getPrimaryFont().getSize());
        DPICustomUtils.setNodeFontSize(networkErrorAlert.getDialogPane().getScene().getRoot(), primaryFontSizePx);
        // networkErrorAlert.getDialogPane().getStylesheets()
        //         .add(UICustomUtils.class.getClassLoader().getResource("css/alertFont.css").toExternalForm());
        // System.out.println(UICustomUtils.class.getClassLoader().getResource("css/alertFont.css").toExternalForm());
        networkErrorAlert.getDialogPane().getStyleClass().add("customAlert");
        networkErrorAlert.setTitle(errorTitle);
        networkErrorAlert.setHeaderText(errorMessage);
        networkErrorAlert.showAndWait();
    }

    public static void showUnrecoverableNetworkError(NetworkDataProvider dataProvider) {
        UICustomUtils.showError("Unrecoverable network error (temporary)", "Unrecoverable network error (temporary)");
        Main.getPrimaryStage().close();
    }

    
    // Returns true if successfull. False otherwise.
    public static boolean tryExportToFile(DocumentGenerator docGen, Window currentWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для экспорта");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Таблица Excel", "*.xlsx"));
        File selectedFile = fileChooser.showSaveDialog(currentWindow);
        OutputStream out = null;

        try {
            try {
                if (selectedFile != null) {
                    selectedFile.createNewFile();
                    out = new BufferedOutputStream(new FileOutputStream(selectedFile));
                    docGen.generateGeneralPlan(out);
                    return true;
                }
            }
            finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка",
                                    "Проиошла ошибка при попытке сохранить экспортированный файл.");
            return false;
        }

        return false;
    }
}
