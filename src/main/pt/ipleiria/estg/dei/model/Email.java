package main.pt.ipleiria.estg.dei.model;

public class Email {
    private String description;
    private String source;

    public Email(String word, String source) {
        this.description = word;
        this.source = source;
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
}