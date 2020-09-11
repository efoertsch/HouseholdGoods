package org.householdgoods.data;

public class HHGCategory {

    private String key;

    private String category;

    private String subCategory;

    private String item;

    public HHGCategory(String key, String category, String subCategory, String item) {
        this.key = key;
        this.category = category;
        this.subCategory = subCategory;
        this.item = item;
    }

    public String getKey() {
        return key;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getItem() {
        return item;
    }
}
