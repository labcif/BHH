package main.pt.ipleiria.estg.dei.labcif.bhh.models;

public class Word {
    private String description;
    private int useCount;
    private String username;
    private String source;

    public Word(String word, int useCount, String username, String source) {
        this.description = word;
        this.useCount = useCount;
        this.username = username;
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
