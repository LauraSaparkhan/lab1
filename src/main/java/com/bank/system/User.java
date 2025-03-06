package com.bank.system;

public class User {
    private String username;
    private String password;
    private double balance;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = ((int)(Math.random() * 1000000))/100.0;
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String password) {
        return this.password.equals(password);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

