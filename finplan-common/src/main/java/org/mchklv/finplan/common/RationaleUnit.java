package org.mchklv.finplan.common;

import java.io.Serializable;
import java.util.LinkedList;

public class RationaleUnit implements Serializable {
	private static final long serialVersionUID = -4208869373353586215L;
    
	private Integer id;
    private String threat;
    private String threatReasons;
    private String problems;
    private LinkedList<ConditionIndicator> condIndicators;
    private LinkedList<Task> tasks;

    
    public RationaleUnit(Integer id, String threat, String threatReasons, String problems) {
        this.id = id;
        this.threat = threat;
        this.threatReasons = threatReasons;
        this.problems = problems;

        // condIndicators = new LinkedList<ConditionIndicator>();
        // tasks = new LinkedList<Task>();
    }

    public RationaleUnit(RationaleUnit rhs) {
        id = rhs.id;
        threat = rhs.threat;
        threatReasons = rhs.threatReasons;
        problems = rhs.problems;
        condIndicators = rhs.condIndicators;
        tasks = rhs.tasks;
    } 

    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getThreat() {
        return threat;
    }

    public void setThreat(String threat) {
        this.threat = threat;
    }

    public String getThreatReasons() {
        return threatReasons;
    }

    public void setThreatReasons(String threatReasons) {
        this.threatReasons = threatReasons;
    }

    public String getProblems() {
        return problems;
    }

    public void setProblems(String problems) {
        this.problems = problems;
    }

    public LinkedList<ConditionIndicator> getCondIndicators() {
        return condIndicators;
    }

    public void setCondIndicators(LinkedList<ConditionIndicator> condIndicators) {
        this.condIndicators = condIndicators;
    }

    public LinkedList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(LinkedList<Task> tasks) {
        this.tasks = tasks;
    }
}
