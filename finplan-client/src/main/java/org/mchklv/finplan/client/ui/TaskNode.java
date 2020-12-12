package org.mchklv.finplan.client.ui;

import java.util.List;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.ConditionIndicator;
import org.mchklv.finplan.common.RationaleUnit;
import org.mchklv.finplan.common.Task;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class TaskNode extends VBox {
    interface TaskNodeContainer {
        public void deleteTask(TaskNode taskNode);
    }
    
    @FXML
    private TextArea taskArea;
    @FXML
    private Button deleteTaskButton;

    private DataProvider dataProvider;
    private Task currentTask;
    private TaskNodeContainer container;
    private boolean isTaskChanged = false;

    public TaskNode(DataProvider dataProvider, Task currentTask, TaskNodeContainer container) {
        this.dataProvider = dataProvider;
        this.currentTask = currentTask;
        this.container = container;
        UICustomUtils.loadControllerFXML(this, "fxml/taskNode.fxml");
        initFields();
        initButtons();
        initSavingChanges();
        initGraphic();
    }

    public Task getTask() {
        return currentTask;
    }

    private void initGraphic() {
        ImageView deleteButtonImageView = new ImageView(LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28));
        deleteTaskButton.setGraphic(deleteButtonImageView);
    }
    
    private void initFields() {
        taskArea.setWrapText(true);
        taskArea.setText(currentTask.getContent());
    }

    private void initButtons() {
        deleteTaskButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    container.deleteTask(TaskNode.this);
                }
            });
    }

    private void initSavingChanges() {
        taskArea.textProperty().addListener(new ChangeListener<String>() {
                
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isTaskChanged = true;
                    currentTask.setContent(newValue);
                }
            });

        taskArea.focusedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == false && isTaskChanged) {
                        try {
                            dataProvider.updateTask(currentTask);
                            isTaskChanged = false;
                        }
                        catch (Exception e) {
                            System.err.println("Error " + e.getMessage());
                            e.printStackTrace();

                            UICustomUtils.showError("Ошибка сети",
                                                    "Проиошла ошибка при соединении с сервером при попытке сохранить задачу.");
                        }
                    }
                }
            });
    }
}
