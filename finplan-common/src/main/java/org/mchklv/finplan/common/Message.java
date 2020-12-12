package org.mchklv.finplan.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = -6953473837623829475L;

    private int command;
    private int subCommand;
    private Object payload;


    public Message(int command, int subCommand, Object payload) {
        setCommand(command);
        setSubCommand(subCommand);
        setPayload(payload);
    }

    public Message(int command, int subCommand) {
        setCommand(command);
        setSubCommand(subCommand);
    }
    

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getSubCommand() {
        return subCommand;
    }

    public void setSubCommand(int subCommand) {
        this.subCommand = subCommand;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

	@Override
	public String toString() {
        String payloadString = (payload == null) ? "Null" : payload.toString();
		return "Message [command=" + command + ", payload=" + payloadString + ", subCommand=" + subCommand + "]";
	}
}
