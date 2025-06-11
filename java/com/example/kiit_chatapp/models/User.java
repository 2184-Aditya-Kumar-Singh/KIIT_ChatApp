package com.example.kiit_chatapp.models;

import java.util.List;
import java.util.Map;

public class User {
    private String uid;
    private String name;
    private String branch;
    private String year;
    private List<String> interests;
    private String email;
    private String role;
    private Map<String, Boolean> joinedGroups;
    private boolean banned; // <--- Add this field

    public User() {}

    public User(String uid, String name, String branch, String year, List<String> interests, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.branch = branch;
        this.year = year;
        this.interests = interests;
        this.email = email;
        this.role = role;
    }

    public Map<String, Boolean> getJoinedGroups() {
        return joinedGroups;
    }

    public void setJoinedGroups(Map<String, Boolean> joinedGroups) {
        this.joinedGroups = joinedGroups;
    }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }
}