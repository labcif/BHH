package main.pt.ipleiria.estg.dei.model;

public class Login {
    private String description;
    private String source;
    private int total;

    public Login(String word, String source, int total) {
        this.description = word;
        this.source = source;
        this.total = total;
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

}