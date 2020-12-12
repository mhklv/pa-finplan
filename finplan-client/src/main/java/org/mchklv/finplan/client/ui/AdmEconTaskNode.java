package org.mchklv.finplan.client.ui;

import java.util.LinkedList;
import java.util.List;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.AdmEconTask;
import org.mchklv.finplan.common.ConditionIndicator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdmEconTaskNode extends VBox implements CondIndicatorNode.CINodeContainer {
    interface AdmEconTaskNodeContainer {
        public void deleteAdmEconTaskNode(AdmEconTaskNode taskNode);
    }

    
    @FXML
    private TextArea taskContentArea;
    @FXML
    private Button addNewIndicatorButton;
    @FXML
    private VBox condIndicatorsBox;
    @FXML
    private Button deleteTaskButton;
    @FXML
    private TextArea taskProblemsArea;

    private DataProvider dataProvider;
    private AdmEconTask currentAdmEconTask;
    private boolean isTaskChanged = false;
    private AdmEconTaskNodeContainer container;

    
    
    public AdmEconTaskNode(DataProvider dataProvider, AdmEconTask currentAdmEconTask, AdmEconTaskNodeContainer container) {
        this.dataProvider = dataProvider;
        this.currentAdmEconTask  = currentAdmEconTask;
        this.container = container;
        UICustomUtils.loadControllerFXML(this, "fxml/admEconTaskNode.fxml");
        initFields();
        initCondIndicators();
        initButtons();
        initSavingChanges();
    }

    public void deleteCondIndicator(CondIndicatorNode cINode) {
        boolean isRemoved = condIndicatorsBox.getChildren().remove(cINode);
        if (isRemoved) {
            try {
                dataProvider.deleteAdmEconCI(cINode.getCondIndicator());
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Проиошла ошибка при соединении с сервером при попытке удалить индикатор состояния.");
                return;
            }
            currentAdmEconTask.getAdmEconCondIndicators().remove(cINode.getCondIndicator());
        }
    }

    public AdmEconTask getAdmEconTask() {
        return currentAdmEconTask;
    }
    

    private void initSavingChanges() {
        taskContentArea.textProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isTaskChanged = true;
                    currentAdmEconTask.setContent(newValue);
                }
            });

        taskProblemsArea.textProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isTaskChanged = true;
                    currentAdmEconTask.setProblems(newValue);
                }
            });

        ChangeListener<Boolean> unfocusedListener = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            if (newValue == false && isTaskChanged) {
                try {
                    dataProvider.updateAdmEconTask(currentAdmEconTask);
                    isTaskChanged = false;
                }
                catch (Exception e) {
                    System.err.println("Error " + e.getMessage());
                    e.printStackTrace();
                    
                    UICustomUtils.showError("Ошибка сети",
                                            "Проиошла ошибка при соединении с сервером при попытке сохранить блок обоснования.");
                }
            }
        };

        taskContentArea.focusedProperty().addListener(unfocusedListener);
        taskProblemsArea.focusedProperty().addListener(unfocusedListener);
    }

    private void initButtons() {
        ImageView deleteButtonImageView = new ImageView(LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28));
        deleteTaskButton.setGraphic(deleteButtonImageView);
        deleteTaskButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    container.deleteAdmEconTaskNode(AdmEconTaskNode.this);
                }
            });

        
        addNewIndicatorButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    ConditionIndicator newCondIndicator = new ConditionIndicator(null, "", "", "");
                    try {
                        dataProvider.insertAdmEconCI(newCondIndicator, currentAdmEconTask);
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке создать индикатор состояния.");
                        return;
                    }

                    if (currentAdmEconTask.getAdmEconCondIndicators() == null) {
                        currentAdmEconTask.setAdmEconCondIndicators(new LinkedList<ConditionIndicator>());
                    }
                    currentAdmEconTask.getAdmEconCondIndicators().add(newCondIndicator);
                    CondIndicatorNode newNode = new CondIndicatorNode(dataProvider, newCondIndicator, AdmEconTaskNode.this, true);
                    condIndicatorsBox.getChildren().add(newNode);
                }
            });
    }

    private void initCondIndicators() {
        List<ConditionIndicator> indicators = currentAdmEconTask.getAdmEconCondIndicators();
        if (indicators != null) {
            for (ConditionIndicator indicator : indicators) {
                CondIndicatorNode indicatorNode = new CondIndicatorNode(dataProvider, indicator, this, true);
                condIndicatorsBox.getChildren().add(indicatorNode);
            }
        }
    }

    private void initFields() {
        HBox.setHgrow(taskContentArea, Priority.ALWAYS);
        taskContentArea.setMaxWidth(DPICustomUtils.fromPtToPx(390));
        taskContentArea.setMaxHeight(DPICustomUtils.fromPtToPx(50));
        taskContentArea.setWrapText(true);
        taskContentArea.setText(currentAdmEconTask.getContent());
        
        HBox.setHgrow(taskProblemsArea, Priority.ALWAYS);
        taskProblemsArea.setMaxWidth(DPICustomUtils.fromPtToPx(390));
        taskProblemsArea.setMaxHeight(DPICustomUtils.fromPtToPx(50));
        taskProblemsArea.setWrapText(true);
        taskProblemsArea.setText(currentAdmEconTask.getProblems());
    }
}
