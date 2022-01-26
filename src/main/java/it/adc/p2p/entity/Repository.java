package it.adc.p2p.entity;

import java.io.File;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;

import net.tomp2p.peers.PeerAddress;

public class Repository implements Serializable {

    public Repository(File directory, String name, PeerAddress peerAddress, int id) {
        this.directory = directory;
        this.name = name;
        peerAddresses = new HashSet<>();
        peerAddresses.add(peerAddress);
        this.id = id;
        files = new HashSet<>();
        commits = new ArrayList<>();
    }

    public String getDirectoryName() {
        return directory.getName();
    }

    public String getName() {
        return name;
    }

    public HashSet<PeerAddress> getPeerAddresses() {
        return peerAddresses;
    }

    public int getId() {
        return id;
    }

    public HashSet<File> getFiles() {
        return files;
    }

    public ArrayList<Commit> getCommits() {
        return commits;
    }

    public boolean addPeerAddresses(HashSet<PeerAddress> peerAddresses) {
        if (peerAddresses.isEmpty())
            return false;
        if (this.peerAddresses.containsAll(peerAddresses))
            return true;

        return this.peerAddresses.addAll(peerAddresses);
    }

    public int checkCommits(ArrayList<Commit> compareList) {
        if (commits.equals(compareList))
            return 0;
        
        if (commits.size() > compareList.size())
            return 1;
        else
            return 2;
    }

    public boolean addFiles(ArrayList<File> files) {
        if (files.isEmpty())
            return false;

        for (File f : files)
            this.files.add(f);

        return true;
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
        if (!this.commits.addAll(commits))
            return false;
        else {
            this.commits.sort(new SortByTime());
            return true;
        }
    }

    private static final long serialVersionUID = 1L;
    private File directory;
    private String name;
    private HashSet<PeerAddress> peerAddresses;
    private int id;
    private HashSet<File> files;
    private ArrayList<Commit> commits;
    
}