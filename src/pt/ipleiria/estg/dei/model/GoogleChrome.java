package pt.ipleiria.estg.dei.model;

public class GoogleChrome {
    private String url;
    private int visitNumber;

    public GoogleChrome(String url, int visitNumber){
        this.url = url;
        this.visitNumber = visitNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(int visitNumber) {
        this.visitNumber = visitNumber;
    }

    @Override
    public String toString() {
        return "Site: " + url + " has been visited: " + visitNumber;
    }
}
