package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class WcImage {


    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("date_created")
    @Expose
    public String date_created;
    @SerializedName("date_created_gmt")
    @Expose
    public String date_created_gmt;
    @SerializedName("date_modified")
    @Expose
    public String date_modified;
    @SerializedName("date_modified_gmt")
    @Expose
    public String date_modified_gmt;
    @SerializedName("src")
    @Expose
    public String src;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("alt")
    @Expose
    public String alt;

    //
    @SerializedName("title")
    @Expose
    public String title;

    // relative path folder (under wp-content/uploads) of the file to create. eg: 2018/05/department/brand
    @SerializedName("media_path")
    @Expose
    public String media_path;

    // WooCommerce specific Base64 string of media binary file
    @SerializedName("media_attachment")
    @Expose
    public String media_attachment;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("date_created", date_created)
                .append("date_created_gmt", date_created_gmt)
                .append("date_modified", date_modified)
                .append("date_modified_gmt", date_modified_gmt)
                .append("src", src)
                .append("name", name)
                .append("alt", alt)
                .append("title", title)
                .toString();
    }
}
