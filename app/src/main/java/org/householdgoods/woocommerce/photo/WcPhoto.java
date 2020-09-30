package org.householdgoods.woocommerce.photo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

// see https://wordpress.org/plugins/woo-media-api/#description
// and https://developer.wordpress.org/rest-api/reference/media/#arguments-2
// This class starts with Wordpress media fields and adds in Wc media_path  and media_attachment
// Take the guid.rendered url and then update the product with the associated rendered urls
public class WcPhoto {

    @SerializedName("id")
    @Expose
    public Integer id;

    @SerializedName("date")
    @Expose
    public String date;

    @SerializedName("date_gmt")
    @Expose
    public String date_gmt;

    @SerializedName("guid")
    @Expose
    public Guid guid;

    // Read only
    @SerializedName("link")
    @Expose
    public String link;

    @SerializedName("modified")
    @Expose
    public String modified;

    @SerializedName("modified_gmt")
    @Expose
    public String modified_gmt;

    @SerializedName("slug")
    @Expose
    public String slug;

    @SerializedName("status")
    @Expose
    public String status = "publish";

    @SerializedName("type")
    @Expose
    public String type = "attachment";

    @SerializedName("permalink_template")
    @Expose
    public String permalink_template;

    @SerializedName("generated_slug")
    @Expose
    public String generated_slug;

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("author")
    @Expose
    public String author;

    @SerializedName("comment_status")
    @Expose
    public String comment_status;

    @SerializedName("ping_status")
    @Expose
    public String ping_status;

    @SerializedName("meta")
    @Expose
    public String meta;

    @SerializedName("template")
    @Expose
    public String template;

    @SerializedName("alt_text")
    @Expose
    public String alt_text;

    @SerializedName("caption")
    @Expose
    public String caption;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("media_type")
    @Expose
    public String media_type = "image";

    @SerializedName("mime_type")
    @Expose
    public String mime_type = "image/jpeg";

    @SerializedName("media_details")
    @Expose
    public String media_details;

    @SerializedName("post")
    @Expose
    public String post;

    @SerializedName("source_url")
    @Expose
    public String source_url;

    @SerializedName("missing_image_sizes")
    @Expose
    public ArrayList<String> missing_image_sizes;

    // relative path folder (under wp-content/uploads) of the file to create. eg: 2018/05/department/brand
    @SerializedName("media_path")
    @Expose
    public String media_path;

    // WooCommerce specific Base64 string of media binary file
    @SerializedName("media_attachment")
    @Expose
    public String media_attachment;


    // Show only some fields, some only have when read.
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("media_path", media_path)
                .append("date ", date )
                .append("linkg", link)
                .append("status", status)
                .append("media_type", media_type)
                .append("mime_type", mime_type)
                .append("source_url", source_url)
                .toString();
    }
}
