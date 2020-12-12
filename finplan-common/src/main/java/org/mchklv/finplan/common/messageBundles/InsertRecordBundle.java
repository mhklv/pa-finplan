package org.mchklv.finplan.common.messageBundles;

import java.io.Serializable;

public class InsertRecordBundle implements Serializable {
	private static final long serialVersionUID = 1903487859734592345L;
    
	private Object recordObj;
    private Object parentObj;


    public InsertRecordBundle(Object record, Object parent) {
        setRecordObj(record);
        setParentObj(parent);
    }

    
	public Object getRecordObj() {
		return recordObj;
	}
    
	public void setRecordObj(Object recordObj) {
		this.recordObj = recordObj;
	}
    
	public Object getParentObj() {
		return parentObj;
	}
    
	public void setParentObj(Object parentObj) {
		this.parentObj = parentObj;
	}
}
