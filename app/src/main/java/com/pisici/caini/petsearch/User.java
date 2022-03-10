package com.pisici.caini.petsearch;

import java.io.Serializable;

public class User implements Serializable {
    private String First_name = "null";
    private String Last_name = "null";
    private String Email= "null";
    private String Phone= "null";
    private String PetId="null";
    private boolean missing=false;

    public User(String first_name,String last_name,String email, String phone,String petId) {
        First_name = first_name;
        Last_name=last_name;
        Email=email;
        Phone=phone;
        PetId=petId;
        missing=false;
    }
    public User(){}

    public String getFirst_name() {
        return First_name;
    }

    public String getLast_name() {
        return Last_name;
    }

    public String getEmail() {
        return Email;
    }

    public String getPhone() {
        return Phone;
    }

    public void setFirst_name(String first_name) {
        First_name = first_name;
    }

    public void setLast_name(String last_name) {
        Last_name = last_name;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPetId() {
        return PetId;
    }

    public void setPetId(String petId) {
        PetId = petId;
    }

    public boolean isMissing() {
        return missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }
}
