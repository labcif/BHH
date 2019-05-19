package main.pt.ipleiria.estg.dei.labcif.bhh.models;

import java.sql.Timestamp;

public class Website {
    private String domain;
    private String urlFull;
    private int visitCount;
    private String date;
    private String username;
    private Timestamp dateStart;
    private Timestamp dateEnd;
    private Double percentage;

    public Website( String domain, int visitCount) {
        this.domain = domain;
        this.visitCount = visitCount;
    }

    public Website(String domain, String date, String urlFull, String username) {
        this.domain = domain;
        this.date = date;
        this.urlFull = urlFull;
        this.username = username;
    }

    public Website(String domain, String username, Timestamp dateStart, Timestamp dateEnd) {
        this.domain = domain;
        this.username = username;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.percentage = null;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public String getUrlFull() {
        return urlFull;
    }

    public void setUrlFull(String urlFull) {
        this.urlFull = urlFull;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getDateStart() {
        return dateStart;
    }

    public void setDateStart(Timestamp dateStart) {
        this.dateStart = dateStart;
    }

    public Timestamp getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Timestamp dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
