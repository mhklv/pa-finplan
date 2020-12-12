package org.mchklv.finplan.client.ui;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.Event;
import org.mchklv.finplan.common.FixedPointDec;
import org.mchklv.finplan.common.Task;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ActScreenEvent extends HBox implements EventBudgetNode.BudgetSumWatcher {
    interface ActScreenEventContainer {
        public void deleteEventNode(ActScreenEvent eventNode);
    }

    
    @FXML
    private TextArea eventTextArea;
    @FXML
    private Button deleteEventButton;
    @FXML
    private Button budgetButton;
    @FXML
    private Label sumLabel;
    

    private DataProvider dataProvider;
    private Event currentEvent;
    private boolean isEventChanged = false;
    private ActScreenEventContainer container;

    private boolean isAdmEconEvent;

    
    
    public ActScreenEvent(DataProvider dataProvider, Event event, boolean isAdmEconEvent, ActScreenEventContainer container) {
        this.isAdmEconEvent = isAdmEconEvent;
        this.dataProvider = dataProvider;
        this.currentEvent = event;
        this.container = container;
        UICustomUtils.loadControllerFXML(this, "fxml/actScreenActivity.fxml");
        setPadding(new Insets(40, 40, 40, 40));
        initFields();
        initSavingChanges();
        initButtons();
    }

    public Event getEvent() {
        return currentEvent;
    }
    

    private void initFields() {
        eventTextArea.setWrapText(true);
        eventTextArea.setText(currentEvent.getContent());
        sumLabel.setText(currentEvent.getExpensesSum().toString() + " ₸");
        eventTextArea.setMaxHeight(DPICustomUtils.fromPtToPx(80));
    }

    private void initButtons() {
        ImageView deleteButtonImageView = new ImageView(LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28)); 
        deleteEventButton.setGraphic(deleteButtonImageView);
        deleteEventButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    container.deleteEventNode(ActScreenEvent.this);
                }
            });

        ImageView budgetButtonImageView = new ImageView(LocalStorageManager.getImageResource("tenge_icon_32x41.png", 25, 32)); 
        budgetButton.setGraphic(budgetButtonImageView);
        budgetButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    NavigatableScene parentScene = (NavigatableScene) ActScreenEvent.this.getScene();
                    EventBudgetNode budgetNode = new EventBudgetNode(dataProvider, currentEvent, isAdmEconEvent);
                    budgetNode.setBudgetSumWatcher(ActScreenEvent.this);
                    parentScene.setNewRoot(budgetNode);
                }
            });
    }

    private void initSavingChanges() {
        eventTextArea.textProperty().addListener(new ChangeListener<String>() {
                
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isEventChanged = true;
                    currentEvent.setContent(newValue);
                }
            });

        eventTextArea.focusedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == false && isEventChanged) {
                        try {
                            if (isAdmEconEvent) {
                                dataProvider.updateAdmEconEvent(currentEvent);
                            }
                            else {
                                dataProvider.updateEvent(currentEvent);
                            }
                            isEventChanged = false;
                        }
                        catch (Exception e) {
                            System.err.println("Error " + e.getMessage());
                            e.printStackTrace();

                            UICustomUtils.showError("Ошибка сети",
                                                    "Проиошла ошибка при соединении с сервером при попытке сохранить мероприятие.");
                        }
                    }
                }
            });
    }


	public void updateSum(FixedPointDec sum) {
        sumLabel.setText(sum.toString() + " ₸");
	}
}
