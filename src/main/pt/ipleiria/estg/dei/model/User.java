package main.pt.ipleiria.estg.dei.model;

import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.model.Word;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.util.List;

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
