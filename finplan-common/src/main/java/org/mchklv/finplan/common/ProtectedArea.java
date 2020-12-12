package org.mchklv.finplan.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;



public class ProtectedArea implements Serializable {
	private static final long serialVersionUID = -7890537265710674707L;
    
	private Integer id;
    private String name;
    private transient String passHash;
    private transient String passSalt;


    public ProtectedArea() {
        
    }

    public ProtectedArea(Integer id, String name) {
        this.id = id;
        this.name = name;
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

	public String getPassHash() {
		return passHash;
	}

	public void setPassHash(String passHash) {
		this.passHash = passHash;
	}

	public String getPassSalt() {
		return passSalt;
	}

	public void setPassSalt(String passSalt) {
		this.passSalt = passSalt;
	}


    public boolean isPasswordValid(String plainPassword) {
        if (passHash == null || passSalt == null) {
            return false;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            md.update(plainPassword.getBytes("UTF-8"));
            md.update(passSalt.getBytes("UTF-8"));

            return passHash.equals(stringFromBin(md.digest()));
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();

            return false;
        }
    }
    
    public void generateHash(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            passSalt = generateSalt();
            
            md.update(plainPassword.getBytes("UTF-8"));
            md.update(passSalt.getBytes("UTF-8"));

            passHash = stringFromBin(md.digest());
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
            
            passHash = null;
            passSalt = null;
        }
    }


    private String stringFromBin(byte[] binData) {
        StringBuilder sb = new StringBuilder();

        for (byte binByte : binData) {
            sb.append(String.format("%02x", binByte));
        }

        return sb.toString();
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[5];
        random.nextBytes(randomBytes);

        return stringFromBin(randomBytes);
    } 
}
