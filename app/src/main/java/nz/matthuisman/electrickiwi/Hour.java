package nz.matthuisman.electrickiwi;

/**
 * Created by Matt on 15/04/2017.
 */

public class Hour {
    private String id;
    private String name;
    private Boolean selected = false;

    public Hour(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String get_id() {
        return id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Boolean isSelected() {
        return selected;
    }

    @Override
    public String toString() {
        return name;
    }
}