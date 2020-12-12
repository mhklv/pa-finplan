package org.mchklv.finplan.client.ui;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.KeyValueGroup;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GroupChosenRightNode extends VBox {
    interface KeyValueAddable {
        public void addKeyValue(KeyValueGroup parentGroup, KeyValue newValue);
    }
    
    @FXML
    private TextField keyValueNameField;
    @FXML
    private Button createKVButton;

    private DataProvider dataProvider;
    private KeyValueGroup currentKeyValueGroup;
    private KeyValueAddable keyValueAddable;

    
    public GroupChosenRightNode(DataProvider dataProvider, KeyValueGroup currentGroup,
                                KeyValueAddable keyValueAddable) {
        this.keyValueAddable = keyValueAddable;
        this.dataProvider = dataProvider;
        this.currentKeyValueGroup = currentGroup;
        UICustomUtils.loadControllerFXML(this, "fxml/keyValueGroupRightScreen.fxml");
        initButton();
    }

    private void initButton() {
        createKVButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    KeyValue newKeyValue = new KeyValue(null, keyValueNameField.getText(), null);

                    try {
                        dataProvider.insertKeyValue(newKeyValue, currentKeyValueGroup);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                "Проиошла ошибка при соединении с сервером при попытке создать новую ключевую ценность");
                        return;
                    }

                    keyValueAddable.addKeyValue(currentKeyValueGroup, newKeyValue);
                }
            });
    }
}
