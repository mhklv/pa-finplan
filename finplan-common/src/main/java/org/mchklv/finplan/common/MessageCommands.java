package org.mchklv.finplan.common;


public final class MessageCommands {
    public static final int REGISTER = 0;
    public static final int AUTHORIZE = 1;
    public static final int INSERT_RECORD = 2;
    public static final int UPDATE_RECORD = 3;
    public static final int DELETE_RECORD = 4;
    public static final int RETRIEVE = 5;

    public static final int NO_OP = Integer.MAX_VALUE - 2;

    public static final int IO_ERROR = Integer.MAX_VALUE - 1;
    public static final int ERROR = Integer.MAX_VALUE;



    public final class RecordTypes {
        public static final int PROTECTED_AREA = 0;
        public static final int KEY_VALUE = 1;
        public static final int RATIONALE_UNIT = 2;
        public static final int COND_INDICATOR = 3;
        public static final int TASK = 4;
        public static final int EVENT = 5;
        public static final int PARTNER_ORG = 6;
        public static final int EXP_CATEGORY = 7;
        public static final int EXPENSE = 8;
        public static final int EVENT_DATE_INTERVAL = 9;

        public static final int ADM_ECON_TASK = 10;
        public static final int ADM_ECON_CI = 11;
        public static final int ADM_ECON_EVENT = 12;
        public static final int ADM_ECON_EXPENSE = 13;
        public static final int ADM_ECON_EVENT_DATE_INTERVAL = 14;
    }


    public final class RetrieveSubCommands {
        public static final int PROTECTED_AREA = 0;
        public static final int KEY_VALUE_GROUPS = 1;
        public static final int KEY_VALUES = 2;
        public static final int EVENTS = 3;
        public static final int EXP_CATEGORIES = 4;
        public static final int ADM_ECON_TASKS = 5;
    }
}
