package org.mchklv.finplan.common.messageBundles;

import java.io.Serializable;

import org.mchklv.finplan.common.ProtectedArea;

public class AuthorizationBundle implements Serializable {
	private static final long serialVersionUID = -7954059636274642068L;
    
	private String answer;
    private String plainPassword;
    private ProtectedArea protectedArea;


    public AuthorizationBundle(ProtectedArea protectedArea, String plainPassword,
                               String answer) {
        setProtectedArea(protectedArea);
        setPlainPassword(plainPassword);
        setAnswer(answer);
    }
    
    
	public String getAnswer() {
		return answer;
	}
    
	public void setAnswer(String answer) {
		this.answer = answer;
	}
    
	public String getPlainPassword() {
		return plainPassword;
	}
    
	public void setPlainPassword(String plainPassword) {
		this.plainPassword = plainPassword;
	}
    
	public ProtectedArea getProtectedArea() {
		return protectedArea;
	}
    
	public void setProtectedArea(ProtectedArea protectedArea) {
		this.protectedArea = protectedArea;
	}
}
