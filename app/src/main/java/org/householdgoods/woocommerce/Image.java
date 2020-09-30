package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

//Slimmed down version of WC Image class. Contains only relevant fields
public class Image {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("src")
    @Expose
    public String src;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("title")
    @Expose
    public String title;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("src", src)
                .append("name", name)
                .append("title", title)
                .toString();
    }

}