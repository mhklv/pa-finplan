package org.mchklv.finplan.client.documentGen;

import java.util.ArrayList;
import java.util.List;

public class SpreadsheetDescriptor {
    private int spreadsheetType = 0;
    private int eventType = 0;
    private boolean isFullExport = false;
    private List<Boolean> columnsList;
    private List<Boolean> admEconColumnsList;
    


    public SpreadsheetDescriptor() {
        columnsList = new ArrayList<Boolean>(17);
        for (int i = 0; i < 17; ++i) {
            columnsList.add(true);
            // columnsList.set(i, true);
        }

        admEconColumnsList = new ArrayList<Boolean>(5);
        for (int i = 0; i < 5; ++i) {
            admEconColumnsList.add(true);
        }
    }
    
    public boolean isFullExport() {
		return isFullExport;
	}

	public void setFullExport(boolean isFullExport) {
		this.isFullExport = isFullExport;
	}

	public int getSpreadsheetType() {
		return spreadsheetType;
	}

	public void setSpreadsheetType(int spreadsheetType) {
		this.spreadsheetType = spreadsheetType;
	}

	public List<Boolean> getColumnsList() {
		return columnsList;
	}
    
	public void setColumnsList(List<Boolean> columnsList) {
		this.columnsList = columnsList;
	}

    public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}


    // public final class YearConstraintType {
    //     public static final int SPAN_5_YEARS = 0;
    //     public static final int SINGLE_YEAR = 1;
    // }
    
    public final class EventTypes {
        public static final int BUDGET_BY_YEARS = 0;
        public static final int BUDGET_BY_EXP_CATS = 1;
        public static final int BUDGET_BY_QUARTERS = 2;
        public static final int BUDGET_BY_MONTHS = 3;
    }
    
    public final class SpreadsheetTypes {
        public static final int GENERAL_BUDGET = 0;
        public static final int PROGRAMMES_BUDGET = 1;
    }
    
	public final class ColumnTypes {
        public static final int EVENT_SUM = 0;
        public static final int EVENT_BUDGET = 1;
        public static final int EVENT_PROGRAMME = 2;
        public static final int EVENT_DEPARTMENT = 3;
        public static final int EVENT_FIN_SRC = 4;
        public static final int EVENT_PARTNERS = 5;
        public static final int EVENT_CONTENT = 6;
        public static final int TASK_CONTENT = 7;
        public static final int CI_TARG_VAL = 8;
        public static final int CI_CUR_VAL = 9;
        public static final int CI_NAME = 10;
        public static final int RU_PROBLEMS = 11;
        public static final int RU_REASONS = 12;
        public static final int RU_THREATS = 13;
        public static final int KV_DESC = 14;
        public static final int KV_NAME = 15;
        public static final int KVG_NAME = 16;
    }
    
    public final static int[] RECORD_COLSPAN = {
        1,
        36,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1
    };

    public final static String[] RECORD_HEADERS = {
        "",
        "",
        "Программа ООПТ",
        "Отдел ООПТ",
        "Источник финансирования",
        "Партнёры",
        "Мероприятие",
        "Задача",
        "Целевая величина индикатора",
        "Текущая величина индиктора",
        "Индикатор состояния",
        "Проблемы",
        "Причины",
        "Угрозы",
        "Описание КЦ",
        "Ключевая ценность",
        "Группа КЦ"
    };

    public final static int[] COLUMN_WIDTHS = {
        0,
        0,
        30,
        30,
        30,
        30,
        40,
        45,
        30,
        30,
        40,
        45,
        35,
        30,
        45,
        25,
        25
    };


    public final class admEconColumnTypes {
        // public static final int ADM_ECON_EVENT_SUM = 0;
        // public static final int ADM_ECON_EVENT_BUDGET = 1;
        // public static final int ADM_ECON_EVENT_PROGRAMME = 2;
        // public static final int ADM_ECON_EVENT_DEPARTMENT = 3;
        // public static final int ADM_ECON_EVENT_FIN_SRC = 4;
        // public static final int ADM_ECON_EVENT_CONTENT = 5;
        public static final int ADM_ECON_CI_TARG_VAL = 0;
        public static final int ADM_ECON_CI_CUR_VAL = 1;
        public static final int ADM_ECON_CI_NAME = 2;
        public static final int ADM_ECON_TASK_PROBLEMS = 3;
        public static final int ADM_ECON_TASK_CONTENT = 4;
    }

    public final static String[] ADM_ECON_RECORD_HEADERS = {
        "Целевая величина индикатора",
        "Текущая величина индиктора",
        "Индикатор состояния",
        "Проблемы",
        "Задача"
    };

    public final static int[] ADM_ECON_COLUMN_WIDTHS = {
        30,
        30,
        40,
        45,
        45
    };



	public List<Boolean> getAdmEconColumnsList() {
		return admEconColumnsList;
	}

	public void setAdmEconColumnsList(List<Boolean> admEconColumnsList) {
		this.admEconColumnsList = admEconColumnsList;
	}
}
