package main.pt.ipleiria.estg.dei.model;

import net.sf.jasperreports.engine.JRDataSource;

public class User {
    private String name;
    private JRDataSource userWebsites;

    public User() {
    }

    public User(String name, JRDataSource userWebsites) {
        this.name = name;
        this.userWebsites = userWebsites;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JRDataSource getUserWebsites() {
        return userWebsites;
    }

    public void setUserWebsites(JRDataSource userWebsites) {
        this.userWebsites = userWebsites;
    }
}
