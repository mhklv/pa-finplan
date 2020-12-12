package org.mchklv.finplan.client.ui;

import java.util.LinkedList;
import java.util.List;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.AdmEconTask;
import org.mchklv.finplan.common.Event;
import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.RationaleUnit;
import org.mchklv.finplan.common.Task;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class TaskEventsScreen extends HBox implements ActScreenEvent.ActScreenEventContainer {
    @FXML
    private VBox tasksBox;
    @FXML
    private VBox eventsBox;
    @FXML
    private Button createEventButton;
    @FXML
    private ScrollPane eventsScrollPane;

    private KeyValue currentKeyalue;
    private DataProvider dataProvider;
    private ActScreenTask selectedTaskNode;

    private boolean isAdmEconScreen;
    List<AdmEconTask> admEconTasks;

    private Background taskNodeSelectedBackground;
    private Background taskNodeNormalBackground;

    

    public TaskEventsScreen(DataProvider dataProvider, KeyValue currentKeyValue) {
        initBackgroundColors();
        isAdmEconScreen = false;
        this.dataProvider = dataProvider;
        this.currentKeyalue = currentKeyValue;
        UICustomUtils.loadControllerFXML(this, "fxml/tasksActivitiesScreen.fxml");
        createEventButton.setDisable(true);
        refreshTasks();
        initButtons();
    }

    public TaskEventsScreen(DataProvider dataProvider, List<AdmEconTask> admEconTasks) {
        initBackgroundColors();
        isAdmEconScreen = true;
        this.dataProvider = dataProvider;
        this.admEconTasks = admEconTasks;
        UICustomUtils.loadControllerFXML(this, "fxml/tasksActivitiesScreen.fxml");
        createEventButton.setDisable(true);
        refreshAdmEconTasks();
        initButtons();
    }

    private void initBackgroundColors() {
        taskNodeSelectedBackground = new Background(new BackgroundFill(
                                                        Color.color(0.81, 0.81, 0.81), null, null));
        // taskNodeNormalBackground = Background.EMPTY;
        taskNodeNormalBackground = new Background(new BackgroundFill(
                                                      Color.color(0.91, 0.91, 0.91), null, null));
    }

    public void refreshAnyTasks() {
        if (isAdmEconScreen) {
            refreshAdmEconTasks();
        }
        else {
            refreshTasks();
        }
        createEventButton.setDisable(true);
    }

    private void refreshAdmEconTasks() {
        tasksBox.getChildren().clear();
        eventsBox.getChildren().clear();

        for (AdmEconTask admEconTask : admEconTasks) {
            ActScreenTask taskNode = new ActScreenTask(admEconTask);
            tasksBox.getChildren().add(0, taskNode);
            taskNode.setBackground(taskNodeNormalBackground);
            taskNode.setFocusTraversable(false);
            taskNode.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        eventsBox.getChildren().clear();

                        if (taskNode.getAdmEconTask().getAdmEconEvents() != null) {
                            for (Event event : taskNode.getAdmEconTask().getAdmEconEvents()) {
                                ActScreenEvent eventNode = new ActScreenEvent(dataProvider, event, true, TaskEventsScreen.this);
                                eventsBox.getChildren().add(0, eventNode);
                            }
                        }
                            
                        createEventButton.setDisable(false);
                            
                        if (selectedTaskNode != null) {
                            selectedTaskNode.setBackground(taskNodeNormalBackground);
                        }
                        selectedTaskNode = taskNode;
                        selectedTaskNode.setBackground(taskNodeSelectedBackground);
                    }
                });
        }
    }
    
    private void refreshTasks() {
        tasksBox.getChildren().clear();
        eventsBox.getChildren().clear();

        if (currentKeyalue.getRationaleUnits() == null) {
            currentKeyalue.setRationaleUnits(new LinkedList<RationaleUnit>());
        }
        
        for (RationaleUnit unit : currentKeyalue.getRationaleUnits()) {
            if (unit.getTasks() != null) {
                for (Task task : unit.getTasks()) {
                    ActScreenTask taskNode = new ActScreenTask(task);
                    tasksBox.getChildren().add(0, taskNode);
                    taskNode.setBackground(taskNodeNormalBackground);
                    taskNode.setFocusTraversable(false);
                    taskNode.setOnMouseClicked(new EventHandler<MouseEvent>() {

                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                eventsBox.getChildren().clear();

                                if (taskNode.getTask().getEvents() == null) {
                                    try {
                                        taskNode.getTask().setEvents(dataProvider.getAllEvents(taskNode.getTask()));
                                    }
                                    catch (Throwable e) {
                                        System.err.println("Error " + e.getMessage());
                                        e.printStackTrace();
                                        UICustomUtils.showError("Ошибка сети",
                                                                "Проиошла ошибка при соединении с сервером при попытке загрузить мероприятия.");
                                    }
                                    finally {
                                        if (taskNode.getTask().getEvents() == null) {
                                            return;
                                        }
                                    }
                                }
                                    
                                for (Event event : taskNode.getTask().getEvents()) {
                                    ActScreenEvent eventNode = new ActScreenEvent(dataProvider, event, false, TaskEventsScreen.this);
                                    eventsBox.getChildren().add(0, eventNode);
                                }

                                createEventButton.setDisable(false);
                                
                                if (selectedTaskNode != null) {
                                    selectedTaskNode.setBackground(taskNodeNormalBackground);
                                }
                                selectedTaskNode = taskNode;
                                selectedTaskNode.setBackground(taskNodeSelectedBackground);
                            }
                        });
                }
            }
        }
    }
    
    private void initButtons() {
        createEventButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    if (isAdmEconScreen) {
                        Event newEvent = new Event(null, "", "", "");
                        newEvent.setFinSource("");
                        newEvent.setPartnerOrgs("");
                        AdmEconTask parentTask = selectedTaskNode.getAdmEconTask();

                        try {
                            dataProvider.insertAdmEconEvent(newEvent, parentTask);
                        }
                        catch (Exception e) {
                            System.err.println("Error " + e.getMessage());
                            e.printStackTrace();

                            UICustomUtils.showError("Ошибка сети",
                                                    "Проиошла ошибка при соединении с сервером при попытке загрузить мероприятие.");
                            return;
                        }

                        if (parentTask.getAdmEconEvents() == null) {
                            parentTask.setAdmEconEvents(new LinkedList<Event>());
                        }
                        
                        parentTask.getAdmEconEvents().add(newEvent);
                        ActScreenEvent newEventNode = new ActScreenEvent(dataProvider, newEvent, true, TaskEventsScreen.this);
                        eventsBox.getChildren().add(0, newEventNode);
                    }
                    else {
                        Event newEvent = new Event(null, "", "", "");
                        newEvent.setFinSource("");
                        newEvent.setPartnerOrgs("");
                        Task parentTask = selectedTaskNode.getTask();
                    
                        try {
                            dataProvider.insertEvent(newEvent, parentTask);
                        }
                        catch (Exception e) {
                            System.err.println("Error " + e.getMessage());
                            e.printStackTrace();

                            UICustomUtils.showError("Ошибка сети",
                                                    "Проиошла ошибка при соединении с сервером при попытке загрузить мероприятие.");
                            return;
                        }

                        parentTask.getEvents().add(newEvent);
                        ActScreenEvent newEventNode = new ActScreenEvent(dataProvider, newEvent, false, TaskEventsScreen.this);
                        eventsBox.getChildren().add(0, newEventNode);
                    }
                }
            });
    }

    private void showCurrentEvents(Task selectedTask) {
        
    }

	@Override
	public void deleteEventNode(ActScreenEvent eventNode) {
        boolean isRemoved = eventsBox.getChildren().remove(eventNode);
        if (isRemoved) {
            try {
                if (isAdmEconScreen) {
                    dataProvider.deleteAdmEconEvent(eventNode.getEvent());
                }
                else {
                    dataProvider.deleteEvent(eventNode.getEvent());
                }
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Произошла ошибка при соединении с сервером при попытке удалить мероприятие.");
                return;
            }
        }

        if (isAdmEconScreen) {
            selectedTaskNode.getAdmEconTask().getAdmEconEvents().remove(eventNode.getEvent());
        }
        else {
            selectedTaskNode.getTask().getEvents().remove(eventNode.getEvent());
        }
	}
}
