package org.mchklv.finplan.common.messageBundles;

import java.io.Serializable;

import org.mchklv.finplan.common.ProtectedArea;

public class RegistrationBundle implements Serializable {
	private static final long serialVersionUID = 4723923671108861571L;

    private String answer;
    private String plainPassword;
	private String regQuotaKey;
    private ProtectedArea protectedArea;


    public RegistrationBundle(String regQuotaKey, ProtectedArea protectedArea,
                              String plainPassword, String answer) {
        setRegQuotaKey(regQuotaKey);
        setProtectedArea(protectedArea);
        setPlainPassword(plainPassword);
        setAnswer(answer);
    }
    
    
	public String getPlainPassword() {
		return plainPassword;
	}

	public void setPlainPassword(String plainPassword) {
		this.plainPassword = plainPassword;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getRegQuotaKey() {
		return regQuotaKey;
	}
    
	public void setRegQuotaKey(String regQuotaKey) {
        if (regQuotaKey.length() != 16) {
            throw new IllegalArgumentException("Registration key must be exactly 16 characters long");
        }
        
		this.regQuotaKey = regQuotaKey;
	}
    
	public ProtectedArea getProtectedArea() {
		return protectedArea;
	}
    
	public void setProtectedArea(ProtectedArea protectedArea) {
		this.protectedArea = protectedArea;
	}
}
