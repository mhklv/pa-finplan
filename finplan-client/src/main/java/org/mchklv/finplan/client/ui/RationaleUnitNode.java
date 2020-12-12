package org.mchklv.finplan.client.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.ConditionIndicator;
import org.mchklv.finplan.common.RationaleUnit;
import org.mchklv.finplan.common.Task;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class RationaleUnitNode extends VBox implements TaskNode.TaskNodeContainer, CondIndicatorNode.CINodeContainer {
    interface RatUnitContainer {
        public void deleteRatUnit(RationaleUnitNode unitNode);
    }

    
    @FXML
    private TextArea threatArea;
    @FXML
    private TextArea threatReasonArea;
    @FXML
    private TextArea problemArea;
    @FXML
    private VBox condIndicatorsBox;
    @FXML
    private VBox tasksBox;
    @FXML
    private Button addNewTaskButton;
    @FXML
    private Button addNewIndicatorButton;
    @FXML
    private Button deleteRatUnitButton;
    
    private DataProvider dataProvider;
    private RationaleUnit currentRationaleUnit;
    private boolean isRatUnitChanged = false;
    private RatUnitContainer container;
    

    public RationaleUnitNode(DataProvider dataProvider, RationaleUnit currentRationaleUnit, RatUnitContainer container) {
        this.dataProvider = dataProvider;
        this.currentRationaleUnit = currentRationaleUnit;
        this.container = container;
        UICustomUtils.loadControllerFXML(this, "fxml/rationaleUnitNode.fxml");
        initFields();
        initCondIndicators();
        initTasks();
        initButtons();
        initSavingChanges();
    }

    public RationaleUnit getRationaleUnit() {
        return currentRationaleUnit;
    }
    
    
    private void initSavingChanges() {
        threatArea.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                isRatUnitChanged = true;
                currentRationaleUnit.setThreat(newValue);
		});

        threatReasonArea.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                isRatUnitChanged = true;
                currentRationaleUnit.setThreatReasons(newValue);
            });

        problemArea.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                isRatUnitChanged = true;
                currentRationaleUnit.setProblems(newValue);
            });

        ChangeListener<Boolean> unfocusedListener = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            if (newValue == false && isRatUnitChanged) {
                try {
                    dataProvider.updateRationaleUnit(currentRationaleUnit);
                    isRatUnitChanged = false;
                }
                catch (Exception e) {
                    System.err.println("Error " + e.getMessage());
                    e.printStackTrace();
                    
                    UICustomUtils.showError("Ошибка сети",
                                            "Проиошла ошибка при соединении с сервером при попытке сохранить блок обоснования.");
                }
            }
        };

        threatArea.focusedProperty().addListener(unfocusedListener);
        threatReasonArea.focusedProperty().addListener(unfocusedListener);
        problemArea.focusedProperty().addListener(unfocusedListener);
    }

    public void deleteTask(TaskNode taskNode) {
        boolean isRemoved = tasksBox.getChildren().remove(taskNode);
        if (isRemoved) {
            try {
                dataProvider.deleteTask(taskNode.getTask());
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Проиошла ошибка при соединении с сервером при попытке удалить задачу.");
                return;
            }
        }
        currentRationaleUnit.getTasks().remove(taskNode.getTask());
    }
    
    private void initFields() {
        threatArea.setWrapText(true);
        threatReasonArea.setWrapText(true);
        problemArea.setWrapText(true);
        
        threatArea.setText(currentRationaleUnit.getThreat());
        threatReasonArea.setText(currentRationaleUnit.getThreatReasons());
        problemArea.setText(currentRationaleUnit.getProblems());
    }

    private void initCondIndicators() {
        List<ConditionIndicator> indicators = currentRationaleUnit.getCondIndicators();

        if (indicators != null) {
            for (ConditionIndicator indicator : indicators) {
                CondIndicatorNode indicatorNode = new CondIndicatorNode(dataProvider, indicator, this, false);
                condIndicatorsBox.getChildren().add(0, indicatorNode);
            }
        }
    }

    private void initTasks() {
        List<Task> tasks = currentRationaleUnit.getTasks();

        if (tasks != null) {
            for (Task task : tasks) {
                TaskNode taskNode = new TaskNode(dataProvider, task, this);
                tasksBox.getChildren().add(0, taskNode);
            }
        }
    }

    private void initButtons() {
        ImageView deleteButtonImageView = new ImageView(LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28));
        deleteRatUnitButton.setGraphic(deleteButtonImageView);
        deleteRatUnitButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    container.deleteRatUnit(RationaleUnitNode.this);
                }
            });
        
        addNewTaskButton.setOnAction(new EventHandler<ActionEvent>(){
                
                @Override
                public void handle(ActionEvent event) {
                    Task newTask = new Task(null, "");
                    try {
                        dataProvider.insertTask(newTask, currentRationaleUnit);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке создать задачу.");
                        return;
                    }

                    if (currentRationaleUnit.getTasks() == null) {
                        currentRationaleUnit.setTasks(new LinkedList<Task>());
                    }
                    currentRationaleUnit.getTasks().add(newTask);
                    TaskNode newTaskNode = new TaskNode(dataProvider, newTask, RationaleUnitNode.this);
                    tasksBox.getChildren().add(0, newTaskNode);
                }
            });

        addNewIndicatorButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    ConditionIndicator newCondIndicator = new ConditionIndicator(null, "", "", "");
                    try {
                        dataProvider.insertCondIndicator(newCondIndicator, currentRationaleUnit);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке создать индикатор состояния.");
                        return;
                    }

                    // System.out.println(newCondIndicator.getId());

                    if (currentRationaleUnit.getCondIndicators() == null) {
                        currentRationaleUnit.setCondIndicators(new LinkedList<ConditionIndicator>());
                    }
                    currentRationaleUnit.getCondIndicators().add(newCondIndicator);
                    CondIndicatorNode newNode = new CondIndicatorNode(dataProvider, newCondIndicator, RationaleUnitNode.this, false);
                    condIndicatorsBox.getChildren().add(0, newNode);
                }
            });
    }

	@Override
	public void deleteCondIndicator(CondIndicatorNode cINode) {
        boolean isRemoved = condIndicatorsBox.getChildren().remove(cINode);
        if (isRemoved) {
            try {
                dataProvider.deleteCondIndicator(cINode.getCondIndicator());
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Проиошла ошибка при соединении с сервером при попытке удалить индикатор состояния.");
                return;
            }
            currentRationaleUnit.getCondIndicators().remove(cINode.getCondIndicator());
        }
	}
}
