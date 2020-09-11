package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;


// Used http://www.jsonschema2pojo.org/ to create java class from Json for this and all related classes
// Note that data types may not be correct if example value not  included in JSON used to generate this POJO
// !!! Some fields assigned default values !!!!
public class Product {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("slug")
    @Expose
    public String slug;
    @SerializedName("permalink")
    @Expose
    public String permalink;
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
    @SerializedName("type")
    @Expose
    public String type = "simple";
    @SerializedName("status")
    @Expose
    public String status = "publish";
    @SerializedName("featured")
    @Expose
    public Boolean featured;
    @SerializedName("catalog_visibility")
    @Expose
    public String catalog_visibility;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("short_description")
    @Expose
    public String short_description;
    @SerializedName("sku")
    @Expose
    public String sku;
    @SerializedName("price")
    @Expose
    public String price = "0.00";
    @SerializedName("regular_price")
    @Expose
    public String regular_price = "0.00";
    @SerializedName("sale_price")
    @Expose
    public String sale_price;
    @SerializedName("date_on_sale_from")
    @Expose
    public String date_on_sale_from;
    @SerializedName("date_on_sale_from_gmt")
    @Expose
    public String date_on_sale_from_gmt;
    @SerializedName("date_on_sale_to")
    @Expose
    public String date_on_sale_to;
    @SerializedName("date_on_sale_to_gmt")
    @Expose
    public String date_on_sale_to_gmt;
    @SerializedName("price_html")
    @Expose
    public String price_html;
    @SerializedName("on_sale")
    @Expose
    public Boolean on_sale;
    @SerializedName("purchasable")
    @Expose
    public Boolean purchasable;
    @SerializedName("total_sales")
    @Expose
    public Integer total_sales;
    @SerializedName("virtual")
    @Expose
    public Boolean virtual;
    @SerializedName("downloadable")
    @Expose
    public Boolean downloadable;
    @SerializedName("downloads")
    @Expose
    public List<Downloads> downloads;
    @SerializedName("download_limit")
    @Expose
    public Integer download_limit;
    @SerializedName("download_expiry")
    @Expose
    public Integer download_expiry;
    @SerializedName("external_url")
    @Expose
    public String external_url;
    @SerializedName("button_text")
    @Expose
    public String button_text;
    @SerializedName("tax_status")
    @Expose
    public String tax_status;
    @SerializedName("tax_class")
    @Expose
    public String tax_class;
    @SerializedName("manage_stock")
    @Expose
    public Boolean manage_stock = true;
    @SerializedName("stock_quantity")
    @Expose
    public int stock_quantity = 0;
    @SerializedName("stock_status")
    @Expose
    public String stock_status;
    @SerializedName("backorders")
    @Expose
    public String backorders;
    @SerializedName("backorders_allowed")
    @Expose
    public Boolean backorders_allowed;
    @SerializedName("backordered")
    @Expose
    public Boolean backordered;
    @SerializedName("sold_individually")
    @Expose
    public Boolean sold_individually;
    @SerializedName("weight")
    @Expose
    public String weight;
    @SerializedName("dimensions")
    @Expose
    public Dimensions dimensions = new Dimensions() ;
    @SerializedName("shipping_required")
    @Expose
    public Boolean shipping_required;
    @SerializedName("shipping_taxable")
    @Expose
    public Boolean shipping_taxable;
    @SerializedName("shipping_class")
    @Expose
    public String shipping_class;
    @SerializedName("shipping_class_id")
    @Expose
    public Integer shipping_class_id;
    @SerializedName("reviews_allowed")
    @Expose
    public Boolean reviews_allowed;
    @SerializedName("average_rating")
    @Expose
    public String average_rating;
    @SerializedName("rating_count")
    @Expose
    public Integer rating_count;
    @SerializedName("related_ids")
    @Expose
    public List<Integer> related_ids;
    @SerializedName("upsell_ids")
    @Expose
    public List<Integer> upsell_ids;
    @SerializedName("cross_sell_ids")
    @Expose
    public List<Integer> cross_sell_ids;
    @SerializedName("parent_id")
    @Expose
    public Integer parent_id ;
    @SerializedName("purchase_note")
    @Expose
    public String purchase_note;
    @SerializedName("categories_json")
    @Expose
    public List<Category> categories;
    @SerializedName("tags")
    @Expose
    public List<Tags> tags = null;
    @SerializedName("images")
    @Expose
    public List<Image> images = null;
    @SerializedName("attributes")
    @Expose
    public List<Attribute> attributes = null;
    //    @SerializedName("default_attributes")
//    @Expose
//    public List<Object> default_attributes = null;
//    @SerializedName("variations")
//    @Expose
//    public List<Object> variations = null;
//    @SerializedName("grouped_products")
//    @Expose
//    public List<Object> grouped_products = null;
    @SerializedName("menu_order")
    @Expose
    public Integer menu_order;

    //    @SerializedName("meta_data")
//    @Expose
//    public List<Object> meta_data = null;
    @SerializedName("_links")
    @Expose
    public org.householdgoods.woocommerce._links _links;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id)
                .append("name", name).append("slug", slug).append("permalink", permalink)
                .append("date_created", date_created).append("date_created_gmt", date_created_gmt)
                .append("date_modified", date_modified).append("date_modified_gmt", date_modified_gmt)
                .append("type", type).append("status", status).append("featured", featured)
                .append("catalog_visibility", catalog_visibility).append("description", description)
                .append("short_description", short_description).append("sku", sku)
                .append("price", price).append("regular_price", regular_price).append("sale_price", sale_price)
                .append("date_on_sale_from", date_on_sale_from).append("date_on_sale_from_gmt", date_on_sale_from_gmt)
                .append("date_on_sale_to", date_on_sale_to).append("date_on_sale_to_gmt", date_on_sale_to_gmt)
                .append("price_html", price_html).append("on_sale", on_sale).append("purchasable", purchasable)
                .append("total_sales", total_sales).append("virtual", virtual).append("downloadable", downloadable)
                .append("downloads", downloads).append("download_limit", download_limit)
                .append("download_expiry", download_expiry).append("external_url", external_url)
                .append("button_text", button_text).append("tax_status", tax_status)
                .append("tax_class", tax_class).append("manage_stock", manage_stock)
                .append("stock_quantity", stock_quantity).append("stock_status", stock_status)
                .append("backorders", backorders).append("backorders_allowed", backorders_allowed)
                .append("backordered", backordered).append("sold_individually", sold_individually)
                .append("weight", weight).append("dimensions", dimensions).append("shipping_required", shipping_required)
                .append("shipping_taxable", shipping_taxable).append("shipping_class", shipping_class)
                .append("shipping_class_id", shipping_class_id).append("reviews_allowed", reviews_allowed)
                .append("average_rating", average_rating).append("rating_count", rating_count)
                .append("related_ids", related_ids).append("upsell_ids", upsell_ids).append("cross_sell_ids", cross_sell_ids).append("parent_id", parent_id).append("purchase_note", purchase_note).append("categories_json", categories).append("tags", tags)
                .append("images", images).append("attributes", attributes)
                //.append("default_attributes", default_attributes)
                // .append("variations", variations)
                //.append("grouped_products", grouped_products)
                .append("menu_order", menu_order)
                //.append("meta_data", meta_data)
                 .append("_links", _links).toString();
    }

}