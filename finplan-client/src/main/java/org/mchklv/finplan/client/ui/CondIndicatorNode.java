package org.mchklv.finplan.client.ui;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.ConditionIndicator;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;


public class CondIndicatorNode extends HBox {
    interface CINodeContainer {
        public void deleteCondIndicator(CondIndicatorNode cINode);
    }
    
    @FXML
    private TextField condIndicatorNameField;
    @FXML
    private TextField condIndicatorCurVal;
    @FXML
    private TextField condIndicatorTargVal;
    @FXML
    private Button deleteCIButton;

    private DataProvider dataProvider;
    private ConditionIndicator currentIndicator;
    private boolean isCIChanged = false;
    private CINodeContainer container;
    private boolean isAdmEconCI;
    
    
    public CondIndicatorNode(DataProvider dataProvider, ConditionIndicator currentIndicator,
                             CINodeContainer container, boolean isAdmEconCI) {
        this.dataProvider = dataProvider;
        this.currentIndicator = currentIndicator;
        this.container = container;
        this.isAdmEconCI = isAdmEconCI;
        UICustomUtils.loadControllerFXML(this, "fxml/condIndicatorNode.fxml");
        initFields();
        initButtons();
        initSavingChanges();
        initGraphic();
        // BooleanBinding showing = Bindings.selectBoolean(this.sceneProperty(), "window", "showing");
        // showing.addListener((obs, wasShowing, isNowShowing) -> {
        //         if (isNowShowing) {
        //             System.out.println("showing");
        //         } else {
        //             System.out.println("not showing");
        //         }
        //     });
    }
    
    public ConditionIndicator getCondIndicator() {
        return currentIndicator;
    }

    private void initGraphic() {
        ImageView deleteButtonImageView = new ImageView(LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28));
        deleteCIButton.setGraphic(deleteButtonImageView);
        
    }

    private void initFields() {
        condIndicatorNameField.setText(currentIndicator.getName());
        condIndicatorCurVal.setText(currentIndicator.getCurrentValue());
        condIndicatorTargVal.setText(currentIndicator.getTargetValue());
    }

    private void initButtons() {
        deleteCIButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    container.deleteCondIndicator(CondIndicatorNode.this);
                }
            });
    }

    private void initSavingChanges() {
        condIndicatorNameField.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            isCIChanged = true;
            currentIndicator.setName(newValue);
            System.out.println("IsChanged = " + isCIChanged + ". New value = " + newValue);
		});
        
        condIndicatorCurVal.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            isCIChanged = true;
            currentIndicator.setCurrentValue(newValue);
		});

        condIndicatorTargVal.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            isCIChanged = true;
            currentIndicator.setTargetValue(newValue);
		});
        
        ChangeListener<Boolean> unfocusedListener = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            System.out.println("Unfocused. Newvlaue = " + newValue);
            if (newValue == false && isCIChanged) {
                try {
                    if (isAdmEconCI) {
                        dataProvider.updateAdmEconCI(currentIndicator);
                    }
                    else {
                        dataProvider.updateCondIndicator(currentIndicator);
                    }
                    System.out.println("Updated CI. Current indicator.id = " + currentIndicator.getId());
                    System.out.println("Updated CI. Current indicator.name = " + currentIndicator.getName());
                    isCIChanged = false;
                }
                catch (Exception e) {
                    System.err.println("Error " + e.getMessage());
                    e.printStackTrace();
                    
                    UICustomUtils.showError("Ошибка сети",
                                            "Проиошла ошибка при соединении с сервером при попытке сохранить индикатор состояния.");
                }
            }
        };
        
        condIndicatorNameField.focusedProperty().addListener(unfocusedListener);
        condIndicatorCurVal.focusedProperty().addListener(unfocusedListener);
        condIndicatorTargVal.focusedProperty().addListener(unfocusedListener);
    }
}
