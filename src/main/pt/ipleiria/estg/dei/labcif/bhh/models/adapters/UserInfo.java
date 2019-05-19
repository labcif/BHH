package main.pt.ipleiria.estg.dei.labcif.bhh.models.adapters;

import main.pt.ipleiria.estg.dei.labcif.bhh.models.Login;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Website;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.util.List;

public class UserInfo {
    private String username;
    private JRDataSource mostVisitedWebsites;
    private JRDataSource blockedVisitedWebsites;
    private JRDataSource logins;


    public UserInfo(String username, List<Website> mostVisitedWebsites, List<Website> blockedVisitedWebsites, List<Login> logins) {
        this.mostVisitedWebsites = new JRBeanCollectionDataSource(mostVisitedWebsites);
        this.blockedVisitedWebsites = new JRBeanCollectionDataSource(blockedVisitedWebsites);
        this.username = username;
        this.logins = new JRBeanCollectionDataSource(logins);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JRDataSource getMostVisitedWebsites() {
        return mostVisitedWebsites;
    }

    public void setMostVisitedWebsites(JRDataSource mostVisitedWebsites) {
        this.mostVisitedWebsites = mostVisitedWebsites;
    }

    public JRDataSource getBlockedVisitedWebsites() {
        return blockedVisitedWebsites;
    }

    public void setBlockedVisitedWebsites(JRDataSource blockedVisitedWebsites) {
        this.blockedVisitedWebsites = blockedVisitedWebsites;
    }

    public JRDataSource getLogins() {
        return logins;
    }

    public void setLogins(JRDataSource logins) {
        this.logins = logins;
    }
}

