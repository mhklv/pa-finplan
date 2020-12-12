package org.mchklv.finplan.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.client.ui.DateIntervalNode.DateIntervalNodeContainer;
import org.mchklv.finplan.common.Event;
import org.mchklv.finplan.common.Expense;
import org.mchklv.finplan.common.ExpenseCategory;
import org.mchklv.finplan.common.FixedPointDec;
import org.mchklv.finplan.common.LocalDateInterval;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;



public class EventBudgetNode extends SplitPane implements DateIntervalNode.DateIntervalNodeContainer {
    interface BudgetSumWatcher {
        public void updateSum(FixedPointDec sum);
    }

    
    @FXML
    private Button backButton;
    @FXML
    private Button deleteExpenseButton;
    @FXML
    private Button newExpenseButton;
    @FXML
    private TableView<Expense> budgetTableView;
    @FXML
    private ComboBox<String> programmeChoiceBox;
    @FXML
    private ComboBox<String> departmentChoiceBox;
    // @FXML
    // private TableView<String> sumTableView;
    @FXML
    private TextArea eventTextArea;
    @FXML
    private Label sumLabel;
    @FXML
    private ComboBox<String> finSrcComboBox;
    @FXML
    private TextArea partnerOrgsArea;
    @FXML
    private Button addNewDateIntervalButton;
    @FXML
    private VBox dateIntervalsBox;
    

    private DataProvider dataProvider;
    private Event currentEvent;
    private ObservableList<Expense> expensesTableList;
    private ObservableList<ExpenseCategory> expenseCats;
    private ObservableList<String> expenseCatStrings;
    // private boolean isExpTableChanged = false;
    private Map<Expense, Boolean> isExpChangedList;
    private ObservableList<String> programmesList;
    private ObservableList<String> departmentsList;
    private ObservableList<String> finSrcsList;

    private BudgetSumWatcher sumWatcher;

    private boolean isAdmEconEvent;

    private boolean isPartnerOrgsChanged = false;

    
    
    private static String[] departmentsArray = {
        "Научный отдел",
        "Отдел экотуризма и просвещения",
        "Отдел охраны",
        "Отдел лесов и животного мира",
        "Финансовый отдел",
        "Администрация"
    };

    private static String[] finSourcesArray = {
        "Государственный бюджет",
        "Доходы ООПТ от оказания платных услуг в туристских и рекреационных целях",
        "Доходы ООПТ от ограниченной хозяйственной деятельности",
        "Платы за использование символики ООПТ",
        "Доходы от производства печатной, сувенирной и другой тиражированной продукции",
        "Гранты и другие средства из различных фондов развития ООПТ",
        "Добровольные взносы и пожертвования",
        "Поступления за ущерб ООПТ и объектам ГПЗФ",
        "Дефицит"
    };
    
    public EventBudgetNode(DataProvider dataProvider, Event event, boolean isAdmEconEvent) {
        this.isAdmEconEvent = isAdmEconEvent;
        this.dataProvider = dataProvider;
        this.currentEvent = event;
        UICustomUtils.loadControllerFXML(this, "fxml/eventBudgetNode.fxml");
        retrieveExpCats();
        initButtons();
        initTable();
        initSavingChanges();
        initEventProperties();
        initLayout();
    }

    public void setBudgetSumWatcher(BudgetSumWatcher watcher) {
        this.sumWatcher = watcher;
    }


    private void initLayout() {
        VBox.setVgrow(budgetTableView, Priority.ALWAYS);
    }
    
    private void initButtons() {
        backButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    NavigatableScene parentScene = (NavigatableScene) EventBudgetNode.this.getScene();
                    parentScene.setPrevoiusRoot();

                    for (Map.Entry<Expense, Boolean> pair : isExpChangedList.entrySet()) {
                        if (pair.getValue() == true) {
                            try {
                                if (isAdmEconEvent) {
                                    dataProvider.updateAdmEconExpense(pair.getKey());
                                }
                                else {
                                    dataProvider.updateExpense(pair.getKey());
                                }
                                pair.setValue(false);

                                if (sumWatcher != null) {
                                    System.out.println(currentEvent.getExpensesSum());
                                    sumWatcher.updateSum(currentEvent.getExpensesSum());
                                }
                            }
                            catch (Exception e) {
                                System.err.println("Error " + e.getMessage());
                                e.printStackTrace();

                                UICustomUtils.showError("Ошибка сети",
                                                        "Проиошла ошибка при соединении с сервером при попытке сохранить расход.");
                            }
                        }
                    }
                }
            });

        newExpenseButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    Expense newExp = new Expense(null, "", "",new FixedPointDec("0"),
                                                 new FixedPointDec("0"), 0);
                    try {
                        if (isAdmEconEvent) {
                            dataProvider.insertAdmEconExpense(newExp, currentEvent);
                        }
                        else {
                            dataProvider.insertExpense(newExp, currentEvent);
                        }
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке сохранить расход.");
                        return;
                    }

                    if (currentEvent.getExpenses() == null) {
                        currentEvent.setExpenses(new LinkedList<Expense>());
                    }
                    currentEvent.getExpenses().add(newExp);
                    expensesTableList.add(newExp);
                    isExpChangedList.put(newExp, false);
                }
            });

        // ImageView deleteButtonImageView = new ImageView(LocalStorageManager.getImageResource("delete_icon_32x32.png", 28, 28));
        // deleteRatUnitButton.setGraphic(deleteButtonImageView);
        deleteExpenseButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if (budgetTableView.getSelectionModel().isEmpty()) {
                        return;
                    }
                    
                    Expense exp = budgetTableView.getSelectionModel().getSelectedItem();
                    budgetTableView.itemsProperty().get().remove(exp);

                    try {
                        if (exp.getId() != null) {
                            if (isAdmEconEvent) {
                                dataProvider.deleteAdmEconExpense(exp);
                            }
                            else {
                                dataProvider.deleteExpense(exp);
                            }
                        }
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке удалить расход.");
                        return;
                    }

                    currentEvent.getExpenses().remove(exp);

                    if (sumWatcher != null) {
                        sumWatcher.updateSum(currentEvent.getExpensesSum());
                    }
                }
            });

        addNewDateIntervalButton.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    LocalDateInterval newDateInterval = new LocalDateInterval(null, null, null);
                    try {
                        if (isAdmEconEvent) {
                            dataProvider.insertAdmEconDateInterval(newDateInterval, currentEvent);
                        }
                        else {
                            dataProvider.insertEventDateInterval(newDateInterval, currentEvent);
                        }
                    }
                    catch (Exception e) {
                        System.err.println("Error " + e.getMessage());
                        e.printStackTrace();

                        UICustomUtils.showError("Ошибка сети",
                                                "Проиошла ошибка при соединении с сервером при попытке создать интервал.");
                        return;
                    }

                    if (currentEvent.getDateIntervals() == null) {
                        currentEvent.setDateIntervals(new ArrayList<LocalDateInterval>());
                    }
                    currentEvent.getDateIntervals().add(newDateInterval);
                    DateIntervalNode newDateIntervalNode = new DateIntervalNode(dataProvider, newDateInterval,
                            EventBudgetNode.this, isAdmEconEvent);
                    dateIntervalsBox.getChildren().add(newDateIntervalNode);
                }
            });
    }

    private void initSavingChanges() {
        budgetTableView.focusedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == false) {
                        for (Map.Entry<Expense, Boolean> pair : isExpChangedList.entrySet()) {
                            if (pair.getValue() == true) {
                                try {
                                    if (isAdmEconEvent) {
                                        dataProvider.updateAdmEconExpense(pair.getKey());
                                    }
                                    else {
                                        dataProvider.updateExpense(pair.getKey());
                                    }
                                    pair.setValue(false);

                                    if (sumWatcher != null) {
                                        System.out.println(currentEvent.getExpensesSum());
                                        sumWatcher.updateSum(currentEvent.getExpensesSum());
                                    }
                                }
                                catch (Exception e) {
                                    System.err.println("Error " + e.getMessage());
                                    e.printStackTrace();

                                    UICustomUtils.showError("Ошибка сети",
                                                            "Проиошла ошибка при соединении с сервером при попытке сохранить расход.");
                                }
                            }
                        }
                    }
                }
            });


        partnerOrgsArea.focusedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == false && isPartnerOrgsChanged) {
                        try {
                            showEventUpdateError();
                            isPartnerOrgsChanged = false;
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

    private void initTable() {
        if (currentEvent.getExpenses() == null) {
            currentEvent.setExpenses(new LinkedList<Expense>());
        }
        
        isExpChangedList = new HashMap<>();
        for (Expense exp : currentEvent.getExpenses()) {
            isExpChangedList.put(exp, false);
        }
        
        budgetTableView.setEditable(true);
        
        TableColumn<Expense, String> expNameCol = new TableColumn<>("Название расхода");
        expNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expNameCol.setPrefWidth(DPICustomUtils.fromPtToPx(105));
        expNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Expense,String>,ObservableValue<String>>(){

                @Override
                public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
                    return new ReadOnlyStringWrapper(cellDataFeatures.getValue().getName());
                }
            });
        expNameCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Expense,String>>() {

                @Override
                public void handle(CellEditEvent<Expense, String> event) {
                    isExpChangedList.replace(event.getRowValue(), true);
                    event.getRowValue().setName(event.getNewValue());
                }
            });

        TableColumn<Expense, String> expUnitCol = new TableColumn<>("Единица измерения");
        expUnitCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expUnitCol.setPrefWidth(DPICustomUtils.fromPtToPx(120));
        expUnitCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Expense,String>,ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
                    return new ReadOnlyStringWrapper(cellDataFeatures.getValue().getUnit());
                }
            });
        expUnitCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Expense,String>>() {

                @Override
                public void handle(CellEditEvent<Expense, String> event) {
                    isExpChangedList.replace(event.getRowValue(), true);
                    event.getRowValue().setUnit(event.getNewValue());;
                }
            });
        
        TableColumn<Expense, String> expAmountCol = new TableColumn<>("Количество");
        expAmountCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expAmountCol.setPrefWidth(DPICustomUtils.fromPtToPx(80));
        expAmountCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Expense,String>,ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
                    return new ReadOnlyStringWrapper(cellDataFeatures.getValue().getAmount().toString());
                }
            });
        expAmountCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Expense,String>>() {

                @Override
                public void handle(CellEditEvent<Expense, String> event) {
                    if (FixedPointDec.isValidDec(event.getNewValue())) {
                        isExpChangedList.replace(event.getRowValue(), true);
                        event.getRowValue().setAmount(new FixedPointDec(event.getNewValue()));
                    }
                    event.getTableView().refresh();
                    sumLabel.setText(currentEvent.getExpensesSum().toString());
                }
            });
        
        TableColumn<Expense, String> expUnitCostCol = new TableColumn<>("Стоимость единицы");
        expUnitCostCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expUnitCostCol.setPrefWidth(DPICustomUtils.fromPtToPx(120));
        expUnitCostCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Expense,String>,ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
                    return new ReadOnlyStringWrapper(cellDataFeatures.getValue().getUnitCost().toString());
                }
            });
        expUnitCostCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Expense,String>>() {

                @Override
                public void handle(CellEditEvent<Expense, String> event) {
                    if (FixedPointDec.isValidDec(event.getNewValue())) {
                        isExpChangedList.replace(event.getRowValue(), true);
                        event.getRowValue().setUnitCost(new FixedPointDec(event.getNewValue()));
                    }
                    event.getTableView().refresh();
                    sumLabel.setText(currentEvent.getExpensesSum().toString());
                }
            });

        TableColumn<Expense, String> expSumCost = new TableColumn<>("Сумма");
        expSumCost.setEditable(false);
        expSumCost.setPrefWidth(DPICustomUtils.fromPtToPx(60));
        expSumCost.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Expense,String>,ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
                    Expense exp = cellDataFeatures.getValue();
                    FixedPointDec curExpCost = exp.getAmount().multiplied(exp.getUnitCost());
                    return new ReadOnlyStringWrapper(curExpCost.toString());
                }
            });
        
        TableColumn<Expense, String> expCategoryCol = new TableColumn<>("Статья расхода");
        expCategoryCol.setCellFactory(ComboBoxTableCell.forTableColumn(expenseCatStrings));
        expCategoryCol.setPrefWidth(DPICustomUtils.fromPtToPx(100));
        expCategoryCol.setCellValueFactory(new Callback<TableColumn.
                                           CellDataFeatures<Expense,String>,ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
                    return new ReadOnlyStringWrapper(stringFromExpCatId(cellDataFeatures.getValue().getExpCatId()));
                }
            });
        
        expCategoryCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Expense,String>>() {

                @Override
                public void handle(CellEditEvent<Expense, String> event) {
                    if (event.getNewValue() != null) {
                        isExpChangedList.replace(event.getRowValue(), true);
                        event.getRowValue().setExpCatId(expCatFromString(event.getNewValue()).getId());
                    }
                }
            });

        budgetTableView.getColumns().addAll(expNameCol, expUnitCol, expAmountCol,
                                            expUnitCostCol, expSumCost, expCategoryCol);
        
        expensesTableList = FXCollections.<Expense>observableArrayList(currentEvent.getExpenses());
        
        budgetTableView.setItems(expensesTableList);

        sumLabel.setText(currentEvent.getExpensesSum().toString());
        sumLabel.setFont(Font.font("Sans", 10));
        
        // Sum table
        // sumTableView.setEditable(false);
        // sumTableView.setMaxHeight(50);

        // TableColumn<String, String> totalNameCol = new TableColumn<>("Всего");
        // totalNameCol.setPrefWidth(DPICustomUtils.fromPtToPx(150));
        // TableColumn<String, String> totalCol = new TableColumn<>(currentEvent.getExpensesSum().toString());
        // totalCol.setPrefWidth(DPICustomUtils.fromPtToPx(150));
        
        // sumTableView.getColumns().addAll(totalNameCol, totalCol);

        
        // expNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        // expNameCol.setPrefWidth(DPICustomUtils.fromPtToPx(105));
        // expNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Expense,String>,ObservableValue<String>>(){

        //         @Override
        //         public ObservableValue<String> call(CellDataFeatures<Expense, String> cellDataFeatures) {
        //             return new ReadOnlyStringWrapper(cellDataFeatures.getValue().getName());
        //         }
        //     });
    }

    private void initEventProperties() {
        setDividerPositions(0.65f, 0.35f);
        
        programmesList = FXCollections.observableArrayList(LocalStorageManager.getPAProgrammes(isAdmEconEvent));
        departmentsList = FXCollections.observableArrayList(departmentsArray);
        finSrcsList = FXCollections.observableArrayList(finSourcesArray);

        if (currentEvent.getProgramme() != null) {
            programmeChoiceBox.getSelectionModel().select(currentEvent.getProgramme());
        }
        programmeChoiceBox.setEditable(true);
        programmeChoiceBox.setItems(programmesList);
        programmeChoiceBox.setPrefWidth(DPICustomUtils.fromPtToPx(170));
        programmeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    currentEvent.setProgramme(newValue);
                    showEventUpdateError();
                    
                    LocalStorageManager.paProgrammeSelected(newValue);
                    LocalStorageManager.paProgrammeDeselected(oldValue);
                }
            });

        departmentChoiceBox.editableProperty().set(true);
        departmentChoiceBox.setItems(departmentsList);
        departmentChoiceBox.setPrefWidth(DPICustomUtils.fromPtToPx(170));
        departmentChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    currentEvent.setDepartment(newValue);
                    showEventUpdateError();
                }
            });

        finSrcComboBox.setItems(finSrcsList);
        finSrcComboBox.setPrefWidth(DPICustomUtils.fromPtToPx(170));
        finSrcComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    currentEvent.setFinSource(newValue);
                    showEventUpdateError();
                }
            });
        
        if (currentEvent.getDepartment() != null) {
            departmentChoiceBox.getSelectionModel().select(currentEvent.getDepartment());
        }
        if (currentEvent.getFinSource() != null) {
            finSrcComboBox.getSelectionModel().select(currentEvent.getFinSource());
        }
        
        eventTextArea.setWrapText(true);
        eventTextArea.setEditable(false);
        eventTextArea.setText(currentEvent.getContent());
        eventTextArea.setPrefRowCount(5);

        partnerOrgsArea.setWrapText(true);
        partnerOrgsArea.setText(currentEvent.getPartnerOrgs());
        partnerOrgsArea.setPrefRowCount(8);
        partnerOrgsArea.textProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    isPartnerOrgsChanged = true;
                    currentEvent.setPartnerOrgs(newValue);
                }
            });

        if (currentEvent.getDateIntervals() != null) {
            for (LocalDateInterval dateInterval : currentEvent.getDateIntervals()) {
                DateIntervalNode newIntervalNode = new DateIntervalNode(dataProvider, dateInterval,
                        EventBudgetNode.this, isAdmEconEvent);
                dateIntervalsBox.getChildren().add(newIntervalNode);
            }
        }
    }

    private void retrieveExpCats() {
        try {
            expenseCats = FXCollections.observableArrayList(dataProvider.getAllExpCats());
            expenseCatStrings = FXCollections.observableArrayList();

            // int a = 0;
            for (ExpenseCategory expCat : expenseCats) {
                // if (a > 5)
                    // break;
                expenseCatStrings.add(expCat.toString());
                // ++a;
            }
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                                    "Проиошла ошибка при соединении с сервером при попытке загрузить категории расходов.");
        }
    }

    private ExpenseCategory expCatFromString(String expCatString) {
        for (ExpenseCategory expCat : expenseCats) {
            if (expCatString.equals(expCat.toString())) {
                return expCat;
            }
        }

        return null;
    }

    private String stringFromExpCatId(int expCatId) {
        for (ExpenseCategory expCat : expenseCats) {
            if (expCatId == expCat.getId()) {
                return expCat.toString();
            }
        }

        return null;
    }

    private void showEventUpdateError() {
        try {
            if (isAdmEconEvent) {
                dataProvider.updateAdmEconEvent(currentEvent);
            }
            else {
                dataProvider.updateEvent(currentEvent);
            }
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            UICustomUtils.showError("Ошибка сети",
                                    "Проиошла ошибка при соединении с сервером при попытке сохранить мероприятие.");
        }
    }

	@Override
	public void deleteIntervalNode(DateIntervalNode dateIntervalNode) {
        boolean isRemoved = dateIntervalsBox.getChildren().remove(dateIntervalNode);
        if (isRemoved) {
            try {
                if (isAdmEconEvent) {
                    dataProvider.deleteAdmEconDateInterval(dateIntervalNode.getDateInterval());
                }
                else {
                    dataProvider.deleteEventDateInterval(dateIntervalNode.getDateInterval());
                }
            }
            catch (Exception e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();

                UICustomUtils.showError("Ошибка сети",
                                        "Проиошла ошибка при соединении с сервером при попытке удалить интервал мероприятия.");
                return;
            }
        }
        currentEvent.getDateIntervals().remove(dateIntervalNode.getDateInterval());
	}
}
