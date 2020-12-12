package org.mchklv.finplan.client;

public class LocalDatabaseTables {
    public static String[][] TABLES_NAMES_AND_DEFINITIONS = {
        {
            "keyValueGroup",
                    "(      -- Группы ключевых ценностей\n    kVGId INTEGER PRIMARY KEY,\n    kVGName VARCHAR(500) NOT NULL\n)"
        },

        {
            "keyValue",
                    "(           -- Ключевые ценности\n    kVId INTEGER PRIMARY KEY,\n    kVName VARCHAR(500) NOT NULL,\n    kVDesc TEXT,\n\n    kVKVGRef INTEGER REFERENCES keyValueGroup (kVGId) ON DELETE CASCADE\n)"
        },

        {
            "rationaleUnit",
            "(      -- Единица обоснования\n    rUId INTEGER PRIMARY KEY,\n    rUThreat TEXT,\n    rUThreatReasons TEXT,\n    rUProblems TEXT,\n\n    rUKVRef INTEGER REFERENCES keyValue (kVId) ON DELETE CASCADE\n)"
        },

        {
            "task",
            "(               -- Задача\n    taskId INTEGER PRIMARY KEY,\n    taskContent TEXT,\n\n    taskRURef INTEGER REFERENCES rationaleUnit (rUId) ON DELETE CASCADE\n)"
        },

        {
            "condIndicator",
            "(      -- Индикатор состояния\n    cIId INTEGER PRIMARY KEY,\n    cIName VARCHAR(500),\n    cICurrentVal VARCHAR(500),\n    cITargetVal VARCHAR(500),\n\n    cIRURef INTEGER REFERENCES rationaleUnit (rUId) ON DELETE CASCADE\n)"
        },

        {
            "PADepartment",
            "(       -- Отдел ООПТ (ограниченный список)\n    PADepId INTEGER PRIMARY KEY,\n    PADepName VARCHAR(500)    \n)"
        },

        {
            "PAProgramme",
            "(        -- Программа ООПТ (ограниченный список)\n    PAProgId INTEGER PRIMARY KEY,\n    PAProgName VARCHAR(500)\n)"
        },

        {
            "partnerOrg",
                    "(        -- Организация-партнёр (для мероприятий)\n    partnerOrgId INTEGER PRIMARY KEY,\n    partnerOrgName VARCHAR(1000),\n\n    partnerOrgEventRef INTEGER REFERENCES event (eventId)\n)"
        },

        {
            "event",
            "(              -- Мероприятие\n    eventId INTEGER PRIMARY KEY,\n    eventContent TEXT,\n    eventProgramme VARCHAR(500),\n    eventDepartment VARCHAR(500),\n    eventFinSrc VARCHAR(500),\n    eventPatnerOrgs TEXT,\n\n    eventTaskRef INTEGER REFERENCES task (taskId) ON DELETE CASCADE\n)"
        },
        
        {
            "eventDateInterval",
                    "(    -- Интервалы дат\n    dateIntervalId INTEGER PRIMARY KEY,\n    startDate DATE,\n    endDate DATE,\n    dateIntervalEventRef INTEGER REFERENCES event (eventId)\n)"
        },

        {
            "finSource",
            "(          -- Источник финансирования для меропритий (ограниченный список)\n    finSrcId INTEGER PRIMARY KEY,\n    finSrcName VARCHAR(500)\n)"
        },

        {
            "expenseCategory",
            "(    -- Статья расхода\n    expCatId INTEGER PRIMARY KEY,\n    expCatName VARCHAR(500),\n    expCatCode VARCHAR(10),\n    expCatFinSrc VARCHAR(500)\n)"
        },

        {
            "expense",
            "(            -- Расход\n    expId INTEGER PRIMARY KEY,\n    expName VARCHAR(500),\n    expUnit VARCHAR(500),\n    expAmount VARCHAR(100),\n    expUnitCost VARCHAR(100),\n\n    expEventRef INTEGER REFERENCES event (eventId) ON DELETE CASCADE,\n    expExpCatRef INTEGER REFERENCES expenseCategory (expCatId)\n)"
        },

        {
            "admEconTask",
                    "(        -- Задача в административно-хозяйственном блоке\n    admEconTaskId INTEGER PRIMARY KEY,\n    admEconTaskCont TEXT,\n    admEconTaskProblems TEXT\n)"
        },

        {
            "admEconCI",
            "(      -- Индикатор состояния в административно-хозяйственном блоке\n    admEconCIId INTEGER PRIMARY KEY,\n    admEconCIName VARCHAR(500),\n    admEconCICurVal VARCHAR(500),\n    admEconCITargVal VARCHAR(500),\n\n    admEconCITaskRef INTEGER REFERENCES admEconTask (admEconTaskId) ON DELETE CASCADE\n)"
        },

        {
            "admEconEvent",
                    "(        -- Мероприятие в административно-хозяйственном блоке\n    admEconEventId INTEGER PRIMARY KEY,\n    admEconEventContent TEXT,\n    admEconEventProg VARCHAR(500),\n    admEconEventDep VARCHAR(500),\n    admEconEventFinSrc VARCHAR(500),\n    admEconEventPartnerOrgs TEXT,\n\n    admEconEventTaskRef INTEGER REFERENCES admEconTask (admEconTaskId) ON DELETE CASCADE\n)"
        },

        {
            "admEconEventDateInterval",
                    "(    -- Интервалы дат\n    admEconDateIntervalId INTEGER PRIMARY KEY,\n    admEconStartDate DATE,\n    admEconEndDate DATE,\n    admEconDateIntervalEventRef INTEGER REFERENCES admEconEvent (admEconEventId)\n)"
        },

        {
            "admEconExpCategory",
            "(    -- Статья расхода в административно-хозяйственном блоке\n    admEconExpCatId INTEGER PRIMARY KEY,\n    admEconExpCatName VARCHAR(500),\n    admEconExpCatCode VARCHAR(10),\n    admEconExpCatFinSrc VARCHAR(500)\n)"
        },

        {
            "admEconExpense",
            "(            -- Расход в административно-хозяйственном блоке\n    admEconExpId INTEGER PRIMARY KEY,\n    admEconExpName VARCHAR(500),\n    admEconExpUnit VARCHAR(500),\n    admEconExpAmount VARCHAR(100),\n    admEconExpUnitCost VARCHAR(100),\n    \n    admEconExpCatRef INTEGER REFERENCES expenseCategory (expCatId),\n    admEconExpEventRef INTEGER REFERENCES admEconEvent (admEconEventId) ON DELETE CASCADE\n)"
        }
    };
}
