package com.example.studentscoremanager.model;

public class UserAccount {
    private long id;
    private String username;
    private String password;
    private String role;
    private String realName;

    public UserAccount() {
    }

    public UserAccount(long id, String username, String password, String role, String realName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.realName = realName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}

