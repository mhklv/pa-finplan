package org.mchklv.finplan.client.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.ui.RationaleUnitNode.RatUnitContainer;
import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.KeyValueGroup;
import org.mchklv.finplan.common.RationaleUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ValueChosenRightNode extends ScrollPane implements RationaleUnitNode.RatUnitContainer {
    interface ChosenKeyValueContainer {
        public void deleteKeyValue(KeyValue keyValue);
        // public void createNewKeyValue(KeyValue keyValue, KeyValueGroup parentGroup);
        public void addKeyValue(KeyValueGroup parentGroup, KeyValue newValue);
        public void refreshTreeView();
    }
    
    @FXML
    private TextField keyValueNameField;
    @FXML
    private Button editKVNameButton;
    @FXML
    private Button deleteKVButton;
    @FXML
    private Button addRationaleUnitButton;
    @FXML
    private TextArea keyValueDescArea;
    @FXML
    private VBox rationaleUnitsBox;
    
    private DataProvider dataProvider;
    private KeyValue currentKeyValue;
    private boolean isKeyValueChanged = false;
    private ChosenKeyValueContainer container;
    private KeyValueGroup parentGroup;


    public ValueChosenRightNode(DataProvider dataProvider, KeyValue currentValue,
                                KeyValueGroup parentGroup, ChosenKeyValueContainer container) {
        this.dataProvider = dataProvider;
        this.currentKeyValue = currentValue;
        this.parentGroup = parentGroup;
        this.container = container;
        UICustomUtils.loadControllerFXML(this, "fxml/keyValueRightScreen.fxml");
        initFields();
        initButtons();
        initSavingChanges();
        initRationaleUnits();
        
        // setPrefSize(100, 100);
        // setFitToWidth(true);
        // setFitToHeight(true);
        // setCache(true);
    }

    private void initFields() {
        keyValueNameField.setText(currentKeyValue.getName());
        keyValueDescArea.setText(currentKeyValue.getDescription());
        keyValueDescArea.setWrapText(true);
    }

    private void initButtons() {
        editKVNameButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    currentKeyValue.setName(keyValueNameField.getText());
                    container.refreshTreeView();
                    

                    try {
                        dataProvider.updateKeyValue(currentKeyValue);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке сохранить ключевую ценность.");
                    }
                }
            });
        
        // createKVButton.setOnAction(new EventHandler<ActionEvent>() {

        //         @Override
        //         public void handle(ActionEvent event) {
        //             KeyValue newKeyValue = new KeyValue(null, "Новая ключевая ценность", "");
        //             try {
        //                 dataProvider.insertKeyValue(newKeyValue, parentGroup);
        //             }
        //             catch (Exception e) {
        //                 System.err.println("Error " + e.getMessage());
        //                 e.printStackTrace();

        //                 UICustomUtils.showError("Ошибка сети",
        //                         "Проиошла ошибка при соединении с сервером при попытке создать ключевую ценность.");
        //                 return;
        //             }
        //             // container.createNewKeyValue(parentGroup);
        //             container.addKeyValue(parentGroup, newKeyValue);
        //         }
        //     });

        deleteKVButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    container.deleteKeyValue(currentKeyValue);
                }
            });

        addRationaleUnitButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    RationaleUnit newRationaleUnit = new RationaleUnit(null, "", "", "");
                    try {
                        dataProvider.insertRationaleUnit(newRationaleUnit, currentKeyValue);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                "Произошла ошибка при соединении с сервером при попытке вставить блок обоснования.");
                        return;
                    }
                    if (currentKeyValue.getRationaleUnits() == null) {
                        currentKeyValue.setRationaleUnits(new LinkedList<RationaleUnit>());
                    }
                    currentKeyValue.getRationaleUnits().add(newRationaleUnit);
                    RationaleUnitNode newNode = new RationaleUnitNode(dataProvider, newRationaleUnit, ValueChosenRightNode.this);
                    rationaleUnitsBox.getChildren().add(0, newNode);
                }
            });
    }

    private void initSavingChanges() {
        keyValueNameField.textProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isKeyValueChanged = true;
                    currentKeyValue.setName(newValue);
                }
            });

        keyValueDescArea.textProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isKeyValueChanged = true;
                    currentKeyValue.setDescription(newValue);
                }
            });

        keyValueDescArea.focusedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == false && isKeyValueChanged) {
                        try {
                            dataProvider.updateKeyValue(currentKeyValue);
                            isKeyValueChanged = false;
                        }
                        catch (Exception e) {
                            System.err.println("Error " + e.getMessage());
                            e.printStackTrace();

                            UICustomUtils.showError("Ошибка сети",
                                                    "Проиошла ошибка при соединении с сервером при попытке сохранить ключевую ценность.");
                        }
                    }
                }
            });
            

        this.parentProperty().addListener(new ChangeListener<Parent>() {

                @Override
                public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
                    if (oldValue != null && newValue == null && isKeyValueChanged) {
                        System.out.println("Updting value");
                        try {
                            dataProvider.updateKeyValue(currentKeyValue);
                            isKeyValueChanged = false;
                        }
                        catch (Exception e) {
                            System.err.println("Error " + e.getMessage());
                            e.printStackTrace();

                            UICustomUtils.showError("Ошибка сети",
                                    "Проиошла ошибка при соединении с сервером при попытке сохранить ключевую ценность.");
                        }
                    }
                }
            });
    }

    private void initRationaleUnits() {
        List<RationaleUnit> units = currentKeyValue.getRationaleUnits();

        if (units != null) {
            for (RationaleUnit unit : units) {
                RationaleUnitNode unitNode = new RationaleUnitNode(dataProvider, unit, this);
                rationaleUnitsBox.getChildren().add(0, unitNode);
            }
        }
    }


	public void deleteRatUnit(RationaleUnitNode unitNode) {
        boolean isRemoved = rationaleUnitsBox.getChildren().remove(unitNode);
        if (isRemoved) {
            try {
                dataProvider.deleteRationaleUnit(unitNode.getRationaleUnit());
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Проиошла ошибка при соединении с сервером при попытке удалить задачу.");
                return;
            }

        }
        currentKeyValue.getRationaleUnits().remove(unitNode.getRationaleUnit());
	}
}
