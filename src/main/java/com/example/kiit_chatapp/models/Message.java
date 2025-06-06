package com.example.kiit_chatapp.models;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String senderEmail; // <-- NEW
    private String senderRole;  // <-- NEW
    private String text;
    private String type;
    private String timeStamp;
    private String fileUrl;
    private boolean seen;

    public Message() {}

    // Constructor without seen
    public Message(String messageId, String senderId, String senderName, String senderEmail, String senderRole, String text, String type, String timeStamp, String fileUrl) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderRole = senderRole;
        this.text = text;
        this.type = type;
        this.timeStamp = timeStamp;
        this.fileUrl = fileUrl;
    }

    // Constructor with seen
    public Message(String messageId, String senderId, String senderName, String senderEmail, String senderRole, String text, String type, String timeStamp, String fileUrl, boolean seen) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderRole = senderRole;
        this.text = text;
        this.type = type;
        this.timeStamp = timeStamp;
        this.fileUrl = fileUrl;
        this.seen = seen;
    }

    // Getters and setters for all fields

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}