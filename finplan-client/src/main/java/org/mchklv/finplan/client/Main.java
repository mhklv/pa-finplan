package org.mchklv.finplan.client;

import java.awt.Toolkit;

import org.mchklv.finplan.client.ui.LoginScreenNode;
import org.mchklv.finplan.client.ui.NavigatableScene;
import org.mchklv.finplan.common.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeView;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class Main extends Application {
    private static Stage primaryStage;
    
    public static void main(String[] args) {
        // System.setProperty("sun.java2d.opengl", "true");
        // System.setProperty("prism.allowhidpi", "true");
        Application.launch(args);
    }
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        LocalStorageManager.initLocalStorageManager();
        setPrimaryStage(primaryStage);
        
        LoginScreenNode loginNode = new LoginScreenNode();
        NavigatableScene loginScene = new NavigatableScene(loginNode); 
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(LocalStorageManager.getSettingsManager().getMainWindowPos().getWidth());
        primaryStage.setHeight(LocalStorageManager.getSettingsManager().getMainWindowPos().getHeight());
        primaryStage.setTitle("Финансовое планирование ООПТ");

        primaryStage.getIcons().addAll(LocalStorageManager.getAppIcons());
        
        // Screen screen = Screen.getPrimary();
        // double dpi = screen.getDpi();
        // double scaleX = 0;
        // double scaleY = 0;
        // System.out.println("DPI: " + dpi + " - scaleX: " + scaleX + " - scaleY: " + scaleY);
        
        primaryStage.show();
	}


    public static NetworkDataProvider initNetworkDataProvider() {
        String serverAddress = LocalStorageManager.getSettingsManager().getServerAddress();
        NetworkDataProvider dataProvider = new NetworkDataProvider(serverAddress);
        // DataProvider dataProvider = new NetworkDataProvider("localhost");
        
        try {
            dataProvider.connect();
        }
        catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
            
            // Alert networkErrorAlert = new Alert(Alert.AlertType.ERROR);
            // networkErrorAlert.setTitle("Ошибка сети");
            // networkErrorAlert.setHeaderText("Произшла ошибка при соединении с Интернетом");
            // networkErrorAlert.showAndWait();
            
            UICustomUtils.showError("Ошибка сети", "Произшла ошибка при соединении с Интернетом");

            return null;
        }

        return dataProvider;
    }

    public static LocalDataProvider initlocalDataProvider() {
        LocalDataProvider dataProvider = new LocalDataProvider();

        try {
            dataProvider.connect();
        }
        catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка", "Произшла ошибка при инициализации локального хранилища");

            return null;
        }

        return dataProvider;
    }


    public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Main.primaryStage = primaryStage;
	}
}
