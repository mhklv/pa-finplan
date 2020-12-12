package org.mchklv.finplan.common;

import java.io.Serializable;
import java.util.LinkedList;

public class Task implements Serializable {
	private static final long serialVersionUID = -5518123664400322979L;
    
	private Integer id;
    private String content;
    private LinkedList<Event> events;

    
    public Task(Integer id, String content) {
        this.id = id;
        this.content = content;
    }

    public Task(Task rhs) {
        id = rhs.id;
        content = rhs.content;
        events = rhs.events;
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

	public LinkedList<Event> getEvents() {
		return events;
	}

	public void setEvents(LinkedList<Event> events) {
		this.events = events;
	}
}
