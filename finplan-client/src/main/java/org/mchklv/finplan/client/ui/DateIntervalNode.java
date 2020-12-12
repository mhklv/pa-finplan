package org.mchklv.finplan.client.ui;

import java.time.LocalDate;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.LocalDateInterval;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class DateIntervalNode extends HBox {
    interface DateIntervalNodeContainer {
        public void deleteIntervalNode(DateIntervalNode dateIntervalNode);
    }

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button deleteIntervalButton;

    private DataProvider dataProvider;
    private LocalDateInterval currentDateInterval;
    private DateIntervalNodeContainer container;

    private boolean isAdmEcon;


    public DateIntervalNode(DataProvider dataProvider, LocalDateInterval currentDateInterval,
                            DateIntervalNodeContainer container, boolean isAdmEcon) {
        this.dataProvider = dataProvider;
        this.currentDateInterval = currentDateInterval;
        this.container = container;
        this.isAdmEcon = isAdmEcon;
        UICustomUtils.loadControllerFXML(this, "fxml/dateIntervalNode.fxml");
        initFields();
        initSavingChanges();
        initButtons();
    }

    public LocalDateInterval getDateInterval() {
        return currentDateInterval;
    }

    private void initFields() {
        if (currentDateInterval.getStartDate() != null) {
            startDatePicker.setValue(currentDateInterval.getStartDate());
        }
        if (currentDateInterval.getEndDate() != null) {
            endDatePicker.setValue(currentDateInterval.getEndDate());
        }

        ImageView deleteIntervalImageView = new ImageView(
                LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28));
        deleteIntervalButton.setGraphic(deleteIntervalImageView);
    }

    private void initSavingChanges() {
        startDatePicker.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    LocalDate endDate = endDatePicker.getValue(), startDate = startDatePicker.getValue();
                    if (startDate == null) {
                        return;
                    }

                    if (endDate != null) {
                        if (startDate.isAfter(endDate)) {
                            startDatePicker.setValue(null);
                            UICustomUtils.showError("Предупреждение", "Дата конца мероприятия не должна предшествовать дате начала.");
                        }
                        else if (startDate.getYear() != endDate.getYear()) {
                            startDatePicker.setValue(null);
                            UICustomUtils.showError("Предупреждение", "Даты должны выбираться в пределах одного года.");
                        }
                        else {
                            currentDateInterval.setStartDate(startDate);
                            tryUpdateInterval();
                        }
                    }
                    else {
                        currentDateInterval.setStartDate(startDate);
                        tryUpdateInterval();
                    }
                }
            });
        endDatePicker.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    LocalDate endDate = endDatePicker.getValue(), startDate = startDatePicker.getValue();
                    if (endDate == null) {
                        return;
                    }

                    if (startDate != null) {
                        if (startDate.isAfter(endDate)) {
                            endDatePicker.setValue(null);
                            UICustomUtils.showError("Предупреждение", "Дата конца мероприятия не должна предшествовать дате начала.");
                        }
                        else if (startDate.getYear() != endDate.getYear()) {
                            endDatePicker.setValue(null);
                            UICustomUtils.showError("Предупреждение", "Даты в одном интервале должны выбираться в пределах одного года.");
                        }
                        else {
                            currentDateInterval.setEndDate(endDate);
                            tryUpdateInterval();
                        }
                    }
                    else {
                        currentDateInterval.setEndDate(endDate);
                        tryUpdateInterval();
                    }
                }
            });
    }

    private void initButtons() {
        deleteIntervalButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    container.deleteIntervalNode(DateIntervalNode.this);
                }
            });
    }
    
    private void tryUpdateInterval() {
        try {
            if (isAdmEcon) {
                dataProvider.updateAdmEconDateInterval(currentDateInterval);
            }
            else {
                dataProvider.updateEventDateInterval(currentDateInterval);
            }
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                                    "Проиошла ошибка при соединении с сервером при попытке сохранить интервалы.");
        }
    }
}
