package main.pt.ipleiria.estg.dei.model;

public class Website {
    private String domain;
    private String urlFull;
    private int visitCount;
    private String date;
    private String username;

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
}
