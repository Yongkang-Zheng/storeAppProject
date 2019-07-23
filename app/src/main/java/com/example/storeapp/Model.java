package com.example.storeapp;


public class Model {
    private String id;
    private String name;
    private String address;
    private String distance;
    private String featureList;
    private String toggleButton;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDistance() {
        return distance;
    }

    public String getFeatureList() {
        return featureList;
    }

    public String getToggleButton() {
        return toggleButton;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setFeatureList(String featureList) {
        this.featureList = featureList;
    }

    public void setToggleButton(String toggleButton) {
        this.toggleButton = toggleButton;
    }
}