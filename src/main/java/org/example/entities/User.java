package org.example.entities;

public class User {
    private final String username;
    private final String role;
    private final String email;
    private final String password;
    private final int id;

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }

    public boolean checkPassword(String  password) {
        return this.password.equals(password);
    }

    @Override
    public String toString(){
        return "Username: " + this.username + "\nE-mail:" + this.email;
    }

    public User(String username, String role, String email, String password, int id) {
        this.username = username;
        this.role = role;
        this.email = email;
        this.password = password;
        this.id = id;
    }
}
