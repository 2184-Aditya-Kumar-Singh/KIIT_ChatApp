package com.example.kiit_chatapp.models;

import java.util.List;
import java.util.Map;

public class Group {
    private String id;
    private String name;
    private String topic;
    private Map<String, Boolean> members;

    public Group() {
    }

    public Group(String id, String name, String topic, Map<String, Boolean> members) {
        this.id = id;
        this.name = name;
        this.topic = topic;
        this.members = members;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }
}
