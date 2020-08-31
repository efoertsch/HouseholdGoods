package org.householdgoods.data.product;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("slug", slug).toString();
    }

}