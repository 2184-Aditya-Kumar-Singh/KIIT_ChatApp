package com.example.kiit_chatapp.models;

import java.util.List;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String senderRole;
    private String text;
    private String type;
    private String timeStamp;
    private String imageUrl;
    private boolean seen;
    private List<String> mentionedEmails;
    private List<String> tagSeenBy; // <-- NEW FIELD!

    public Message() {}

    public Message(String messageId, String senderId, String senderName, String senderEmail, String senderRole, String text, String type, String timeStamp, String fileUrl, List<String> mentionedEmails) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderRole = senderRole;
        this.text = text;
        this.type = type;
        this.timeStamp = timeStamp;
        this.imageUrl = fileUrl;
        this.mentionedEmails = mentionedEmails;
    }

    public Message(String messageId, String senderId, String senderName, String senderEmail, String senderRole, String text, String type, String timeStamp, String fileUrl) {
        this(messageId, senderId, senderName, senderEmail, senderRole, text, type, timeStamp, fileUrl, null);
    }

    public Message(String messageId, String senderId, String senderName, String senderEmail, String senderRole, String text, String type, String timeStamp, String fileUrl, boolean seen, List<String> mentionedEmails) {
        this(messageId, senderId, senderName, senderEmail, senderRole, text, type, timeStamp, fileUrl, mentionedEmails);
        this.seen = seen;
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

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

    public String getFileUrl() { return imageUrl; }
    public void setFileUrl(String fileUrl) { this.imageUrl = fileUrl; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public List<String> getMentionedEmails() { return mentionedEmails; }
    public void setMentionedEmails(List<String> mentionedEmails) { this.mentionedEmails = mentionedEmails; }

    public List<String> getTagSeenBy() { return tagSeenBy; }
    public void setTagSeenBy(List<String> tagSeenBy) { this.tagSeenBy = tagSeenBy; }
}