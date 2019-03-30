package main.pt.ipleiria.estg.dei.model;

public class Word {
    private String description;
    private int useCount;

    public Word(String word, int useCount) {
        this.description = word;
        this.useCount = useCount;
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
}
