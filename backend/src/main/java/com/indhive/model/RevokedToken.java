package com.indhive.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class RevokedToken {
    @Id
    private String token;
    private Date revokedAt;

    public RevokedToken() {}

    public RevokedToken(String token, Date revokedAt) {
        this.token = token;
        this.revokedAt = revokedAt;
    }

    // Getters y Setters
    public Date getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Date revokedAt) {
        this.revokedAt = revokedAt;
    }
}
