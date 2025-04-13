package com.geradev.mobilalk;

import java.util.Date;

public class MeterReading {
    private String id;
    private double reading;
    private Date date;
    private String userId;

    public MeterReading() {
    }

    public MeterReading(double reading, Date date, String userId) {
        this.reading = reading;
        this.date = date;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getReading() {
        return reading;
    }

    public void setReading(double reading) {
        this.reading = reading;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}