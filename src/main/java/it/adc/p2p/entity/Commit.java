package it.adc.p2p.entity;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Commit implements Serializable {

    public Commit(int author, String message, LocalTime time) {
        this.author = author;
        this.message = message;
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm");
        this.time = time.format(myFormatObj);
    }

    public String getTime() {
        return time;
    }

    @Override
    public boolean equals(Object object) {
        Commit commit = (Commit) object;

        return (this.author == commit.author &&
                this.message.equals(commit.message) &&
                this.time.equals(commit.time));
    }

    @Override
    public String toString() {
        return "Commit[author = " + this.author + ", message = " + this.message + ", time = " + this.time + "]";
    }

    private int author;
    private String message, time;

}
