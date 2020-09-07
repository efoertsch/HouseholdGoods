package org.householdgoods.woocommerce;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Category {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("slug")
    @Expose
    public String slug;
    @SerializedName("parent")
    @Expose
    public Integer parent;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("display")
    @Expose
    public String display;
    @SerializedName("image")
    @Expose
    public  Image image = null;
    @SerializedName("menu_order")
    @Expose
    public Integer menu_order;
    @SerializedName("count")
    @Expose
    public Integer count;
    @SerializedName("_links")
    @Expose
    public _links _links;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("slug", slug).append("parent", parent).append("description", description).append("display", display).append("image", image).append("menu_order", menu_order).append("count", count).append("_links", _links).toString();
    }

}