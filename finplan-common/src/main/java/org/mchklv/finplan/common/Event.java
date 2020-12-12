package org.mchklv.finplan.common;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;


public class Event implements Serializable {
	private static final long serialVersionUID = 8738032454221528400L;

    private Integer id;
    private String content;
    private String programme;
    private String department;
    private String finSource;
    private List<LocalDateInterval> dateIntervals;
    private String partnerOrgs;
    private LinkedList<Expense> expenses;

    
    public Event(Integer id, String content, String programme, String department) {
        this.id = id;
        this.content = content;
        this.programme = programme;
        this.department = department;
    }

    public String getFinSource() {
		return finSource;
	}

	public void setFinSource(String finSource) {
		this.finSource = finSource;
	}

	public Event(Event rhs) {
        id = rhs.id;
        content = rhs.content;
        programme = rhs.programme;
        department = rhs.department;
        finSource = rhs.finSource;
        dateIntervals = rhs.dateIntervals;
        partnerOrgs = rhs.partnerOrgs;
        expenses = rhs.expenses;
    }


    public FixedPointDec getExpensesSum() {
        FixedPointDec sum = new FixedPointDec("0");
        
        if (expenses == null || expenses.isEmpty()) {
            return sum;
        }
        
        for (Expense exp : expenses) {
            sum.add(exp.getAmount().multiplied(exp.getUnitCost()));
        }

        return sum;
    }

    
	public Integer getId() {
		return id;
	}

    public void setId(Integer id) {
        this.id = id;
    }

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getProgramme() {
		return programme;
	}

	public void setProgramme(String programme) {
		this.programme = programme;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
	
	public String getPartnerOrgs() {
		return partnerOrgs;
	}

	public void setPartnerOrgs(String partnerOrgs) {
		this.partnerOrgs = partnerOrgs;
	}

	public LinkedList<Expense> getExpenses() {
		return expenses;
	}

	public void setExpenses(LinkedList<Expense> expenses) {
		this.expenses = expenses;
	}

	public List<LocalDateInterval> getDateIntervals() {
		return dateIntervals;
	}

	public void setDateIntervals(List<LocalDateInterval> dateIntervals) {
		this.dateIntervals = dateIntervals;
	}
}
