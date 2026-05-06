package com.vis.model;

import java.time.LocalDateTime;

public class Alert extends Thing {
    private int senderId;
    private Integer recipientId; // Can be null
    private String message;
    private String type;
    private boolean isBroadcast;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Alert() {}

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public Integer getRecipientId() { return recipientId; }
    public void setRecipientId(Integer recipientId) { this.recipientId = recipientId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isBroadcast() { return isBroadcast; }
    public void setBroadcast(boolean broadcast) { isBroadcast = broadcast; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    @Override
    public String getSummary() {
        return (isBroadcast ? "[BROADCAST] " : "") + message;
    }
}
