package com.example.hobbyzooapp.Sessions;

import java.sql.Time;

public class Session {

    private String sessionId,activityId,activityName, image;
    private Time time;
    private int month, day, year;


    public Session(String id, String activityId, String activityName,Time time,
                   int day, int month , int year, String image) {
        this.sessionId = id;
        this.time = time;
        this.activityId=activityId;
        this.day = day;
        this.image=image;
        this.month = month;
        this.year = year;
        this.activityName=activityName;
    }

    public String getImage() {
        return image;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getName() {return sessionId;}

    public Time getTime() {return time;}

    public int getMonth() {return month;}

    public int getDay() {return day;}

    public int getYear() {return year;}
}