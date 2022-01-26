package it.adc.p2p.entity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Commit {

    public Commit(int id, String message, LocalTime time) {
        this.id = id;
        this.message = message;
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm");
        this.time = time.format(myFormatObj);
    }

    private int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public boolean equals(Object object) {
        Commit commit = (Commit) object;

        return (this.id == commit.id &&
                this.message.equals(commit.message) &&
                this.time.equals(commit.time));
    }

    public String toString() {
        return "Commit[id = " + this.id + ", message = " + this.message + ", time = " + this.time + "]";
    }

    private int id;
    private String message, time;

}
