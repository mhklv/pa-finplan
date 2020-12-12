package org.mchklv.finplan.client.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.documentGen.DocumentGenerator;
import org.mchklv.finplan.client.documentGen.SpreadsheetDescriptor;
import org.mchklv.finplan.client.ui.export.ExportDialogStage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MainScreenNode extends VBox {
    @FXML
    private RadioButton envirSecRadioButton;
    @FXML
    private RadioButton admEconSecRadioButton;
    @FXML
    private ToggleGroup sectionToggleGroup;

    @FXML
    private BorderPane mainPane;

    @FXML
    private MenuBar menuBar;

    private DataProvider dataProvider;
    private KeyValuesScreenNode keyValuesScreenNode;
    
    private AdmEconTasksScreenNode admEconScreenNode;
    private TasksActivitiesTabPane admEconTabPane;
    

    public MainScreenNode(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        UICustomUtils.loadControllerFXML(this, "fxml/mainScreenNode.fxml");
        
        keyValuesScreenNode = new KeyValuesScreenNode(dataProvider);
        mainPane.setCenter(keyValuesScreenNode);
        initMenuBar();
        
        sceneProperty().addListener(new ChangeListener<Scene>() {

                @Override
                public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
                    if (getScene() != null) {
                        mainPane.prefHeightProperty().bind(getScene().heightProperty());
                    }
                }
            });

        initButtons();
        
        // SpreadsheetDescriptor desc = new SpreadsheetDescriptor();
        // DocumentGenerator gen = new DocumentGenerator(dataProvider, desc);
        // gen.setFirstYearDate(LocalDate.of(2020, 1, 1));
        
        // File file = new File("/home/michael/test.xlsx");
        
        // try {
        //     file.createNewFile();
        //     FileOutputStream out = new FileOutputStream(file);
        //     gen.generateGeneralPlan(out);
        //     out.close();
        // }
        // catch (Exception e) {
        //     System.err.println("Error " + e.getMessage());
        //     e.printStackTrace();
        // }

        
        
        
    }

    private void initMenuBar() {
        Menu exportMenu = new Menu("Экспорт");
        menuBar.getMenus().add(exportMenu);

        MenuItem exportDialogItem = new MenuItem("Экспорт...");
        exportMenu.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    ExportDialogStage exportDialog = new ExportDialogStage(dataProvider);
                    exportDialog.showAndWait();
                }
            });

        exportMenu.getItems().add(exportDialogItem);
    }

    private void initButtons() {
        envirSecRadioButton.setSelected(true);
        
        envirSecRadioButton.getStyleClass().remove("radio-button");
        envirSecRadioButton.getStyleClass().add("toggle-button");
        admEconSecRadioButton.getStyleClass().remove("radio-button");
        admEconSecRadioButton.getStyleClass().add("toggle-button");

        admEconSecRadioButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    if (admEconTabPane == null) {
                        admEconTabPane = new TasksActivitiesTabPane();
                        admEconScreenNode = new AdmEconTasksScreenNode(dataProvider);
                        TaskEventsScreen admEconTasksEventsNode = new TaskEventsScreen(dataProvider,
                                                                                       admEconScreenNode.getAdmEconTasksList());
                        admEconTabPane.setRationaleNode(admEconScreenNode);
                        admEconTabPane.setTasksScreenNode(admEconTasksEventsNode);
                    }

                    mainPane.setCenter(admEconTabPane);
                    
                    // if (admEconScreenNode == null) {
                    //     admEconScreenNode = new AdmEconTasksScreenNode(dataProvider);
                    // }

                    // mainPane.setCenter(admEconScreenNode);
                }
            });

        envirSecRadioButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if (keyValuesScreenNode == null) {
                        keyValuesScreenNode = new KeyValuesScreenNode(dataProvider);
                    }

                    mainPane.setCenter(keyValuesScreenNode);
                }
            });
    }
    
}
