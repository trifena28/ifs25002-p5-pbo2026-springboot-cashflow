package org.delcom.ifs25002springbootcashflow.domain;

import java.util.UUID;

public class User {
    private String id;
    private String nama;
    private String username;
    private String password;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String nama, String username, String password) {
        this.id = UUID.randomUUID().toString();
        this.nama = nama;
        this.username = username;
        this.password = password;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}