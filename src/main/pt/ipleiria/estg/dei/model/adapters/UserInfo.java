package main.pt.ipleiria.estg.dei.model.adapters;

import main.pt.ipleiria.estg.dei.model.Website;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.util.List;

public class UserInfo {
    private String username;
    private JRDataSource mostVisitedWebsites;
    private JRDataSource blockedVisitedWebsites;


    public UserInfo(String username, List<Website> mostVisitedWebsites, List<Website> blockedVisitedWebsites ) {
        this.mostVisitedWebsites = new JRBeanCollectionDataSource(mostVisitedWebsites);
        this.blockedVisitedWebsites = new JRBeanCollectionDataSource(blockedVisitedWebsites);
        this.username = username;
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
}

