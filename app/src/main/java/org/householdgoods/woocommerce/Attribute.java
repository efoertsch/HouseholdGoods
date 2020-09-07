package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Attribute {

    @SerializedName("id")
    @Expose
    public Integer id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("position")
    @Expose
    public Integer position;

    @SerializedName("visible")
    @Expose
    public Boolean visible;

    @SerializedName("variation")
    @Expose
    public Boolean variation;

    @SerializedName("options")
    @Expose
    public List<String> options;





}
