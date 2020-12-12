package org.mchklv.finplan.client.ui.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.documentGen.DocumentGenerator;
import org.mchklv.finplan.client.documentGen.SpreadsheetDescriptor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class PredefinedExportNode extends VBox {
    private DataProvider dataProvider;
    private List<SpreadsheetDescriptor> predefSpreadsheetDescriptors;

    @FXML
    private Button exportButton;
    @FXML
    private ComboBox<Integer> firstYearComboBox;
    @FXML
    private ComboBox<String> exportFormatComboBox;
    @FXML
    private Label messageLabel;


    
    public PredefinedExportNode(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        UICustomUtils.loadControllerFXML(this, "fxml/export/predefinedExportNode.fxml");
        initPredefDescriptors();
        initChoiceBoxes();
        initButtons();
    }

    private void initChoiceBoxes() {
        for (int i = 0; i < predefinedFormatsArray.length; ++i) {
            exportFormatComboBox.getItems().add(predefinedFormatsArray[i]);
        }
        exportFormatComboBox.getSelectionModel().selectFirst();

        LocalDate currentDate = LocalDate.now();
        int diff = 10, currentYear = currentDate.getYear();
        for (int i = currentYear + diff; i >= currentYear - diff; --i) {
            firstYearComboBox.getItems().add(i);
        }
        firstYearComboBox.getSelectionModel().select((Integer) currentYear);
    }

    private void initPredefDescriptors() {
        if (predefSpreadsheetDescriptors == null) {
            predefSpreadsheetDescriptors = new ArrayList<>(5);
        }

        SpreadsheetDescriptor spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KVG_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_DESC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_THREATS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_REASONS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM, false);

        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS, false);
        
        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_YEARS);
        predefSpreadsheetDescriptors.add(spreadDesc);

        
        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setSpreadsheetType(SpreadsheetDescriptor.SpreadsheetTypes.PROGRAMMES_BUDGET);
        predefSpreadsheetDescriptors.add(spreadDesc);

        
        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KVG_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_DESC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_THREATS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_REASONS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM, false);

        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS, false);
        
        predefSpreadsheetDescriptors.add(spreadDesc);

        
        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_QUARTERS);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KVG_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_DESC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_THREATS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_REASONS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM, false);

        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS, false);
        
        predefSpreadsheetDescriptors.add(spreadDesc);

        
        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_MONTHS);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KVG_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_DESC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_THREATS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_REASONS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM, false);

        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS, false);
        
        predefSpreadsheetDescriptors.add(spreadDesc);

        
        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET, false);

        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS, false);
        
        predefSpreadsheetDescriptors.add(spreadDesc);


        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.KV_DESC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_THREATS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_REASONS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_NAME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.TASK_CONTENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET, false);

        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME, false);
        spreadDesc.getAdmEconColumnsList().set(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS, false);
        
        predefSpreadsheetDescriptors.add(spreadDesc);


        spreadDesc = new SpreadsheetDescriptor();
        spreadDesc.setFullExport(true);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET, false);
        spreadDesc.getColumnsList().set(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM, false);

        spreadDesc.setEventType(SpreadsheetDescriptor.EventTypes.BUDGET_BY_YEARS);
        
        predefSpreadsheetDescriptors.add(spreadDesc);
    }

    private void initButtons() {
        exportButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    messageLabel.setText("");
                    int selectedIndex = exportFormatComboBox.getSelectionModel().getSelectedIndex();
                    SpreadsheetDescriptor chosenDescriptor = predefSpreadsheetDescriptors.get(selectedIndex);
                    
                    LocalDate firstYearDate = LocalDate.of(firstYearComboBox.getSelectionModel().getSelectedItem(), 1, 1);
                    DocumentGenerator docGen = new DocumentGenerator(dataProvider, chosenDescriptor, firstYearDate);
                    
                    boolean isSuccess = UICustomUtils.tryExportToFile(docGen, PredefinedExportNode.this.getScene().getWindow());

                    if (isSuccess) {
                        messageLabel.setText("Файл успешно экспортирован!");
                    }
                }
            });
    }


    private String[] predefinedFormatsArray = {
        "Общий бюджет на 5 летний период плана управления",
        "Общий бюджет по программам",
        "Годовой бюджет по спецификам",
        "Календарный годовой рабочий план по кварталам",
        "Календарный годовой рабочий план по месяцам",
        "Общий бюджет по задачам",
        "Общий бюджет по ключевым ценностям",
        "Рабочий режим"
    };
}
