package org.mchklv.finplan.common;

import java.io.Serializable;
import java.util.LinkedList;

public class KeyValueGroup implements Serializable {
	private static final long serialVersionUID = -8342223438605458276L;
    
	private Integer id;
    private String name;
    private LinkedList<KeyValue> keyValues;

    
    public KeyValueGroup(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public KeyValueGroup(KeyValueGroup rhs) {
        id = rhs.id;
        name = rhs.name;
        keyValues = rhs.keyValues;
    }
    

	public LinkedList<KeyValue> getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(LinkedList<KeyValue> keyValues) {
		this.keyValues = keyValues;
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


	@Override
	public int hashCode() {
		return super.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}
