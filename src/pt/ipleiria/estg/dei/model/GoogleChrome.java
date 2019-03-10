package pt.ipleiria.estg.dei.model;

import java.io.Serializable;
import java.util.Date;

public class GoogleChrome implements Serializable {

    /**Primary key used to link all tables*/
    private int id;

    /**
     * Stores a unique visited URL.
     * Original column name: url
     * Table: urls
     */
    private String url;

    /**
     * Stores the total visit count for this URL.
     * Original column name: visit_count
     * Table: urls
     */
    private int visitCount;

    /**
     * Stores the URL’s page title.
     * Original column name: title
     * Table: urls
     */
    private String pageTitle;

    /**
     * Stores the number of times the URL was typed manually
     * Original column name: typed_count
     * Tables: urls
     */
    private int typedCount;

    /**
     * Stores the last time the URL was visited. This is stored in Chrome’s
     * time format.
     * Original column name: last_visit_time
     * Table: urls
     */
    private Date lastVisitTime;

    /**
     * Indicates if the URL will be displayed by the autocomplete function. A
     * value of 1 will keep it hidden and 0 will display it.
     * Original column name: hidden
     * Table: urls
     */
    private boolean hidden;

    /**
     * Stores the time the URL was visited in Chrome’s time format
     * Original column name: visit_time
     * Table: visits
     */
    private Date visitTime;//

    /**
     * Stores the id of the referring URL. If there is no referrer, stores the value 0.
     * Original column name: from_visit
     * Table: visits
     */
    private int fromVisit;


    /**
     * This value describes how the URL was loaded in the browser.
     */
    private long transition;//transition

    public GoogleChrome(String url, int visitCount){
        this.url = url;
        this.visitCount = visitCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public int getTypedCount() {
        return typedCount;
    }

    public void setTypedCount(int typedCount) {
        this.typedCount = typedCount;
    }

    public Date getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(Date lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Date getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Date visitTime) {
        this.visitTime = visitTime;
    }

    public int getFromVisit() {
        return fromVisit;
    }

    public void setFromVisit(int fromVisit) {
        this.fromVisit = fromVisit;
    }

    public long getTransition() {
        return transition;
    }

    public void setTransition(long transition) {
        this.transition = transition;
    }

    @Override
    public String toString() {
        return "Site: " + url + " has been visited: " + visitCount;
    }
}
