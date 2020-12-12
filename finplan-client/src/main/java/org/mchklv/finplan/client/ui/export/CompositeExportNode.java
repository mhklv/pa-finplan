package org.mchklv.finplan.client.ui.export;

import java.time.LocalDate;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.documentGen.DocumentGenerator;
import org.mchklv.finplan.client.documentGen.SpreadsheetDescriptor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class CompositeExportNode extends BorderPane {
    private DataProvider dataProvider;
    
    @FXML
    private VBox checkBoxesContainer1;
    @FXML
    private VBox checkBoxesContainer2;
    @FXML
    private VBox admEconCheckBoxesContainer;
    @FXML
    private ComboBox<String> eventTypesCBox;
    @FXML
    private Button exportButton;
    @FXML
    private ComboBox<Integer> firstYearComboBox;
    @FXML
    private Label messageLabel;


    
    public CompositeExportNode(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        UICustomUtils.loadControllerFXML(this, "fxml/export/compositeExportNode.fxml");
        initCheckboxes();
        initEventTypesTitles();
        initComboBox();
        initButtons();
    }

    private void initCheckboxes() {
        int j = 0;
        for (;j < checkBoxesTitles.length; ++j) {
            if (j == 6) {
                break;
            }
            
            CheckBox newCheckBox = new CheckBox(checkBoxesTitles[j]);
            newCheckBox.setSelected(true);
            checkBoxesContainer1.getChildren().add(newCheckBox);
            VBox.setMargin(newCheckBox, new Insets(5, 0, 5, 0));
        }

        for (;j < checkBoxesTitles.length; ++j) {
            CheckBox newCheckBox = new CheckBox(checkBoxesTitles[j]);
            newCheckBox.setSelected(true);
            checkBoxesContainer2.getChildren().add(newCheckBox);
            VBox.setMargin(newCheckBox, new Insets(5, 0, 5, 0));
        }

        for (int i = 0; i < admEconCheckBoxesTitles.length; ++i) {
            CheckBox newCheckBox = new CheckBox(admEconCheckBoxesTitles[i]);
            newCheckBox.setSelected(true);
            admEconCheckBoxesContainer.getChildren().add(newCheckBox);
            VBox.setMargin(newCheckBox, new Insets(5, 0, 5, 0));
        }
    }

    private void initEventTypesTitles() {
        for (int i = 0; i < eventTypesTitles.length; ++i) {
            eventTypesCBox.getItems().add(eventTypesTitles[i]);
        }
        eventTypesCBox.getSelectionModel().select(1);
    }

    private void initComboBox() {
        LocalDate currentDate = LocalDate.now();
        int diff = 10, currentYear = currentDate.getYear();
        for (int i = currentYear + diff; i >= currentYear - diff; --i) {
            firstYearComboBox.getItems().add(i);
        }
        firstYearComboBox.getSelectionModel().select((Integer) currentYear);
    }

    private void initButtons() {
        exportButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    messageLabel.setText("");
                    SpreadsheetDescriptor spreadsheetDescriptor = new SpreadsheetDescriptor();
                    spreadsheetDescriptor.setEventType(eventTypesCBox.getSelectionModel().getSelectedIndex());

                    int j = 0, k = 0;
                    for (; j < checkBoxesContainer1.getChildren().size(); ++j) {
                        CheckBox currentCheckbox = (CheckBox) checkBoxesContainer1.getChildren().get(j);
                        spreadsheetDescriptor.getColumnsList().set(j + k + 1, currentCheckbox.isSelected());
                    }
                    for (; k < checkBoxesContainer2.getChildren().size(); ++k) {
                        CheckBox currentCheckbox = (CheckBox) checkBoxesContainer2.getChildren().get(k);
                        spreadsheetDescriptor.getColumnsList().set(j + k + 1, currentCheckbox.isSelected());
                    }

                    for (int i = 0; i < admEconCheckBoxesContainer.getChildren().size(); ++i) {
                        CheckBox currentCheckbox = (CheckBox) admEconCheckBoxesContainer.getChildren().get(i);
                        spreadsheetDescriptor.getAdmEconColumnsList().set(i, currentCheckbox.isSelected());
                    }

                    spreadsheetDescriptor.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM, false);

                    LocalDate firstYearDate = LocalDate.of(firstYearComboBox.getSelectionModel().getSelectedItem(), 1, 1);
                    DocumentGenerator docGen = new DocumentGenerator(dataProvider, spreadsheetDescriptor, firstYearDate);

                    boolean isSuccess = UICustomUtils.tryExportToFile(docGen, CompositeExportNode.this.getScene().getWindow());

                    if (isSuccess) {
                        messageLabel.setText("Файл успешно экспортирован!");
                    }
                }
            });
    }


    
    private String[] checkBoxesTitles = {
        "Бюджет по мероприятию",
        "Программа ООПТ",
        "Отдел ООПТ",
        "Источник финансирования",
        "Партнёры",
        "Мероприятие",
        "Задача",
        "Целевое значение индикаотра состояния",
        "Текущее значение индикатора состояния",
        "Индикатор состояния",
        "Проблемы",
        "Причины",
        "Угрозы",
        "Описание ключевой ценности",
        "Ключевая ценность",
        "Группа ключевых ценностей"
    };

    private String[] admEconCheckBoxesTitles = {
        // "Сумма бюджета по мероприятию (адм.-хоз.)",
        // "Бюджет по мероприятию (адм.-хоз.)",
        // "Программа ООПТ (адм.-хоз.)",
        // "Отдел ООПТ (адм.-хоз.)",
        // "Источник финансирования (адм.-хоз.)",
        // "Содержание мероприятия (адм.-хоз.)",
        "Целевое значение индикаотра состояния (адм.-хоз.)",
        "Текущее значение индикатора состояния (адм.-хоз.)",
        "Индикатор состояния (адм.-хоз.)",
        "Проблемы (адм.-хоз.)",
        "Задача (адм.-хоз.)"
    };

    private String[] eventTypesTitles = {
        "Бюджет по годам",
        "Бюджет по спецификам",
        "Мероприятия по кварталам",
        "Мероприятия по месяцам"
    };
}
