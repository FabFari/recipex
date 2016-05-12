package com.recipex.utilities;

public class ContactItem {
    private int icon_id;
    private String label;

    public ContactItem(int icon_id, String label) {
        this.icon_id = icon_id;
        this.label = label;
    }

    public int getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(int icon_id) {
        this.icon_id = icon_id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
