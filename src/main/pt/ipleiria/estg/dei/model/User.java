package main.pt.ipleiria.estg.dei.model;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.util.List;

public class User {
    private String name;
    private JRDataSource userWebsitesVisited;
    private JRDataSource userWebsitesBlocked;

    public User() {
    }

    public User(String name, List<Website> userWebsitesVisited, List<Website> userWebsitesBlocked ) {
        this.name = name;
        this.userWebsitesVisited = new JRBeanCollectionDataSource(userWebsitesVisited);
        this.userWebsitesBlocked = new JRBeanCollectionDataSource(userWebsitesBlocked);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JRDataSource getUserWebsitesVisited() {
        return userWebsitesVisited;
    }

    public void setUserWebsitesVisited(JRDataSource userWebsitesVisited) {
        this.userWebsitesVisited = userWebsitesVisited;
    }

    public JRDataSource getUserWebsitesBlocked() {
        return userWebsitesBlocked;
    }

    public void setUserWebsitesBlocked(JRDataSource userWebsitesBlocked) {
        this.userWebsitesBlocked = userWebsitesBlocked;
    }
}

