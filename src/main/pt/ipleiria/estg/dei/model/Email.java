package main.pt.ipleiria.estg.dei.model;

public class Email {
    private String description;
    private String source;
    private int total;
    private String password;

    public Email(String word, String source, int total, String password) {
        this.description = word;
        this.source = source;
        this.total = total;
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}