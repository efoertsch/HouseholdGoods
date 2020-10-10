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

    private Product(){}

    @SerializedName("id")
    @Expose
    public Integer id;

    @SerializedName("name")
    @Expose
    public String name;

    // read only value
    @SerializedName("permalink")
    @Expose
    public String permalink;

    @SerializedName("type")
    @Expose
    public String type ;

    @SerializedName("status")
    @Expose
    public String status ;

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
    public String regular_price;

    @SerializedName("manage_stock")
    @Expose
    public Boolean manage_stock ;

    @SerializedName("stock_quantity")
    @Expose
    public Integer stock_quantity;

    @SerializedName("stock_status")
    @Expose
    public String stock_status;

    @SerializedName("sold_individually")
    @Expose
    public Boolean sold_individually;

    @SerializedName("dimensions")
    @Expose
    public Dimensions dimensions;

    @SerializedName("parent_id")
    @Expose
    public Integer parent_id ;

    @SerializedName("categories")
    @Expose
    public List<Category> categories;

    @SerializedName("tags")
    @Expose
    public List<Tags> tags;

    @SerializedName("images")
    @Expose
    public List<Image> images;

    public static Product getProductForAdd(){
        Product product = new Product();
        // assign default values
        product.id = 0;
        product.type =  "simple";
        product.regular_price =  "0.00";
        product.manage_stock =  true;
        product.stock_status = "instock";
        product.sold_individually = true;
        product.dimensions = new Dimensions();
        product.parent_id = 0;
        return product;
    }

    // Requires id of product you want to update
    public static Product getProductForUpdate(int id){
        Product product = new Product();
        product.id = id;
        return product;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id)
                .append("name", name)
                .append("type", type)
                .append("status", status)
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