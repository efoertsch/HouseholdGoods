package org.householdgoods.data;

public class HHGCategory {

    private String key;

    private String category;

    private String item;

    public HHGCategory(String key, String category, String item) {
        this.key = key;
        this.category = category;
        this.item = item;
    }

    public String getKey() {
        return key;
    }

    public String getCategory() {
        return category;
    }

    public String getItem() {
        return item;
    }
}
