package com.racing.newracingapp;

public class RaceModel {

    private String raceTitle,raceId;

    public RaceModel() {
    }

    public RaceModel(String raceTitle, String raceId) {
        this.raceTitle = raceTitle;
        this.raceId = raceId;
    }

    public String getRaceTitle() {
        return raceTitle;
    }

    public void setRaceTitle(String raceTitle) {
        this.raceTitle = raceTitle;
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }
}
