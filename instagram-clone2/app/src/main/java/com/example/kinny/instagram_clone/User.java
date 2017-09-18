package com.example.kinny.instagram_clone;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by kinny on 9/18/2017.
 */

public class User {
    public String email;
    public String password;

    public User(){}

    public User(String username, String password){
        this.email = username;
        this.password = password;
    }
}
