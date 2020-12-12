package org.mchklv.finplan.client.ui;

import java.util.LinkedList;
import java.util.List;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.AdmEconTask;
import org.mchklv.finplan.common.KeyValue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;


public class AdmEconTasksScreenNode extends ScrollPane implements AdmEconTaskNode.AdmEconTaskNodeContainer {
    @FXML
    private VBox tasksBox;
    @FXML
    private Button addTaskButton;


    private DataProvider dataProvider;
    private List<AdmEconTask> tasksList;


    public AdmEconTasksScreenNode(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        UICustomUtils.loadControllerFXML(this, "fxml/admEconTasksScreenNode.fxml");

        initTaskNodes();
        initButtons();
    }

    public List<AdmEconTask> getAdmEconTasksList() {
        return tasksList;
    }

    private void initTaskNodes() {
        tasksList = retrieveAdmEconTasks();
        
        if (tasksList != null) {
            for (AdmEconTask task : tasksList) {
                AdmEconTaskNode taskNode = new AdmEconTaskNode(dataProvider, task, this);
                tasksBox.getChildren().add(0, taskNode);
            }
        }
    }

    private void initButtons() {
        addTaskButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    AdmEconTask newTask = new AdmEconTask(null, "", "");
                    try {
                        dataProvider.insertAdmEconTask(newTask);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке создать задачу.");
                        return;
                    }

                    AdmEconTaskNode taskNode = new AdmEconTaskNode(dataProvider, newTask, AdmEconTasksScreenNode.this);
                    tasksBox.getChildren().add(0, taskNode);
                    tasksList.add(newTask);
                }
            });
    }

    private List<AdmEconTask> retrieveAdmEconTasks() {
        List<AdmEconTask> tasksList;

        try {
            tasksList = dataProvider.getAllAdmEconTasks();
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                                    "Проиошла ошибка при соединении с сервером при попытке загрузить задачи.");
            return new LinkedList<AdmEconTask>();
        }


        return tasksList;
    }

	@Override
	public void deleteAdmEconTaskNode(AdmEconTaskNode taskNode) {
        boolean isRemoved = tasksBox.getChildren().remove(taskNode);
        if (isRemoved) {
            try {
                dataProvider.deleteAdmEconTask(taskNode.getAdmEconTask());
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Проиошла ошибка при соединении с сервером при попытке удалить задачу.");
                return;
            }

            tasksList.remove(taskNode.getAdmEconTask());
        }
	}
}
