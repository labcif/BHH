package main.pt.ipleiria.estg.dei.labcif.bhh.dtos;

import java.io.Serializable;

public class RelativeFrequencyBrowser implements Serializable {
    private String Domain;
    private int totalDaysVisited;
    private float totalDaysDB;
    private float frequency;

    public RelativeFrequencyBrowser(String domain, int totalDaysVisited, float totalDaysDB) {
        Domain = domain;
        this.totalDaysVisited = totalDaysVisited;
        this.totalDaysDB = totalDaysDB;
        frequency = (float)this.totalDaysVisited / this.totalDaysDB;
    }

    public String getDomain() {
        return Domain;
    }

    public void setDomain(String domain) {
        Domain = domain;
    }

    public int getTotalDaysVisited() {
        return totalDaysVisited;
    }

    public void setTotalDaysVisited(int totalDaysVisited) {
        this.totalDaysVisited = totalDaysVisited;
    }

    public float getTotalDaysDB() {
        return totalDaysDB;
    }

    public void setTotalDaysDB(float totalDaysDB) {
        this.totalDaysDB = totalDaysDB;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }
}
