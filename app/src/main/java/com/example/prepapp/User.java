package com.example.prepapp;

public class User {

    private String email;
    private String id;
    private String username;

    public User(){

    }

    public User(String email, String id, String username){
        this.email = email;
        this.id = id;
        this.username = username;
    }

    public String getEmail(){
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
