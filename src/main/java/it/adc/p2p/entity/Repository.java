package it.adc.p2p.entity;

import java.io.File;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;

import net.tomp2p.peers.PeerAddress;

public class Repository implements Serializable {

    public Repository(File directory, String name, PeerAddress peerAddress) {
        this.name = name;
        peerAddresses = new HashSet<>();
        peerAddresses.add(peerAddress);
        files = new HashSet<>();
        commits = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public HashSet<PeerAddress> getContributors() {
        return peerAddresses;
    }

    public HashSet<File> getFiles() {
        return files;
    }

    public ArrayList<Commit> getCommits() {
        return commits;
    }

    public boolean addContributors(HashSet<PeerAddress> peerAddresses) {
        if (peerAddresses.isEmpty())
            return false;
        if (this.peerAddresses.containsAll(peerAddresses))
            return true;

        return this.peerAddresses.addAll(peerAddresses);
    }

    public boolean addFiles(ArrayList<File> files) {
        if (files.isEmpty())
            return false;
        if (this.files.containsAll(files))
            return false;

        return this.files.addAll(files);
    }

    public boolean addFiles(HashSet<File> files) {
        if (files.isEmpty())
            return false;
        if (this.files.containsAll(files))
            return true;

        return this.files.addAll(files);
    }

    public boolean addCommit(int id, String message, LocalTime time) {
        return commits.add(new Commit(id, message, time));
    }

    public boolean addCommits(ArrayList<Commit> commits) {
        if (commits.isEmpty())
            return false;
        if (this.commits.containsAll(commits))
            return true;

        for(Commit commit : commits)
            if (!this.commits.contains(commit))
                this.commits.add(commit);

        this.commits.sort(new SortByTime());
        return true;
    }

    public boolean checkCommits(ArrayList<Commit> compareList) {
        return commits.containsAll(compareList);
    }

    private String name;
    private HashSet<PeerAddress> peerAddresses;
    private HashSet<File> files;
    private ArrayList<Commit> commits;
    
}