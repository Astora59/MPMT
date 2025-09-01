package com.projectManager.pmt.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class Users {


    @Id //let us know it's our primary key
    @GeneratedValue
    @Column()
    private UUID users_id;


    private String username;

    private String email;

    private String passwordHash;

    //users_id getter and setter
    public UUID getUsers_id() {
        return users_id;
    }

    public void setUsers_id(UUID users_id) {
        this.users_id = users_id;
    }

    //username getter and setter

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //email getter and setter

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //password_hash getter and setter


    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
