package org.mchklv.finplan.client.ui;

import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.common.ProtectedArea;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PARegistrationNode extends VBox {
    public interface PAAddable {
        public void addProtectedArea(ProtectedArea newArea);
    }

    
    @FXML
    private TextField PANameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordRepField;
    @FXML
    private TextField regQuotaField;
    @FXML
    private Button registerButton;
    @FXML
    private Button cancelButton;

    private DataProvider dataProvider;
    private NavigatableScene parentScene;
    private PAAddable PAContainer;
    private boolean isLocal;
    

    public PARegistrationNode(DataProvider dataProvider, PAAddable PAContainer, boolean isLocal) {
        this.isLocal = isLocal;
        this.dataProvider = dataProvider;
        this.PAContainer = PAContainer;
        UICustomUtils.loadControllerFXML(this, "fxml/PARegScreen.fxml");
        initButtons();
        initFields();
    }

    public void setAsRoot(NavigatableScene parentScene) {
        this.parentScene = parentScene;
        parentScene.setNewRoot(this);
    }

    
    private void initFields() {
        if (isLocal) {
            passwordField.setDisable(true);
            passwordRepField.setDisable(true);
            regQuotaField.setDisable(true);
        }
    }

    private void initButtons() {
        cancelButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    parentScene.setPrevoiusRoot();
                }
            });

        registerButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if (!checkFields()) {
                        return;
                    }
                    
                    String password = passwordField.getText();
                    String regQuota = regQuotaField.getText().toLowerCase();
                    ProtectedArea newArea = new ProtectedArea(null, PANameField.getText());

                    try {
                        dataProvider.register(newArea, regQuota, password);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();
                        UICustomUtils.showError("Ошибка соединения", e.getMessage());
                        return;
                    }
                    
                    if (newArea.getId() == null) {
                        UICustomUtils.showError("Регистрация не удалась",
                                "Регистрация не удалась. Возможно, ключ регистрации недействителен или введён неверно");
                        return;
                    }

                    PAContainer.addProtectedArea(newArea);

                    parentScene.setPrevoiusRoot();
                }

                private boolean checkFields() {
                    String areaName = PANameField.getText();
                    String password = passwordField.getText();
                    String passwordRep = passwordRepField.getText();
                        
                    if (isLocal) {
                        if (areaName.isEmpty()) {
                            UICustomUtils.showError("Пустое название ООПТ",
                                                    "Поле с названием ООПТ не должно быть пустым");
                            return false;
                        }
                        else {
                            return true;
                        }
                    }
                    else {
                        if (!password.equals(passwordRep)) {
                            UICustomUtils.showError("Пароли не совпадают",
                                                    "Пароли не совпадают. Пожалуйста, проверьте правильность ввода");
                            return false;
                        }
                        else if (password.length() < 8) {
                            UICustomUtils.showError("Пароль слишком короткий",
                                                    "Пароль слишком короткий. Пожалуйста, придумайте пароль длиной в 8 символов или длиннее.");
                            return false;
                        }
                        else if (areaName.isEmpty()) {
                            UICustomUtils.showError("Пустое название ООПТ",
                                                    "Поле с названием ООПТ не должно быть пустым");
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
