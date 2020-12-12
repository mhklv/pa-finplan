package org.mchklv.finplan.common;

import java.io.Serializable;
import java.util.LinkedList;

public class KeyValue implements Serializable {
	private static final long serialVersionUID = 6037165475637875981L;
    
	private Integer id;
    private String name;
    private String description;
    private LinkedList<RationaleUnit> rationaleUnits;

    
    public KeyValue(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public KeyValue(KeyValue rhs) {
        id = rhs.id;
        name = rhs.name;
        description = rhs.description;
        rationaleUnits = rhs.rationaleUnits;
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

	public String getDescription() {
		return description;
	}

    public void setDescription(String description) {
        this.description = description;
    }

	public LinkedList<RationaleUnit> getRationaleUnits() {
		return rationaleUnits;
	}
    
	public void setRationaleUnits(LinkedList<RationaleUnit> rationaleUnits) {
		this.rationaleUnits = rationaleUnits;
	}
}
