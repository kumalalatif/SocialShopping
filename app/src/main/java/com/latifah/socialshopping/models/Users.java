package com.latifah.socialshopping.models;

/**
 * Created by User on 16/12/2017.
 */

public class Users {
    private String user_id;
    private String email;
    private long phone_number;
    private String username;

    public Users(String user_id, String email, long phone_number, String username) {
        this.user_id = user_id;
        this.email = email;
        this.phone_number = phone_number;
        this.username = username;
    }
    public Users(){

    }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Users{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", phone_number=" + phone_number +
                ", username='" + username + '\'' +
                '}';
    }
}
