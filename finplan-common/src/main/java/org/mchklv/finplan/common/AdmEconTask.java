package org.mchklv.finplan.common;

import java.io.Serializable;
import java.util.List;

public class AdmEconTask implements Serializable {
	private static final long serialVersionUID = 4156880850624106418L;
    
	private Integer id;
    private String content;
    private String problems;
    private List<ConditionIndicator> admEconCondIndicators;
    private List<Event> admEconEvents;


    public AdmEconTask(Integer id, String content, String problems) {
        this.id = id;
        this.content = content;
        this.problems = problems;
    }
    
	public AdmEconTask(AdmEconTask rhs) {
        this.id = rhs.id;
        this.content = rhs.content;
        this.problems = rhs.problems;
        this.admEconCondIndicators = rhs.admEconCondIndicators;
        this.admEconEvents = rhs.admEconEvents;
    }

    
    public String getProblems() {
		return problems;
	}

	public void setProblems(String problems) {
		this.problems = problems;
	}
    
	public List<Event> getAdmEconEvents() {
		return admEconEvents;
	}

	public void setAdmEconEvents(List<Event> admEconEvents) {
		this.admEconEvents = admEconEvents;
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

	public List<ConditionIndicator> getAdmEconCondIndicators() {
		return admEconCondIndicators;
	}

	public void setAdmEconCondIndicators(List<ConditionIndicator> admEconCondIndicators) {
		this.admEconCondIndicators = admEconCondIndicators;
	}
}
