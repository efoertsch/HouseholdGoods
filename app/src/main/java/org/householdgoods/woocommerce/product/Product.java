package org.householdgoods.woocommerce.product;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.householdgoods.woocommerce.category.Category;
import org.householdgoods.woocommerce.Image;
import org.householdgoods.woocommerce.Tags;

import java.util.List;


// Used http://www.jsonschema2pojo.org/ to create java class from Json for this and all related classes
// Then removed numerous unneeded fields. Only kept those needed for creating new product record
// !!! Some fields assigned default values !!!!
public class Product {

    @SerializedName("id")
    @Expose
    public Integer id = 0;
    @SerializedName("name")
    @Expose
    public String name = "";

    @SerializedName("type")
    @Expose
    public String type = "simple";

    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("short_description")
    @Expose
    public String short_description;
    @SerializedName("sku")
    @Expose
    public String sku;

    @SerializedName("regular_price")
    @Expose
    public String regular_price = "0.00`";

    @SerializedName("manage_stock")
    @Expose
    public Boolean manage_stock = true;
    @SerializedName("stock_quantity")
    @Expose
    public int stock_quantity = 0;
    @SerializedName("stock_status")
    @Expose
    public String stock_status = "instock";

    @SerializedName("sold_individually")
    @Expose
    public Boolean sold_individually = true;

    @SerializedName("dimensions")
    @Expose
    public Dimensions dimensions = new Dimensions();

    @SerializedName("parent_id")
    @Expose
    public Integer parent_id = 0;

    @SerializedName("categories")
    @Expose
    public List<Category> categories;
    @SerializedName("tags")
    @Expose
    public List<Tags> tags = null;
    @SerializedName("images")
    @Expose
    public List<Image> images = null;


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id)
                .append("name", name)
                .append("type", type)
                .append("description", description)
                .append("short_description", short_description)
                .append("sku", sku)
                .append("regular_price", regular_price)
                .append("manage_stock", manage_stock)
                .append("stock_quantity", stock_quantity)
                .append("stock_status", stock_status)
                .append("sold_individually", sold_individually)
                .append("dimensions", dimensions)
                .append("parent_id", parent_id)
                .append("categories", categories)
                .append("tags", tags)
                .toString();
    }

}