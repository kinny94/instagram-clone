package com.example.kinny.instagram_clone;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by kinny on 9/18/2017.
 */

public class User {
    public String email;
    public String password;
    public String username;

    public User(){}

    public User(String email, String password, String username){
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
