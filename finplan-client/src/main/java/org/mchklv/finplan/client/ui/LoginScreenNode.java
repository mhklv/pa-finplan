package org.mchklv.finplan.client.ui;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalDataProvider;
import org.mchklv.finplan.client.Main;
import org.mchklv.finplan.client.NetworkDataProvider;
import org.mchklv.finplan.common.*;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;



public class LoginScreenNode extends BorderPane implements PARegistrationNode.PAAddable {
    @FXML
    private ListView<ProtectedArea> PAsListView;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authorizeButton;
    @FXML
    private Button registerButton;
    @FXML
    private RadioButton networkModeRadioButton;
    @FXML
    private RadioButton localModeRadioButton;

    private DataProvider dataProvider;
    private NetworkDataProvider networkDataProvider;
    private LocalDataProvider localDataProvider;

    
    public LoginScreenNode() {
        this.networkDataProvider = Main.initNetworkDataProvider();
        this.localDataProvider = Main.initlocalDataProvider();
        UICustomUtils.loadControllerFXML(this, "fxml/loginScreen.fxml");
        initPAsList();
        initButtons();
        // DPICustomUtils.setNodeFontSize(this, DPICustomUtils.fromPtToPx(8));
    }

    public void addProtectedArea(ProtectedArea newArea) {
        // refreshList();
        PAsListView.getItems().add(newArea);
        // PAsListView.refresh();
    }


    
    private void initPAsList() {
        PAsListView.setCellFactory(new Callback<ListView<ProtectedArea>, ListCell<ProtectedArea>>() {

                @Override
                public ListCell<ProtectedArea> call(ListView<ProtectedArea> param) {
                    return new ListCell<ProtectedArea>() {
                        @Override
                        public void updateItem(ProtectedArea item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setText("");
                            }
                            else {
                                setText(item.getName());
                                setGraphic(null);
                            }
                        }
                    };
                }
            });
        PAsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // refreshList();
    }

    private void refreshList() {
        if (dataProvider != null) {
            try {
                PAsListView.setItems(FXCollections.observableArrayList(dataProvider.getAllProtectedAreas()));
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();
            
                Alert networkErrorAlert = new Alert(Alert.AlertType.ERROR);
                networkErrorAlert.setTitle("Ошибка сети");
                networkErrorAlert.setHeaderText("Произшла ошибка при соединении с Интернетом при попытке загрузить список ООПТ");
                networkErrorAlert.showAndWait();
            }
        }
    }

    private void initButtons() {
        localModeRadioButton.getStyleClass().remove("radio-button");
        localModeRadioButton.getStyleClass().add("toggle-button");
        networkModeRadioButton.getStyleClass().remove("radio-button");
        networkModeRadioButton.getStyleClass().add("toggle-button");

        localModeRadioButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    dataProvider = localDataProvider;
                    passwordField.setDisable(true);
                    refreshList();
                }
            });
        networkModeRadioButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    dataProvider = networkDataProvider;
                    passwordField.setDisable(false);
                    refreshList();
                }
            });
        
        networkModeRadioButton.setSelected(true);
        dataProvider = networkDataProvider;
        refreshList();

        
        registerButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    boolean isLocal;
                    if (localModeRadioButton.isSelected()) {
                        isLocal = true;
                    }
                    else {
                        isLocal = false;
                    }
                    
                    NavigatableScene parentScene = (NavigatableScene) getScene();
                    PARegistrationNode regNode = new PARegistrationNode(dataProvider, LoginScreenNode.this, isLocal);
                    regNode.setAsRoot(parentScene);
                }
            });

        authorizeButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if (!checkFields()) {
                        return;
                    }
                    
                    ProtectedArea chosenArea = PAsListView.getSelectionModel().getSelectedItem();
                    String password = passwordField.getText();

                    try {
                        dataProvider.authorize(chosenArea, password);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();
                        UICustomUtils.showError("Произошла ошибка при попытке авторизации", e.getMessage());
                        return;
                    }

                    NavigatableScene parentScene = (NavigatableScene) getScene();
                    MainScreenNode mainScreenNode = new MainScreenNode(dataProvider);
                    parentScene.setNewRoot(mainScreenNode);
                    // KeyValuesScreenNode kvNode = new KeyValuesScreenNode(dataProvider);
                    // kvNode.setAsRoot(parentScene);
                }

                private boolean checkFields() {
                    boolean isLocal;
                    if (localModeRadioButton.isSelected()) {
                        isLocal = true;
                    }
                    else {
                        isLocal = false;
                    }

                    String password = passwordField.getText();
                    boolean isPANotSelected = PAsListView.getSelectionModel().isEmpty();

                    if (isLocal) {
                        if (isPANotSelected) {
                            UICustomUtils.showError("Не выбрана ООПТ",
                                                    "Не выбрана ООПТ. Пожалуйста, выберете Вашу ООПТ в списке слева.");
                            return false;
                        }
                        else {
                            return true;
                        }
                    }
                    else {
                        if (isPANotSelected) {
                            UICustomUtils.showError("Не выбрана ООПТ",
                                                    "Не выбрана ООПТ. Пожалуйста, выберете Вашу ООПТ в списке слева.");
                            return false;
                        }
                        else if (password.length() < 8) {
                            UICustomUtils.showError("Пароль слишком короткий",
                                                    "Пароль слишком короткий. Пожалуйста, введите пароль длиной в 8 символов или длиннее.");
                            return false;
                        }
                        else {
                            return true;
                        }
                    }
                }
            });
    }
}
