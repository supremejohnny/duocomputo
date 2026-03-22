package com.duocomputo.model;

import java.time.Instant;

public class User {
    private String UserId;
    private String username;
    private String passwordHash;
    private Instant createAt;
    private Instant lastLoginAt;

    public String getUserId() { return UserId; }
    public void setUserId(String UserId) { this.UserId = UserId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Instant getCreateAt() { return createAt; }
    public void setCreateAt(Instant createAt) { this.createAt = createAt; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

}
