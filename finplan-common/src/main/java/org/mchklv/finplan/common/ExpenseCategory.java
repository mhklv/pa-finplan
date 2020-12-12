package org.mchklv.finplan.common;

import java.io.Serializable;
import java.util.LinkedList;

public class ExpenseCategory implements Serializable {
	private static final long serialVersionUID = 5382694018733888008L;
    
	private Integer id;
    private String name;
    private String categoryCode;
    private String finSource;


    public ExpenseCategory(Integer id, String name, String categoryCode, String finSource) {
        this.id = id;
        this.name = name;
        this.categoryCode = categoryCode;
        this.finSource = finSource;
    }

    public ExpenseCategory(ExpenseCategory rhs) {
        id = rhs.id;
        name = rhs.name;
        categoryCode = rhs.categoryCode;
        finSource = rhs.finSource;
    }

    
	public Integer getId() {
		return id;
	}
    
	public void setId(Integer id) {
        this.id = id;
    }

    public String getFinSource() {
		return finSource;
	}

	public void setFinSource(String finSource) {
		this.finSource = finSource;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	@Override
	public String toString() {
		return categoryCode + " " + name;
	}
}


