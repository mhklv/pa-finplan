package org.mchklv.finplan.client.documentGen;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.text.AttributedString;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.ui.EventBudgetNode;
import org.mchklv.finplan.common.AdmEconTask;
import org.mchklv.finplan.common.ConditionIndicator;
import org.mchklv.finplan.common.Event;
import org.mchklv.finplan.common.Expense;
import org.mchklv.finplan.common.ExpenseCategory;
import org.mchklv.finplan.common.FixedPointDec;
import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.KeyValueGroup;
import org.mchklv.finplan.common.LocalDateInterval;
import org.mchklv.finplan.common.RationaleUnit;
import org.mchklv.finplan.common.Task;


public class DocumentGenerator {
    private DataProvider dataProvider;
    private SpreadsheetDescriptor spreadDesc;
    private LocalDate firstYearDate;

    // private XSSFWorkbook wb;

    private List<ExpenseCategory> expCats;


    private CellStyle greyBackgroundCellStyle;
    private CellStyle wrapTextCellStyle;
    private CellStyle wrapHorizontalCenterStyle;
    private Font boldFont;
    

    
    public DocumentGenerator() {
        
    }

    public DocumentGenerator(DataProvider dataProvider, SpreadsheetDescriptor spreadDesc,
                             LocalDate firstYearDate) {
        this.setFirstYearDate(firstYearDate);
        this.setDataProvider(dataProvider);
        this.setSpreadDesc(spreadDesc);
    }

    
    public void generateGeneralPlan(OutputStream out)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        if (dataProvider == null || spreadDesc == null) {
            // return false;
            throw new IllegalStateException("Document generator should have DataProvider and SpreadSHeetDescriptor defined.");
        }
        
        XSSFWorkbook wb = new XSSFWorkbook();
        // XSSFSheet planSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("План"));

        if (greyBackgroundCellStyle == null) {
            greyBackgroundCellStyle = createGreyCellStyle(wb);
        }

        if (wrapTextCellStyle == null) {
            wrapTextCellStyle = wb.createCellStyle();
            wrapTextCellStyle.setWrapText(true);
            wrapTextCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }

        if (wrapHorizontalCenterStyle == null) {
            wrapHorizontalCenterStyle = wb.createCellStyle();
            wrapHorizontalCenterStyle.setWrapText(true);
            wrapHorizontalCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            wrapHorizontalCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        }

        if (boldFont == null) {
            boldFont = wb.createFont();
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setFontName("Calibri");
            boldFont.setBold(true);
        }

        List<KeyValueGroup> groups = dataProvider.getAllKeyValueGroups();
        // List<KeyValue> keyValues = new LinkedList<KeyValue>();
        for (KeyValueGroup group : groups) {
            LinkedList<KeyValue> tmpValues = dataProvider.getAllKeyValues(group);
            
            for (KeyValue keyValue : tmpValues) {
                if (keyValue.getRationaleUnits() != null) {
                    for (RationaleUnit ru : keyValue.getRationaleUnits()) {
                        if (ru.getTasks() != null) {
                            for (Task task : ru.getTasks()) {
                                task.setEvents(dataProvider.getAllEvents(task));
                            }
                        }
                    }
                }
            }
            
            group.setKeyValues(tmpValues);
        }

        List<AdmEconTask> admEconTasks = dataProvider.getAllAdmEconTasks();
        
        switch (spreadDesc.getSpreadsheetType()) {
            case SpreadsheetDescriptor.SpreadsheetTypes.GENERAL_BUDGET: {
                if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET) &&
                    (spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS ||
                     spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_QUARTERS ||
                     spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_MONTHS)) {
                    for (int i = 0; i < 5; ++i) {
                        Integer year = firstYearDate.getYear() + i;
                        XSSFSheet newSheet = wb.createSheet(WorkbookUtil.createSafeSheetName(year.toString() + " год"));
                        int tableFirstRow = writeHeadersOnSheet(newSheet, 0, 0, false);

                        int currentRow = 0;
                        for (int j = 0; j < groups.size(); ++j) {
                            KeyValueGroup group = groups.get(j);
                            if (group.getKeyValues() != null && !group.getKeyValues().isEmpty()) {
                                CellRangeAddress rangeAddress =
                                    writeKeyValueGroupOnSheet(newSheet, group, 0, currentRow + tableFirstRow, year);
                                currentRow += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                            }
                        }

                        if (spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS &&
                            spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                            deleteZeroExpCats(newSheet);
                            addSumsToSheet(newSheet);
                        }

                        setupFiltersEnvir(newSheet);
                    }

                    for (int i = 0; i < 5; ++i) {
                        Integer year = firstYearDate.getYear() + i;
                        XSSFSheet admEconSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Адм.-хоз. " + year.toString() + " год"));
                        int tableFirstRow = writeHeadersOnSheet(admEconSheet, 0, 0, true);

                        int currentRow = 0;
                        for (int j = 0; j < admEconTasks.size(); ++j) {
                            AdmEconTask task = admEconTasks.get(j);
                            // TODO: check if this is necessery
                            // if (task.getAdmEconEvents() != null && task.getAdmEconCondIndicators() != null) {
                            CellRangeAddress rangeAddress = writeAdmEconTasksOnSheet(admEconSheet, task,
                                                                                     0, currentRow + tableFirstRow, year);
                            currentRow += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                            // }
                        }

                        if (spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS &&
                            spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                            deleteZeroExpCats(admEconSheet);
                            addSumsToSheet(admEconSheet);
                        }
                        
                        setupFiltersAdmEcon(admEconSheet);
                    }
                }
                else {
                    XSSFSheet newSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Природоохранная деятельность"));
                    int tableFirstRow = writeHeadersOnSheet(newSheet, 0, 0, false);

                    int currentRow = 0;
                    for (int j = 0; j < groups.size(); ++j) {
                        KeyValueGroup group = groups.get(j);
                        if (group.getKeyValues() != null && !group.getKeyValues().isEmpty()) {
                            CellRangeAddress rangeAddress =
                                writeKeyValueGroupOnSheet(newSheet, group, 0, currentRow + tableFirstRow, null);
                            currentRow += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                        }
                    }
                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        addSumsToSheet(newSheet);
                    }
                    
                    setupFiltersEnvir(newSheet);


                    XSSFSheet admEconSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Административно-хозяйственная деятельность"));
                    tableFirstRow = writeHeadersOnSheet(admEconSheet, 0, 0, true);
                
                    currentRow = 0;
                    for (int j = 0; j < admEconTasks.size(); ++j) {
                        AdmEconTask task = admEconTasks.get(j);
                        // TODO: check if this is necessery
                        // if (task.getAdmEconEvents() != null && task.getAdmEconCondIndicators() != null) {
                        CellRangeAddress rangeAddress = writeAdmEconTasksOnSheet(admEconSheet, task, 0, currentRow + tableFirstRow,
                                                                                     null);
                        currentRow += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                        // }
                    }

                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        addSumsToSheet(admEconSheet);
                    }
                    
                    setupFiltersAdmEcon(admEconSheet);
                }
                
                break;
            }

            case SpreadsheetDescriptor.SpreadsheetTypes.PROGRAMMES_BUDGET: {
                XSSFSheet newSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("План"));
                int tableFirstRow = writeHeadersOnSheet(newSheet, 0, 0, false);
                writeProgrammesBudgetOnSheet(newSheet, groups, admEconTasks,  0, tableFirstRow);
                setupFiltersEnvir(newSheet);
                
                break;
            }
        }
        
        writePlan(out, wb);
        
        // return true;
    }
    
    public SpreadsheetDescriptor getSpreadDesc() {
		return spreadDesc;
	}

	public void setSpreadDesc(SpreadsheetDescriptor spreadDesc) {
		this.spreadDesc = spreadDesc;
	}

	public DataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}


    private void writeProgrammesBudgetOnSheet(Sheet sheet, List<KeyValueGroup> groups,
                                              List<AdmEconTask> admEconTasks, int startColNum, int startRowNum) {
        
        List<Map<String, FixedPointDec>> progBudget = new ArrayList<Map<String, FixedPointDec>>();
        for (int i = 0; i < 5; ++i) {
            progBudget.add(new HashMap<String, FixedPointDec>());
        }

        for (KeyValueGroup group : groups) {
            if (group.getKeyValues() == null) {
                continue;
            }
            for (KeyValue keyValue : group.getKeyValues()) {
                if (keyValue.getRationaleUnits() == null) {
                    continue;
                }
                for (RationaleUnit ru : keyValue.getRationaleUnits()) {
                    if (ru.getTasks() == null) {
                        continue;
                    }
                    for (Task task : ru.getTasks()) {
                        if (task.getEvents() == null) {
                            continue;
                        }
                        for (Event event : task.getEvents()) {
                            if (event.getExpenses() == null || event.getDateIntervals() == null) {
                                continue;
                            }
                            List<Integer> yearDiffs = eventStartYearDifferences(event, firstYearDate.getYear());
                            
                            // if (event.getPlannedStartDate().getYear() - firstYearDate.getYear() > 4 ||
                            //     event.getPlannedStartDate().getYear() - firstYearDate.getYear() < 0) {
                            //     continue;
                            // }
                            
                            for (Integer yearDiff : yearDiffs) {
                                if (yearDiff >= 0 && yearDiff < 5) {
                                    for (Expense exp : event.getExpenses()) {
                                        Map<String, FixedPointDec> currentMap = progBudget.get(yearDiff);
                                        currentMap.put(event.getProgramme(), currentMap.getOrDefault(event.getProgramme(), new FixedPointDec("0")).added(exp.getAmount().multiplied(exp.getUnitCost())));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (AdmEconTask admEconTask : admEconTasks) {
            if (admEconTask.getAdmEconEvents() == null) {
                continue;
            }
            for (Event event : admEconTask.getAdmEconEvents()) {
                if (event.getExpenses() == null || event.getDateIntervals() == null) {
                    continue;
                }
                List<Integer> yearDiffs = eventStartYearDifferences(event, firstYearDate.getYear());
                            
                // if (event.getPlannedStartDate().getYear() - firstYearDate.getYear() > 4 ||
                //     event.getPlannedStartDate().getYear() - firstYearDate.getYear() < 0) {
                //     continue;
                // }
                            
                for (Integer yearDiff : yearDiffs) {
                    if (yearDiff >= 0 && yearDiff < 5) {
                        for (Expense exp : event.getExpenses()) {
                            Map<String, FixedPointDec> currentMap = progBudget.get(yearDiff);
                            currentMap.put(event.getProgramme(), currentMap.getOrDefault(event.getProgramme(), new FixedPointDec("0")).added(exp.getAmount().multiplied(exp.getUnitCost())));
                        }
                    }
                }
            }
        }

        // getCell(sheet, startColNum, startRowNum).setCellValue("Программа ООПТ");
        // getCell(sheet, startColNum + 1, startRowNum).setCellValue("Общий бюджет");
        // sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + 1, startColNum, startColNum));
        // sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, startColNum + 1, startColNum + 5));
        // for (int i = 1; i <= 5; ++i) {
        //     getCell(sheet, startColNum + i, startRowNum + 1).setCellValue(i + " год");
        // }

        String[] programmesArray = new String[]{
                "Административно-хозяйственное обеспечение",
                "Обеспечение охраны и предотвращение незаконных видов деятельности на территории ООПТ",
                "Обеспечение противопожарной безопасности на территории ООПТ",
                "Охрана, восстановление и разведение лесов",
                "Охрана животного мира",
                "Управление видами",
                "Управление популяциями",
                "Управление экосистемами и местообитаниями",
                "Проведение НИР",
                "Реинтродукции видов",
                "Проведение системного мониторинга в ООПТ",
                "Хозяйственная деятельность",
                "Информирование и вовлечение заинтересованных сторон",
                "Развитие экологического туризма и рекреации"
        };

        for (int i = 0; i < progBudget.size(); ++i) {
            for (int j = 0; j < programmesArray.length; ++j) {
                String programme = programmesArray[j];
                getCell(sheet, startColNum + i + 1, startRowNum + j).
                        setCellValue(progBudget.get(i).getOrDefault(programme, new FixedPointDec("0")).doubleValue());
            }
        }

        for (int i = 0; i < programmesArray.length; ++i) {
            getCell(sheet, startColNum, startRowNum + i).setCellValue(programmesArray[i]);
        }
    }
    
    private boolean writePlan(OutputStream out, XSSFWorkbook wb) {
        if (wb == null) {
            return false;
        }
        try {
            wb.write(out);
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    private Cell getCell(Sheet sheet, int colNum, int rowNum) {        
        Row chosenRow = sheet.getRow(rowNum);
        if (chosenRow == null) {
            chosenRow = sheet.createRow(rowNum);
        }

        Cell chosenCell = chosenRow.getCell(colNum);
        if (chosenCell == null) {
            chosenCell = chosenRow.createCell(colNum);
        }

        return chosenCell;
    }

    private int computeColumnsCount() {
        if (spreadDesc == null) {
            return 0;
        }
        
        int c = 0, i = 0;
        for (Boolean isColPresent : spreadDesc.getColumnsList()) {
            if (isColPresent) {
                c += SpreadsheetDescriptor.RECORD_COLSPAN[i];
            }
            ++i;
        }

        return c;
    }

    
    // Returns cell range which the record spans.
    private CellRangeAddress writeKeyValueGroupOnSheet(Sheet sheet, KeyValueGroup group,
                                                       int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        int rowSpan = 0, maxColSpan = 0, colSpan = 0, colOffset = 0;
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.KVG_NAME)) {
            ++colOffset;
        }
        
        if (group.getKeyValues() != null) {
            for (KeyValue keyValue : group.getKeyValues()) {
                CellRangeAddress rangeAddress = writeKeyValueOnSheet(sheet, keyValue,
                        startColNum + colOffset, startRowNum + rowSpan, year);
                colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
            
                if (colSpan > maxColSpan) {
                    maxColSpan = colSpan;
                }

                rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
            }
            colSpan = maxColSpan;
        }

        if (rowSpan > 0 || spreadDesc.isFullExport()) {
            colOffset = 0;
            if (rowSpan == 0) {
                rowSpan = 1;
            }
            
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.KVG_NAME)) {
                String cellContent = group.getName().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(cellContent);

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }
                
                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }
        }
        else {
            colOffset = 0;
        }
        
        colSpan = colSpan + colOffset;

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeKeyValueOnSheet(Sheet sheet, KeyValue keyValue,
                                                  int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        int rowSpan = 0, maxColSpan = 0, colSpan = 0, colOffset = 0;

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.KV_NAME)) {
            ++colOffset;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.KV_DESC)) {
            ++colOffset;
        }

        if (keyValue.getRationaleUnits() != null) {
            for (RationaleUnit ratUnit : keyValue.getRationaleUnits()) {
                CellRangeAddress rangeAddress = writeRationaleUnitOnSheet(sheet, ratUnit,
                        startColNum + colOffset, startRowNum + rowSpan, year);
                // colSpan = writeEventOnSheet(sheet, event, startColNum + colOffset, startRowNum + rowSpan);
                colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
            
                if (colSpan > maxColSpan) {
                    maxColSpan = colSpan;
                }

                rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
            }
            colSpan = maxColSpan;
        }

        if (rowSpan > 0 || spreadDesc.isFullExport()) {
            colOffset = 0;
            if (rowSpan == 0) {
                rowSpan = 1;
            }
            
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.KV_NAME)) {
                String cellContent = keyValue.getName().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapHorizontalCenterStyle);
                    cell.setCellValue(cellContent);

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }
        
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.KV_DESC)) {
                String cellContent = keyValue.getDescription().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(keyValue.getDescription().trim());

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }

            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM) &&
                lastComponentNum() == 4) {
                for (int i = 0; i < 5; ++i) {
                    FixedPointDec sum = computeExpensesSum(keyValue, firstYearDate.getYear() + i);
                
                    for (int j = startRowNum; j < startRowNum + rowSpan; ++j) {
                        Cell cell = getCell(sheet, startColNum + colOffset, j);
                        cell.setCellStyle(wrapTextCellStyle);
                        cell.setCellValue(sum.doubleValue());
                    }

                    if (rowSpan > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                    }

                    ++colOffset;
                }
            }
        }
        else {
            colOffset = 0;
        }
        
        colSpan = colSpan + colOffset;

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeRationaleUnitOnSheet(Sheet sheet, RationaleUnit ratUnit,
                                                       int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        int rowSpan = 0, maxColSpan = 0, colSpan = 0, colOffset = 0;

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.RU_THREATS)) {
            ++colOffset;
        }
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.RU_REASONS)) {
            ++colOffset;
        }
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS)) {
            ++colOffset;
        }
        
        int ciColSpan = 0;
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.CI_NAME)) {
            ++ciColSpan;
        }
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL)) {
            ++ciColSpan;
        }
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL)) {
            ++ciColSpan;
        }

        if (ratUnit.getTasks() != null) {
            for (Task task : ratUnit.getTasks()) {
                CellRangeAddress rangeAddress = writeTaskOnSheet(sheet, task,
                        startColNum + ciColSpan + colOffset, startRowNum + rowSpan, year);
                colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
                // colSpan = writeCIOnSheet(sheet, condIndicator, startColNum + colOffset, startRowNum + rowSpan);
            
                if (colSpan > maxColSpan) {
                    maxColSpan = colSpan;
                }
                
                rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                
                // System.out.println(task.getContent());
                // System.out.println(rowSpan);
            }
            colSpan = maxColSpan;
        }
        int tasksRowSpan = rowSpan;
        int tasksColSpan = colSpan;

        rowSpan = 0;
        colSpan = 0;
        maxColSpan = 0;

        if (ratUnit.getCondIndicators() != null && tasksRowSpan > 0) {
            for (ConditionIndicator condIndicator : ratUnit.getCondIndicators()) {
                CellRangeAddress rangeAddress = writeCIOnSheet(sheet, condIndicator, startColNum + colOffset, startRowNum + rowSpan);
                colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
            
                if (colSpan > maxColSpan) {
                    maxColSpan = colSpan;
                }
            
                rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
            }
            colSpan = maxColSpan;
        }
        int ciRowSpan = rowSpan;
        
        rowSpan = Math.max(ciRowSpan, tasksRowSpan);

        if (rowSpan > 0 || spreadDesc.isFullExport()) {
            colOffset = 0;
            if (rowSpan == 0) {
                rowSpan = 1;
            }
            
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.RU_THREATS)) {
                String cellContent = ratUnit.getThreat().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(cellContent);

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }
        
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.RU_REASONS)) {
                String cellContent = ratUnit.getThreatReasons().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(cellContent);

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }
        
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS)) {
                String cellContent = ratUnit.getProblems().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(cellContent);

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }

                ++colOffset;
            }
        }
        else {
            colOffset = 0;
        }
        
        colSpan = colOffset + ciColSpan + tasksColSpan;
        
        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeCIOnSheet(Sheet sheet, ConditionIndicator condIndicator,
                               int startColNum, int startRowNum) {

        int colSpan = 0, rowSpan = 0;
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.CI_NAME)) {
            Cell cell = getCell(sheet, startColNum, startRowNum);
            cell.setCellStyle(wrapTextCellStyle);
            cell.setCellValue(condIndicator.getName().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            rowSpan = 1;
            ++colSpan;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.CI_CUR_VAL)) {
            getCell(sheet, startColNum + 1, startRowNum).setCellValue(condIndicator.getCurrentValue().trim());
            rowSpan = 1;
            ++colSpan;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL)) {
            getCell(sheet, startColNum + 2, startRowNum).setCellValue(condIndicator.getTargetValue().trim());
            rowSpan = 1;
            ++colSpan;
        }

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeAdmEconCIOnSheet(Sheet sheet, ConditionIndicator condIndicator,
                                                   int startColNum, int startRowNum) {

        int colSpan = 0, rowSpan = 0;
        
        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME)) {
            Cell cell = getCell(sheet, startColNum, startRowNum);
            cell.setCellStyle(wrapTextCellStyle);
            cell.setCellValue(condIndicator.getName().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            rowSpan = 1;
            ++colSpan;
        }

        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL)) {
            getCell(sheet, startColNum + 1, startRowNum).setCellValue(condIndicator.getCurrentValue().trim());
            rowSpan = 1;
            ++colSpan;
        }

        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL)) {
            getCell(sheet, startColNum + 2, startRowNum).setCellValue(condIndicator.getTargetValue().trim());
            rowSpan = 1;
            ++colSpan;
        }

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeTaskOnSheet(Sheet sheet, Task task, int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        int rowSpan = 0, maxColSpan = 0, colSpan = 0, colOffset = 0;

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.TASK_CONTENT)) {
            ++colOffset;
        }
        
        if (task.getEvents() != null) {
            for (Event event : task.getEvents()) {
                if (!event.getContent().isEmpty()) {
                    CellRangeAddress rangeAddress = null;
                    
                    switch (spreadDesc.getEventType()) {
                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_YEARS:
                            rangeAddress = writeEventOnSheetByYears(sheet, event, startColNum + colOffset, startRowNum + rowSpan);
                            break;

                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS:
                            rangeAddress = writeEventOnSheetByExpCats(sheet, event, startColNum + colOffset, startRowNum + rowSpan, year);
                            break;

                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_QUARTERS:
                            rangeAddress = writeEventOnSheetByQuarters(sheet, event, startColNum + colOffset, startRowNum + rowSpan, year);
                            break;

                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_MONTHS:
                            rangeAddress = writeEventOnSheetByMonths(sheet, event, startColNum + colOffset, startRowNum + rowSpan, year);
                            break;
                    }
                    
                    colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
            
                    if (colSpan > maxColSpan) {
                        maxColSpan = colSpan;
                    }

                    rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                }
            }
            colSpan = maxColSpan;
        }

        if (rowSpan > 0 || spreadDesc.isFullExport()) {
            colOffset = 0;
            if (rowSpan == 0) {
                rowSpan = 1;
            }
            
            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.TASK_CONTENT)) {
                String cellContent = task.getContent().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(cellContent);

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }

                ++colOffset;
            }

            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM) &&
                lastComponentNum() == 1) {
                for (int i = 0; i < 5; ++i) {
                    FixedPointDec sum = computeExpensesSum(task, firstYearDate.getYear() + i);
                
                    for (int j = startRowNum; j < startRowNum + rowSpan; ++j) {
                        Cell cell = getCell(sheet, startColNum + colOffset, j);
                        cell.setCellStyle(wrapTextCellStyle);
                        cell.setCellValue(sum.doubleValue());
                    }

                    if (rowSpan > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                    }
                    
                    ++colOffset;
                }
            }
        }
        else {
            colOffset = 0;
        }
        
        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan + colOffset);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeEventOnSheetByExpCats(Sheet sheet, Event event, int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        int i = 0, j = 0, rowSpan = 0;

        List<Integer> yearDiffs = eventStartYearDifferences(event, year);

        if (year == null || (year != null && yearDiffs.contains(0))) {
            rowSpan = 1;
        }
        else {
            return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
        }

        if (event.getContent().isEmpty() || event.getDateIntervals() == null ||
            event.getDateIntervals().isEmpty()) {
            return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
        }
        else {
            rowSpan = 1;
        }
        
        if (expCats == null) {
            expCats = dataProvider.getAllExpCats();
        }
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getContent().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getPartnerOrgs().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getFinSource().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getDepartment().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getProgramme().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }

            ++i;
        }
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
            for (; j < expCats.size(); ++j) {
                FixedPointDec expCatSum = computeExpensesSum(event, expCats.get(j));
                getCell(sheet, startColNum + i + j, startRowNum).setCellValue(expCatSum.doubleValue());
            }
        }

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + i + j);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeEventOnSheetByYears(Sheet sheet, Event event, int startColNum, int startRowNum)
        throws IOException, ClassNotFoundException {

        int i = 0, j = 0, rowSpan = 0;

        if (spreadDesc.isFullExport()) {
            rowSpan = 1;
        }
        else {
            if (event.getContent().isEmpty() || event.getDateIntervals() == null || event.getDateIntervals().isEmpty()) {
                return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
            }
            else {
                rowSpan = 1;
            }
        }
        
        FixedPointDec expSum = computeExpensesSum(event, (ExpenseCategory) null);
        List<Integer> yearDiffs = null;
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
            yearDiffs = eventStartYearDifferences(event, firstYearDate.getYear());
            boolean fitsYear = false;

            for (Integer diff : yearDiffs) {
                if (diff >= 0 && diff <= 4) {
                    fitsYear = true;
                    break;
                }
            }

            if (!fitsYear || expSum.equals(new FixedPointDec("0"))) {
                return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
            }
        }
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getContent().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getPartnerOrgs().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }
        
        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getFinSource().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getDepartment().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getProgramme().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
            for (; j < 5; ++j) {
                if (yearDiffs.contains(j)) {
                    getCell(sheet, startColNum + i + j, startRowNum).setCellValue(expSum.doubleValue());
                }
                else {
                    getCell(sheet, startColNum + i + j, startRowNum).setCellValue(new FixedPointDec("0").doubleValue());
                }
            }
        }

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + i + j);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeEventOnSheetByQuarters(Sheet sheet, Event event,
                                                         int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException {

        int i = 0, j = 0, rowSpan = 0;

        List<Integer> yearDiffs = eventStartYearDifferences(event, year);
        
        if (year == null || (year != null && yearDiffs.contains(0))) {
            rowSpan = 1;
        }
        else {
            return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
        }
        
        if (event.getContent().isEmpty()) {
            
            return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
        }
        else {
            rowSpan = 1;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getContent().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getPartnerOrgs().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getFinSource().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getDepartment().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getProgramme().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        LocalDate eventStartDate = null, eventEndDate = null;
        
        for (LocalDateInterval dateInterval : event.getDateIntervals()) {
            if (dateInterval.getStartDate().getYear() == year) {
                eventStartDate = dateInterval.getStartDate();
                eventEndDate = dateInterval.getEndDate();
                break;
            }
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET) &&
            eventStartDate != null) {

            int startQuarterNum = ((eventStartDate.getMonthValue() - 1) / 3) + 1, endQuaterNum;
            if (eventEndDate == null) {
                endQuaterNum = startQuarterNum;
            }
            else if (eventEndDate.getYear() == year) {
                endQuaterNum = ((eventEndDate.getMonthValue() - 1) / 3) + 1;
            }
            else {
                endQuaterNum = 4;
            }

            for (int k = startQuarterNum; k <= endQuaterNum; ++k) {
                getCell(sheet, startColNum + i + k - 1, startRowNum).setCellValue("X");
                getCell(sheet, startColNum + i + k - 1, startRowNum).setCellStyle(greyBackgroundCellStyle);
            }
            j += 4;
        }
        
        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + i + j);
    }

    // Returns cell range which the record spans.
    private CellRangeAddress writeEventOnSheetByMonths(Sheet sheet, Event event,
                                                         int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException {

        int i = 0, j = 0, rowSpan = 0;

        List<Integer> yearDiffs = eventStartYearDifferences(event, year);
        
        if (year == null || (year != null && yearDiffs.contains(0))) {
            rowSpan = 1;
        }
        else {
            return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
        }
        
        if (event.getContent().isEmpty()) {
            
            return new CellRangeAddress(startRowNum, startRowNum, startColNum, startColNum);
        }
        else {
            rowSpan = 1;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getContent().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PARTNERS)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getPartnerOrgs().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_FIN_SRC)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getFinSource().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_DEPARTMENT)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getDepartment().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME)) {
            Cell cell = getCell(sheet, startColNum + i, startRowNum);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(event.getProgramme().trim());

            int lines = measureCellContent(cell);
            short recommendedHeight = (short) (lines * cell.getRow().getHeight());
            if (recommendedHeight > cell.getRow().getHeight()) {
                cell.getRow().setHeight(recommendedHeight);
            }
            
            ++i;
        }

        LocalDate eventStartDate = null, eventEndDate = null;
        
        for (LocalDateInterval dateInterval : event.getDateIntervals()) {
            if (dateInterval.getStartDate().getYear() == year) {
                eventStartDate = dateInterval.getStartDate();
                eventEndDate = dateInterval.getEndDate();
                break;
            }
        }

        if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET) &&
            eventStartDate != null) {

            int startQuarterNum = eventStartDate.getMonthValue(), endQuaterNum;
            if (eventEndDate == null) {
                endQuaterNum = startQuarterNum;
            }
            else if (eventEndDate.getYear() == year) {
                endQuaterNum = eventEndDate.getMonthValue();
            }
            else {
                endQuaterNum = 12;
            }

            if (startQuarterNum == endQuaterNum) {
                if (eventEndDate == null) {
                    getCell(sheet, startColNum + i + startQuarterNum - 1, startRowNum).setCellValue(eventStartDate.getDayOfMonth());
                }
                else {
                    getCell(sheet, startColNum + i + startQuarterNum - 1, startRowNum).setCellValue(
                        eventStartDate.getDayOfMonth() + " – " + eventEndDate.getDayOfMonth());
                }
                getCell(sheet, startColNum + i + startQuarterNum - 1, startRowNum).setCellStyle(greyBackgroundCellStyle);
            }
            else {
                for (int k = startQuarterNum; k <= endQuaterNum; ++k) {
                    if (k == startQuarterNum) {
                        getCell(sheet, startColNum + i + k - 1, startRowNum).setCellValue(eventStartDate.getDayOfMonth());
                    }
                    else if (k == endQuaterNum) {
                        getCell(sheet, startColNum + i + k - 1, startRowNum).setCellValue(eventEndDate.getDayOfMonth());
                    }
                    
                    getCell(sheet, startColNum + i + k - 1, startRowNum).setCellStyle(greyBackgroundCellStyle);
                }
            }

            j += 12;
        }

        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + i + j);
    }


    private CellRangeAddress writeAdmEconTasksOnSheet(Sheet sheet, AdmEconTask task,
                                                      int startColNum, int startRowNum, Integer year)
        throws IOException, ClassNotFoundException, GeneralSecurityException {

        int rowSpan = 0, maxColSpan = 0, colSpan = 0, colOffset = 0;

        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_CONTENT)) {
            ++colOffset;
        }
        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS)) {
            ++colOffset;
        }

        int ciColSpan = 0;
        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME)) {
            ++ciColSpan;
        }
        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL)) {
            ++ciColSpan;
        }
        if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL)) {
            ++ciColSpan;
        }

        if (task.getAdmEconEvents() != null) {
            for (Event event : task.getAdmEconEvents()) {
                if (!event.getContent().isEmpty()) {
                    CellRangeAddress rangeAddress = null;
                    
                    switch (spreadDesc.getEventType()) {
                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_YEARS:
                            rangeAddress = writeEventOnSheetByYears(sheet, event,
                                                                    startColNum + colOffset + ciColSpan, startRowNum + rowSpan);
                            break;

                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS:
                            rangeAddress = writeEventOnSheetByExpCats(sheet, event,
                                                                      startColNum + colOffset + ciColSpan, startRowNum + rowSpan, year);
                            break;

                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_QUARTERS:
                            rangeAddress = writeEventOnSheetByQuarters(sheet, event,
                                                                       startColNum + colOffset + ciColSpan, startRowNum + rowSpan, year);
                            break;

                        case SpreadsheetDescriptor.EventTypes.BUDGET_BY_MONTHS:
                            rangeAddress = writeEventOnSheetByMonths(sheet, event,
                                                                       startColNum + colOffset + ciColSpan, startRowNum + rowSpan, year);
                            break;
                    }
                    
                    colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
            
                    if (colSpan > maxColSpan) {
                        maxColSpan = colSpan;
                    }

                    rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                }
            }
            colSpan = maxColSpan;
        }
        int eventsRowSpan = rowSpan;
        int eventsColSpan = colSpan;

        rowSpan = 0;
        colSpan = 0;
        maxColSpan = 0;

        if (task.getAdmEconCondIndicators() != null && eventsRowSpan > 0) {
            for (ConditionIndicator condIndicator : task.getAdmEconCondIndicators()) {
                CellRangeAddress rangeAddress = writeAdmEconCIOnSheet(sheet, condIndicator,
                                                                      startColNum + colOffset, startRowNum + rowSpan);
                colSpan = rangeAddress.getLastColumn() - rangeAddress.getFirstColumn();
            
                if (colSpan > maxColSpan) {
                    maxColSpan = colSpan;
                }
            
                rowSpan += rangeAddress.getLastRow() - rangeAddress.getFirstRow();
            }
            colSpan = maxColSpan;
        }
        int ciRowSpan = rowSpan;
        // else {
        //     if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME)) {
        //         ++colSpan;
        //     }
        //     if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_CUR_VAL)) {
        //         ++colSpan;
        //     }
        //     if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL)) {
        //         ++colSpan;
        //     }
        // }

        rowSpan = Math.max(ciRowSpan, eventsRowSpan);

        if (rowSpan > 0 || spreadDesc.isFullExport()) {
            colOffset = 0;
            if (rowSpan == 0) {
                rowSpan = 1;
            }
            
            if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_CONTENT)) {
                String cellContent = task.getContent().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(task.getContent().trim());

                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }
            
            if (spreadDesc.getAdmEconColumnsList().get(SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_PROBLEMS)) {
                String cellContent = task.getProblems().trim();
                Cell tmpCell = getCell(sheet, startColNum + colOffset, startRowNum);
                int lines = measureCellContent(cellContent, sheet.getColumnWidth(tmpCell.getColumnIndex()));
                short recommendedRowHeight = (short) (lines * sheet.getDefaultRowHeight() / rowSpan);

                for (int i = startRowNum; i < startRowNum + rowSpan; ++i) {
                    Cell cell = getCell(sheet, startColNum + colOffset, i);
                    cell.setCellStyle(wrapTextCellStyle);
                    cell.setCellValue(task.getProblems().trim());
                    
                    if (recommendedRowHeight > cell.getRow().getHeight()) {
                        cell.getRow().setHeight(recommendedRowHeight);
                    }
                }

                if (rowSpan > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                }
                
                ++colOffset;
            }

            if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM) &&
                isAdmEconTaskLastCol()) {
                for (int i = 0; i < 5; ++i) {
                    FixedPointDec sum = computeExpensesSum(task, firstYearDate.getYear() + i);
                
                    for (int j = startRowNum; j < startRowNum + rowSpan; ++j) {
                        Cell cell = getCell(sheet, startColNum + colOffset, j);
                        cell.setCellStyle(wrapTextCellStyle);
                        cell.setCellValue(sum.doubleValue());
                    }

                    if (rowSpan > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + rowSpan - 1, startColNum + colOffset, startColNum + colOffset));
                    }

                    ++colOffset;
                }
            }
        }
        else {
            colOffset = 0;
        }
        
        colSpan = colOffset + ciColSpan + eventsColSpan;
        
        return new CellRangeAddress(startRowNum, startRowNum + rowSpan, startColNum, startColNum + colSpan);
    }
    
    
    private FixedPointDec computeExpensesSum(Event event, ExpenseCategory expCat) {
        if (event.getExpenses() == null) {
            return new FixedPointDec("0");
        }

        FixedPointDec sum = new FixedPointDec("0");
        for (Expense exp : event.getExpenses()) {
            if (expCat != null) {
                if (exp.getExpCatId() == expCat.getId()) {
                    sum.add(exp.getUnitCost().multiplied(exp.getAmount()));
                }
            }
            else {
                sum.add(exp.getUnitCost().multiplied(exp.getAmount()));
            }
        }

        return sum;
    }

    private FixedPointDec computeExpensesSum(Event event, Integer year) {
        if (event.getExpenses() == null) {
            return new FixedPointDec("0");
        }
        FixedPointDec sum = new FixedPointDec("0");
        List<Integer> yearDiffs = eventStartYearDifferences(event, year);

        if (year == null || yearDiffs.contains(0)) {
            for (Expense exp : event.getExpenses()) {
                sum.add(exp.getUnitCost().multiplied(exp.getAmount()));
            }
        }
        
        return sum;
    }

    private FixedPointDec computeExpensesSum(Task task, Integer year) {
        FixedPointDec sum = new FixedPointDec("0");

        if (task.getEvents() != null) {
            for (Event event : task.getEvents()) {
                sum.add(computeExpensesSum(event, year));
            }
        }

        return sum;
    }

    private FixedPointDec computeExpensesSum(AdmEconTask task, Integer year) {
        FixedPointDec sum = new FixedPointDec("0");

        if (task.getAdmEconEvents() != null) {
            for (Event event : task.getAdmEconEvents()) {
                sum.add(computeExpensesSum(event, year));
            }
        }

        return sum;
    }

    private FixedPointDec computeExpensesSum(RationaleUnit ratUnit, Integer year) {
        FixedPointDec sum = new FixedPointDec("0");

        if (ratUnit.getTasks() != null) {
            for (Task task : ratUnit.getTasks()) {
                sum.add(computeExpensesSum(task, year));
            }
        }

        return sum;
    }

    private FixedPointDec computeExpensesSum(KeyValue keyValue, Integer year) {
        FixedPointDec sum = new FixedPointDec("0");

        if (keyValue.getRationaleUnits() != null) {
            for (RationaleUnit ratUnit : keyValue.getRationaleUnits()) {
                sum.add(computeExpensesSum(ratUnit, year));
            }
        }

        return sum;
    }

    private FixedPointDec computeExpensesSum(KeyValueGroup keyValueGroup, Integer year) {
        FixedPointDec sum = new FixedPointDec("0");

        if (keyValueGroup.getKeyValues() != null) {
            for (KeyValue keyValue : keyValueGroup.getKeyValues()) {
                sum.add(computeExpensesSum(keyValue, year));
            }
        }

        return sum;
    }

    private CellStyle createGreyCellStyle(Workbook wb) {
        try {
            // String rgbS = "DBDBDB";
            // byte[] rgbB = Hex.decodeHex(rgbS); // get byte array from hex string
            byte[] rgbB = {(byte) 0xDB, (byte) 0xDB, (byte) 0xDB};
            XSSFColor color = new XSSFColor(rgbB, null); //IndexedColorMap has no usage until now. So it can be set null.

            XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
            cellStyle.setFillForegroundColor(color);
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            return cellStyle;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Return amount of rows that the header spans
    private int writeHeadersOnSheet(Sheet sheet, int startColNum, int startRowNum, boolean isAdmEcon)
        throws IOException, ClassNotFoundException, GeneralSecurityException {
        
        int headerRowSpan = 0;
        
        if (spreadDesc.getSpreadsheetType() == SpreadsheetDescriptor.SpreadsheetTypes.GENERAL_BUDGET) {
            int curCol = startColNum;

            if (isAdmEcon) {
                for (int i = SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_CONTENT + 1;
                     i-- > SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL;) {
                    if (spreadDesc.getAdmEconColumnsList().get(i)) {
                        Cell curCell = getCell(sheet, curCol, startRowNum);
                        curCell.setCellValue(SpreadsheetDescriptor.ADM_ECON_RECORD_HEADERS[i]);
                        CellUtil.setAlignment(curCell, HorizontalAlignment.CENTER);
                        CellUtil.setVerticalAlignment(curCell, VerticalAlignment.CENTER);
                        CellUtil.setFont(curCell, boldFont);
                        sheet.setColumnWidth(curCol, SpreadsheetDescriptor.ADM_ECON_COLUMN_WIDTHS[i] * 256);
                        ++curCol;
                    }
                }
                
                for (int i = SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT + 1;
                     i-- > SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME;) {
                    if (spreadDesc.getColumnsList().get(i)) {
                        Cell curCell = getCell(sheet, curCol, startRowNum);
                        curCell.setCellValue(SpreadsheetDescriptor.RECORD_HEADERS[i]);
                        CellUtil.setAlignment(curCell, HorizontalAlignment.CENTER);
                        CellUtil.setVerticalAlignment(curCell, VerticalAlignment.CENTER);
                        CellUtil.setFont(curCell, boldFont);
                        sheet.setColumnWidth(curCol, SpreadsheetDescriptor.COLUMN_WIDTHS[i] * 256);
                        ++curCol;
                    }
                }
            }
            else {
                for (int i = SpreadsheetDescriptor.ColumnTypes.KVG_NAME + 1;
                     i-- > SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME;) {
                    if (spreadDesc.getColumnsList().get(i)) {
                        Cell curCell = getCell(sheet, curCol, startRowNum);
                        curCell.setCellValue(SpreadsheetDescriptor.RECORD_HEADERS[i]);
                        CellUtil.setAlignment(curCell, HorizontalAlignment.CENTER);
                        CellUtil.setVerticalAlignment(curCell, VerticalAlignment.CENTER);
                        CellUtil.setFont(curCell, boldFont);
                        sheet.setColumnWidth(curCol, SpreadsheetDescriptor.COLUMN_WIDTHS[i] * 256);
                        ++curCol;
                    }
                }
            }
            ++headerRowSpan;
            

            switch (spreadDesc.getEventType()) {
                case SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS: {
                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        if (expCats == null) {
                            expCats = dataProvider.getAllExpCats();
                        }

                        // getCell(sheet, curCol, startRowNum).setCellValue("Специфики");
                        // CellUtil.setAlignment(getCell(sheet, curCol, startRowNum), HorizontalAlignment.CENTER);
                        // CellUtil.setFont(getCell(sheet, curCol, startRowNum), boldFont);

                        for (ExpenseCategory expCat : expCats) {
                            getCell(sheet, curCol, startRowNum).setCellValue(expCat.getCategoryCode());
                            CellUtil.setFont(getCell(sheet, curCol, startRowNum), boldFont);
                            ++curCol;
                        }
                        getCell(sheet, curCol, startRowNum).setCellValue("ИТОГО");
                        CellUtil.setAlignment(getCell(sheet, curCol, startRowNum), HorizontalAlignment.RIGHT);
                        CellUtil.setFont(getCell(sheet, curCol, startRowNum), boldFont);
                        ++curCol;

                        // sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, curCol - expCats.size() - 1, curCol - 1));
                        
                        headerRowSpan = 1;
                    }

                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM) &&
                        !spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        
                        getCell(sheet, curCol, startRowNum).setCellValue("Сумма");
                        CellUtil.setAlignment(getCell(sheet, curCol, startRowNum), HorizontalAlignment.CENTER);
                        CellUtil.setFont(getCell(sheet, curCol, startRowNum), boldFont);
                        sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, curCol, curCol + 4));

                        for (int i = 0; i < 5; ++i) {
                            getCell(sheet, curCol + i, startRowNum + 1).setCellValue(firstYearDate.getYear() + i);
                            CellUtil.setAlignment(getCell(sheet, curCol + i, startRowNum + 1), HorizontalAlignment.CENTER);
                            CellUtil.setFont(getCell(sheet, curCol + i, startRowNum + 1), boldFont);
                        }

                        ++curCol;
                        headerRowSpan = 2;
                    }
                    
                    break;
                }

                case SpreadsheetDescriptor.EventTypes.BUDGET_BY_QUARTERS: {
                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        // for (int i = 0; i < 5; ++i) {
                        //     getCell(sheet, curCol + i * 4, startRowNum).setCellValue(firstYearDate.getYear() + i);
                        //     CellUtil.setAlignment(getCell(sheet, curCol + i * 4, startRowNum), HorizontalAlignment.CENTER);
                        //     CellUtil.setFont(getCell(sheet, curCol + i * 4, startRowNum), boldFont);
                        //     sheet.addMergedRegionUnsafe(new CellRangeAddress(startRowNum, startRowNum,
                        //                                                      curCol + i * 4, curCol + (i + 1) * 4 - 1));
                            
                        for (int j = 0; j < 4; ++j) {
                            getCell(sheet, curCol + j, startRowNum).setCellValue((j + 1) + " кв");
                        }
                        // }
                        curCol += 4;
                        
                        headerRowSpan = 1;
                    }

                    break;
                }

                case SpreadsheetDescriptor.EventTypes.BUDGET_BY_MONTHS: {
                    String[] months =
                        {"Январь", "Февраль", "Март", "Апрель",
                        "Май", "Июнь", "Июль", "Август",
                        "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
                    
                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {

                        for (int j = 0; j < 12; ++j) {
                            getCell(sheet, curCol + j, startRowNum).setCellValue(months[j]);
                        }

                        curCol += 12;
                        
                        headerRowSpan = 1;
                    }

                    break;
                }

                case SpreadsheetDescriptor.EventTypes.BUDGET_BY_YEARS: {
                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        getCell(sheet, curCol, startRowNum).setCellValue("Общий бюджет");
                        sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, curCol, curCol + 4));
                        CellUtil.setAlignment(getCell(sheet, curCol, startRowNum), HorizontalAlignment.CENTER);
                        CellUtil.setFont(getCell(sheet, curCol, startRowNum), boldFont);

                        for (int i = 0; i < 5; ++i) {
                            getCell(sheet, curCol + i, startRowNum + 1).setCellValue(firstYearDate.getYear() + i);
                            CellUtil.setFont(getCell(sheet, curCol + i, startRowNum + 1), boldFont);
                        }
                        curCol += 5;

                        headerRowSpan = 2;
                    }

                    if (spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_SUM) &&
                        !spreadDesc.getColumnsList().get(SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET)) {
                        
                        getCell(sheet, curCol, startRowNum).setCellValue("Сумма");
                        CellUtil.setAlignment(getCell(sheet, curCol, startRowNum), HorizontalAlignment.CENTER);
                        CellUtil.setFont(getCell(sheet, curCol, startRowNum), boldFont);
                        // sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + 1, curCol, curCol));

                        ++curCol;
                    }
                    
                    break;
                }
            }
            
            if (headerRowSpan == 2) {
                if (isAdmEcon) {
                    int c = 0;
                    for (int i = SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_CONTENT + 1;
                         i-- > SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL;) {
                        if (spreadDesc.getAdmEconColumnsList().get(i)) {
                            sheet.addMergedRegion(
                                new CellRangeAddress(startRowNum, startRowNum + 1, startColNum + c, startColNum + c));
                            ++c;
                        }
                    }
                    for (int i = SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT + 1;
                         i-- > SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME;) {
                        if (spreadDesc.getColumnsList().get(i)) {
                            sheet.addMergedRegion(
                                new CellRangeAddress(startRowNum, startRowNum + 1, startColNum + c, startColNum + c));
                            ++c;
                        }
                    }
                }
                else {
                    int c = 0;
                    for (int i = SpreadsheetDescriptor.ColumnTypes.KVG_NAME + 1;
                         i-- > SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME;) {
                        if (spreadDesc.getColumnsList().get(i)) {
                            sheet.addMergedRegion(
                                new CellRangeAddress(startRowNum, startRowNum + 1, startColNum + c, startColNum + c));
                            ++c;
                        }
                    }
                }
            }

        }
        else {
            getCell(sheet, startColNum, startRowNum).setCellValue("Программа ООПТ");
            getCell(sheet, startColNum + 1, startRowNum).setCellValue("Общий бюджет");
            sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum + 1, startColNum, startColNum));
            sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, startColNum + 1, startColNum + 5));

            for (int i = 0; i < 5; ++i) {
                getCell(sheet, startColNum + i + 1, startRowNum + 1).setCellValue(firstYearDate.getYear() + i);
                CellUtil.setFont(getCell(sheet, startColNum + i + 1, startRowNum + 1), boldFont);
                CellUtil.setAlignment(getCell(sheet, startColNum + i + 1, startRowNum + 1), HorizontalAlignment.CENTER);
                sheet.setColumnWidth(i + 1, 20 * 256);
            }

            headerRowSpan = 2;

            sheet.setColumnWidth(0, 84 * 256);

            
            CellUtil.setAlignment(getCell(sheet, startColNum, startRowNum), HorizontalAlignment.CENTER);
            CellUtil.setVerticalAlignment(getCell(sheet, startColNum, startRowNum), VerticalAlignment.CENTER);
            CellUtil.setAlignment(getCell(sheet, startColNum + 1, startRowNum), HorizontalAlignment.CENTER);
            CellUtil.setFont(getCell(sheet, startColNum, startRowNum), boldFont);
            CellUtil.setFont(getCell(sheet, startColNum + 1, startRowNum), boldFont);
        }

        return headerRowSpan;
    }

    
    // TODO: implement function that calculates the required height of a cell
    private void autoSizeCellHeight(XSSFCell cell) {
        
//         // Create Font object with Font attribute (e.g. Font family, Font size, etc) for calculation
//         java.awt.Font currFont = new java.awt.Font(fontName, 0, fontSize);
//         AttributedString attrStr = new AttributedString(cellValue);
//         attrStr.addAttribute(TextAttribute.FONT, currFont);

// // Use LineBreakMeasurer to count number of lines needed for the text
//         FontRenderContext frc = new FontRenderContext(null, true, true);
//         LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), frc);
//         int nextPos = 0;
//         int lineCnt = 0;
//         while (measurer.getPosition() < cellValue.length())
//         {
//             nextPos = measurer.nextOffset(mergedCellWidth); // mergedCellWidth is the max width of each line
//             lineCnt++;
//             measurer.setPosition(nextPos);
//         }

//         Row currRow = currSht.getRow(rowNum);
//         currRow.setHeight((short)(currRow.getHeight() * lineCnt));
    }



    private void deleteZeroExpCats(Sheet sheet) {
        Row expCatsHeaderRow = sheet.getRow(0);
        int firstExpCatsCol = 1, lastExpCatsCol = 0;
        String firstExpCatCode = expCats.get(0).getCategoryCode();
        
        for (int i = 0; i < expCatsHeaderRow.getLastCellNum(); ++i) {
            Cell c = expCatsHeaderRow.getCell(i);
            if (c != null) {
                if (c.getStringCellValue().equals(firstExpCatCode)) {
                    firstExpCatsCol = i;
                    lastExpCatsCol = i + expCats.size() - 1;
                    break;
                }
            }
        }

        int lastRow = sheet.getLastRowNum();
        List<Integer> colsToDelete = new ArrayList<>();
        
        for (int i = firstExpCatsCol; i <= lastExpCatsCol; ++i) {
            boolean isColEmpty = true;
            
            for (int j = 1; j <= lastRow; ++j) {
                Row row = sheet.getRow(j);
                if (row != null) {
                    Cell c = row.getCell(i);
                    if (c != null) {
                        if (c.getCellType() == CellType.NUMERIC) {
                            double cellValue = c.getNumericCellValue();
                            if (cellValue == 0.0) {
                                continue;
                            }
                            else {
                                isColEmpty = false;
                                break;
                            }
                        }
                        else {
                            String cellValue = c.getStringCellValue();
                            if (cellValue.isEmpty() || cellValue.equals("0")) {
                                continue;
                            }
                            else {
                                System.out.println(cellValue);
                                isColEmpty = false;
                                break;
                            }
                        }
                    }
                }
            }

            if (isColEmpty) {
                colsToDelete.add(i);
            }
        }

        while (!colsToDelete.isEmpty()) {
            deleteColumn(sheet, colsToDelete.get(0));
            colsToDelete.remove(0);

            for (int i = 0; i < colsToDelete.size(); ++i) {
                colsToDelete.set(i, colsToDelete.get(i) - 1);
            }
        }
        
    }


    private void addSumsToSheet(Sheet sheet) {
        if (spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_EXP_CATS) {
            Row expCatsHeaderRow = sheet.getRow(0);
            int firstExpCatsCol = 1, lastExpCatsCol = 0;
            String firstExpCatCode = expCats.get(0).getCategoryCode();
        
            for (int i = 0; i < expCatsHeaderRow.getLastCellNum(); ++i) {
                Cell c = expCatsHeaderRow.getCell(i);
                if (c != null) {
                    if (isExpCatCode(c.getStringCellValue())) {
                        firstExpCatsCol = i;
                        for (int j = 0; j < expCats.size(); ++j) {
                            c = expCatsHeaderRow.getCell(i + j);
                            if (!isExpCatCode(c.getStringCellValue())) {
                                break;
                            }
                            lastExpCatsCol = i + j;
                        }
                    
                        break;
                    }
                }
            }

            if (firstExpCatsCol > lastExpCatsCol) {
                return;
            }

            int lastFilledRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastFilledRow; ++i) {
                Row r = sheet.getRow(i);
                if (r != null) {
                    CellRangeAddress sumRange = new CellRangeAddress(i, i, firstExpCatsCol, lastExpCatsCol);
                    String sumFormula = String.format("SUM(%s)", sumRange.formatAsString());
                    getCell(sheet, lastExpCatsCol + 1, i).setCellFormula(sumFormula);
                }
            }

            if (lastFilledRow < 2) {
                return;
            }
        
            for (int i = firstExpCatsCol; i <= lastExpCatsCol + 1; ++i) {
                CellRangeAddress sumRange = new CellRangeAddress(1, lastFilledRow, i, i);
                String sumFormula = String.format("SUM(%s)", sumRange.formatAsString());
                getCell(sheet, i, lastFilledRow + 2).setCellFormula(sumFormula);
            }

            getCell(sheet, 0, lastFilledRow + 2).setCellValue("Итого");
            sheet.addMergedRegion(new CellRangeAddress(lastFilledRow + 2, lastFilledRow + 2, 0, firstExpCatsCol - 1));
        }
        else if (spreadDesc.getEventType() == SpreadsheetDescriptor.EventTypes.BUDGET_BY_YEARS) {
            Row firstRow = sheet.getRow(0);
            int firstBudgetCol = -1, lastBudgetCol = -1;
            
            for (int i = 0; i < firstRow.getLastCellNum(); ++i) {
                Cell c = firstRow.getCell(i);
                if (c != null) {
                    if (c.getStringCellValue().equals("Общий бюджет")) {
                        firstBudgetCol = i;
                        lastBudgetCol = i + 4;
                        break;
                    }
                }
            }

            if (firstBudgetCol == -1 || lastBudgetCol == -1) {
                return;
            }

            int lastFilledRow = sheet.getLastRowNum();

            for (int i = 2; i <= lastFilledRow; ++i) {
                Row r = sheet.getRow(i);
                if (r != null) {
                    CellRangeAddress sumRange = new CellRangeAddress(i, i, firstBudgetCol, lastBudgetCol);
                    String sumFormula = String.format("SUM(%s)", sumRange.formatAsString());
                    getCell(sheet, lastBudgetCol + 1, i).setCellFormula(sumFormula);
                }
            }

            if (lastFilledRow < 2) {
                return;
            }

            for (int i = firstBudgetCol; i <= lastBudgetCol + 1; ++i) {
                CellRangeAddress sumRange = new CellRangeAddress(2, lastFilledRow, i, i);
                String sumFormula = String.format("SUM(%s)", sumRange.formatAsString());
                getCell(sheet, i, lastFilledRow + 2).setCellFormula(sumFormula);
            }

            getCell(sheet, 0, lastFilledRow + 2).setCellValue("Итого");
            sheet.addMergedRegion(new CellRangeAddress(lastFilledRow + 2, lastFilledRow + 2, 0, firstBudgetCol - 1));
        }
    }

    private boolean isExpCatCode(String str) {
        for (ExpenseCategory expCat : expCats) {
            if (expCat.getCategoryCode().equals(str)) {
                return true;
            }
        }

        return false;
    }
    
    // Returns the amount of lines that str spans. ColumnWidth is in characters.
    private int measureCellContent(String str, int columnWidth) {
        if (str.isEmpty()) {
            return 1;
        }
        
        java.awt.Font currFont = new java.awt.Font("Calibri", 0, 11);
        AttributedString attrStr = new AttributedString(str);
        attrStr.addAttribute(java.awt.font.TextAttribute.FONT, currFont);

        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
        java.awt.font.LineBreakMeasurer measurer = new java.awt.font.LineBreakMeasurer(attrStr.getIterator(), frc);
        int nextPos = 0;
        int lineCnt = 0;
        while (measurer.getPosition() < str.length()) {
            nextPos = measurer.nextOffset(columnWidth);
            lineCnt++;
            measurer.setPosition(nextPos);
        }

        return lineCnt;
    }
    
    // Returns the amount of lines that cellContent spans
    private int measureCellContent(Cell cell) {
        String cellValue = cell.getStringCellValue();
        if (cellValue.isEmpty()) {
            return 1;
        }
        
        java.awt.Font currFont = new java.awt.Font("Calibri", 0, 11);
        AttributedString attrStr = new AttributedString(cellValue);
        attrStr.addAttribute(java.awt.font.TextAttribute.FONT, currFont);

        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
        java.awt.font.LineBreakMeasurer measurer = new java.awt.font.LineBreakMeasurer(attrStr.getIterator(), frc);
        int nextPos = 0;
        int lineCnt = 0;
        while (measurer.getPosition() < cellValue.length()) {
            nextPos = measurer.nextOffset(cell.getSheet().getColumnWidth(cell.getColumnIndex()));
            lineCnt++;
            measurer.setPosition(nextPos);
        }

        return lineCnt;
    }


    
     /**
     * Given a sheet, this method deletes a column from a sheet and moves
     * all the columns to the right of it to the left one cell.
     * 
     * Note, this method will not update any formula references.
     * 
     * @param sheet
     * @param column
     */
    private void deleteColumn( Sheet sheet, int columnToDelete ){
        for (int rId = 0; rId <= sheet.getLastRowNum(); rId++) {
            Row row = sheet.getRow(rId);
            
            if (row == null) {
                continue;
            }
            
            for (int cID = columnToDelete; cID < row.getLastCellNum(); cID++) {
                Cell cOld = row.getCell(cID);
                if (cOld != null) {
                    row.removeCell(cOld);
                }
                Cell cNext = row.getCell(cID + 1);
                if (cNext != null) {
                    Cell cNew = row.createCell(cID, cNext.getCellType());
                    cloneCell(cNew, cNext);
                    //Set the column width only on the first row.
                    //Other wise the second row will overwrite the original column width set previously.
                    if(rId == 0) {
                        sheet.setColumnWidth(cID, sheet.getColumnWidth(cID + 1));
                    }
                }
            }
        }

        
        // int maxColumn = 0;
        // for ( int r=0; r < sheet.getLastRowNum()+1; r++ ){
        //     Row row = sheet.getRow( r );

        //     // if no row exists here; then nothing to do; next!
        //     if ( row == null )
        //         continue;

        //     // if the row doesn't have this many columns then we are good; next!
        //     int lastColumn = row.getLastCellNum();
        //     if ( lastColumn > maxColumn )
        //         maxColumn = lastColumn;

        //     if ( lastColumn < columnToDelete )
        //         continue;

        //     for ( int x=columnToDelete+1; x < lastColumn + 1; x++ ){
        //         Cell oldCell    = row.getCell(x-1);
        //         if ( oldCell != null )
        //             row.removeCell( oldCell );

        //         Cell nextCell   = row.getCell( x );
        //         if ( nextCell != null ){
        //             Cell newCell    = row.createCell( x-1, nextCell.getCellType() );
        //             cloneCell(newCell, nextCell);
        //         }
        //     }
        // }


        // // Adjust the column widths
        // for ( int c=0; c < maxColumn; c++ ){
        //     sheet.setColumnWidth( c, sheet.getColumnWidth(c+1) );
        // }
    }


    // Процедура, которая настраивает автофильтры на листе с природоохранной выгрузкой.
    private void setupFiltersEnvir(Sheet sheet) {
        int firstFilteredCol = 0, lastFilteredColumn = -1;
        
        if (spreadDesc.getSpreadsheetType() == SpreadsheetDescriptor.SpreadsheetTypes.GENERAL_BUDGET) {
            for (int i = SpreadsheetDescriptor.ColumnTypes.KVG_NAME + 1;
                 i-- > SpreadsheetDescriptor.ColumnTypes.EVENT_PROGRAMME;) {
                if (spreadDesc.getColumnsList().get(i)) {
                    ++lastFilteredColumn;
                }
            }
        }
        else if (spreadDesc.getSpreadsheetType() == SpreadsheetDescriptor.SpreadsheetTypes.PROGRAMMES_BUDGET) {
            lastFilteredColumn = 0;
        }

        if (firstFilteredCol <= lastFilteredColumn) {
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), firstFilteredCol, lastFilteredColumn));
        }
    }

    // Процедура, которая настраивает автофильтры на листе с административно-хозяйственной выгрузкой.
    private void setupFiltersAdmEcon(Sheet sheet) {
        int firstFilteredCol = 0, lastFilteredColumn = -1;

        for (int i = SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_TASK_CONTENT + 1;
             i-- > SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL;) {
            if (spreadDesc.getColumnsList().get(i)) {
                ++lastFilteredColumn;
            }
        }

        if (firstFilteredCol <= lastFilteredColumn) {
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), firstFilteredCol, lastFilteredColumn));
        }
    }

    private List<Integer> eventStartYearDifferences(Event event, Integer baseYear) {
        List<Integer> resDiffs = new ArrayList<Integer>();

        if (event.getDateIntervals() != null && baseYear != null) {
            for (LocalDateInterval dateInterval : event.getDateIntervals()) {
                if (dateInterval.getStartDate() != null) {
                    resDiffs.add(dateInterval.getStartDate().getYear() - baseYear);
                }
            }
        }

        return resDiffs;
    }

    /*
     * Takes an existing Cell and merges all the styles and forumla
     * into the new one
     */
    private void cloneCell(Cell cNew, Cell cOld) {
        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());

        if (CellType.BOOLEAN == cNew.getCellType()) {
            cNew.setCellValue(cOld.getBooleanCellValue());
        } else if (CellType.NUMERIC == cNew.getCellType()) {
            cNew.setCellValue(cOld.getNumericCellValue());
        } else if (CellType.STRING == cNew.getCellType()) {
            cNew.setCellValue(cOld.getStringCellValue());
        } else if (CellType.ERROR == cNew.getCellType()) {
            cNew.setCellValue(cOld.getErrorCellValue());
        } else if (CellType.FORMULA == cNew.getCellType()) {
            cNew.setCellValue(cOld.getCellFormula());
        }
    }

    // 0 - event, 1 - task, 2 - ci, 3 - ru, 4 - kv, 5 - kvg
    private int lastComponentNum() {
        int lastColNum = 1;
        for (int i = SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET;
             i <= SpreadsheetDescriptor.ColumnTypes.KVG_NAME;
             ++i) {
            if (spreadDesc.getColumnsList().get(i)) {
                lastColNum = i;
                break;
            }
        }

        if (lastColNum >= SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET &&
            lastColNum <= SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT) {
            return 0;
        }
        else if (lastColNum == SpreadsheetDescriptor.ColumnTypes.TASK_CONTENT) {
            return 1;
        }
        else if (lastColNum >= SpreadsheetDescriptor.ColumnTypes.CI_TARG_VAL &&
                 lastColNum <= SpreadsheetDescriptor.ColumnTypes.CI_NAME) {
            return 2;
        }
        else if (lastColNum >= SpreadsheetDescriptor.ColumnTypes.RU_PROBLEMS &&
                 lastColNum <= SpreadsheetDescriptor.ColumnTypes.RU_THREATS) {
            return 3;
        }
        else if (lastColNum >= SpreadsheetDescriptor.ColumnTypes.KV_DESC &&
                 lastColNum <= SpreadsheetDescriptor.ColumnTypes.KV_NAME) {
            return 4;
        }
        else if (lastColNum == SpreadsheetDescriptor.ColumnTypes.KVG_NAME) {
            return 5;
        }

        return 0;
    }

    private boolean isAdmEconTaskLastCol() {
        for (int i = SpreadsheetDescriptor.ColumnTypes.EVENT_BUDGET;
             i <= SpreadsheetDescriptor.ColumnTypes.EVENT_CONTENT;
             ++i) {
            if (spreadDesc.getColumnsList().get(i)) {
                return false;
            }
        }
        for (int i = SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_TARG_VAL;
             i <= SpreadsheetDescriptor.admEconColumnTypes.ADM_ECON_CI_NAME;
             ++i) {
            if (spreadDesc.getAdmEconColumnsList().get(i)) {
                return false;
            }
        }

        return true;
    }

    
	public LocalDate getFirstYearDate() {
		return firstYearDate;
	}

	public void setFirstYearDate(LocalDate firstYearDate) {
		this.firstYearDate = firstYearDate;
	}
	
}
