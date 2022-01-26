package it.adc.p2p.entity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Commit {

    public Commit(int peerId, String message, LocalTime time) {
        this.peerId = peerId;
        this.message = message;
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm");
        this.time = time.format(myFormatObj);
    }

    private int getPeerId() {
        return peerId;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    @Override
    public boolean equals(Object object) {
        Commit commit = (Commit) object;

        return (this.peerId == commit.peerId &&
                this.message.equals(commit.message) &&
                this.time.equals(commit.time));
    }

    @Override
    public String toString() {
        return "Commit[peerId = " + this.peerId + ", message = " + this.message + ", time = " + this.time + "]";
    }

    private int peerId;
    private String message, time;

}
