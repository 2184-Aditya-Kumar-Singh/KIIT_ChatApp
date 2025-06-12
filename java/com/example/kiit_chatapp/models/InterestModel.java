package com.example.kiit_chatapp.models;

public class InterestModel {
    private String name;
    private boolean selected;

    public InterestModel(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public String getName() { return name; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
