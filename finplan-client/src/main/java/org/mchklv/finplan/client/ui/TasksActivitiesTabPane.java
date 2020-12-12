package org.mchklv.finplan.client.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TasksActivitiesTabPane extends TabPane {
    private Tab rationaleTab, activitiesTab;

    
    public TasksActivitiesTabPane() {
        rationaleTab = new Tab("Обоснование");
        rationaleTab.setClosable(false);
        activitiesTab = new Tab("План действий");
        activitiesTab.setClosable(false);
        getTabs().addAll(rationaleTab, activitiesTab);
    }

    public void setRationaleNode(Node rationaleNode) {
        rationaleTab.setContent(rationaleNode);
    }

    public void setTasksScreenNode(TaskEventsScreen tasksNode) {
        activitiesTab.selectedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    tasksNode.refreshAnyTasks();
                }
            });
        
        activitiesTab.setContent(tasksNode);
    }
}
