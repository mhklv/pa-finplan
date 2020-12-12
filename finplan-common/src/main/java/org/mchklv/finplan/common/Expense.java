package org.mchklv.finplan.common;

import java.io.Serializable;

public class Expense implements Serializable {
	private static final long serialVersionUID = -6266359791385719044L;
    
	private Integer id;
    private String name;
    private String unit;
    private FixedPointDec amount;
    private FixedPointDec unitCost;
    private Integer expCatId;


    public Expense(Integer id, String name, String unit,
                   FixedPointDec amount, FixedPointDec unitCost,
                   Integer expCatId) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.amount = amount;
        this.unitCost = unitCost;
        this.expCatId = expCatId;
    }

    public Integer getExpCatId() {
		return expCatId;
	}

	public void setExpCatId(Integer expCatId) {
		this.expCatId = expCatId;
	}

	public Expense(Expense rhs) {
        id = rhs.id;
        name = rhs.name;
        unit = rhs.unit;
        amount = rhs.amount;
        unitCost = rhs.unitCost;
        expCatId = rhs.expCatId;
    }

    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public FixedPointDec getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(FixedPointDec unitCost) {
        this.unitCost = unitCost;
    }

    public FixedPointDec getAmount() {
        return amount;
    }

    public void setAmount(FixedPointDec amount) {
        this.amount = amount;
    }


	// @Override
	// public int hashCode() {
	// 	final int prime = 31;
	// 	int result = 1;
	// 	result = prime * result + ((amount == null) ? 0 : amount.hashCode());
	// 	result = prime * result + ((expCatId == null) ? 0 : expCatId.hashCode());
	// 	result = prime * result + ((id == null) ? 0 : id.hashCode());
	// 	result = prime * result + ((name == null) ? 0 : name.hashCode());
	// 	result = prime * result + ((unit == null) ? 0 : unit.hashCode());
	// 	result = prime * result + ((unitCost == null) ? 0 : unitCost.hashCode());
	// 	return result;
	// }


	// @Override
	// public boolean equals(Object obj) {
	// 	if (this == obj)
	// 		return true;
	// 	if (obj == null)
	// 		return false;
	// 	if (getClass() != obj.getClass())
	// 		return false;
	// 	Expense other = (Expense) obj;
	// 	if (amount == null) {
	// 		if (other.amount != null)
	// 			return false;
	// 	} else if (!amount.equals(other.amount))
	// 		return false;
	// 	if (expCatId == null) {
	// 		if (other.expCatId != null)
	// 			return false;
	// 	} else if (!expCatId.equals(other.expCatId))
	// 		return false;
	// 	if (id == null) {
	// 		if (other.id != null)
	// 			return false;
	// 	} else if (!id.equals(other.id))
	// 		return false;
	// 	if (name == null) {
	// 		if (other.name != null)
	// 			return false;
	// 	} else if (!name.equals(other.name))
	// 		return false;
	// 	if (unit == null) {
	// 		if (other.unit != null)
	// 			return false;
	// 	} else if (!unit.equals(other.unit))
	// 		return false;
	// 	if (unitCost == null) {
	// 		if (other.unitCost != null)
	// 			return false;
	// 	} else if (!unitCost.equals(other.unitCost))
	// 		return false;
	// 	return true;
	// }
}
