package main.pt.ipleiria.estg.dei.model;

public class Website {
    private String domain;
    private int visitCount;

    public Website( String domain, int visitCount) {
        this.domain = domain;
        this.visitCount = visitCount;
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
}
