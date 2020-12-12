package org.mchklv.finplan.common;

import java.io.Serializable;

public class ConditionIndicator implements Serializable {
	private static final long serialVersionUID = 4740896867266091260L;
    
	private Integer id;
    private String name;
    private String currentValue;
    private String targetValue;


    public ConditionIndicator(Integer id, String name, String currentValue, String targetValue) {
        this.id = id;
        this.name = name;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
    }

    public ConditionIndicator(ConditionIndicator rhs) {
        id = rhs.id;
        name = rhs.name;
        currentValue = rhs.currentValue;
        targetValue = rhs.targetValue;
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

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentValue == null) ? 0 : currentValue.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((targetValue == null) ? 0 : targetValue.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConditionIndicator other = (ConditionIndicator) obj;
		if (currentValue == null) {
			if (other.currentValue != null)
				return false;
		} else if (!currentValue.equals(other.currentValue))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (targetValue == null) {
			if (other.targetValue != null)
				return false;
		} else if (!targetValue.equals(other.targetValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConditionIndicator [currentValue=" + currentValue + ", id=" + id + ", name=" + name + ", targetValue="
				+ targetValue + "]";
	}
}
