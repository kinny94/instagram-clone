package com.example.kinny.instagram_clone;

/**
 * Created by kinny on 9/18/2017.
 */

public class User {
    public String username;
    public String password;
    public String email;

    public User(){}

    public User(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
