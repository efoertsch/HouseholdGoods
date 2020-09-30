package org.householdgoods.woocommerce.photo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Guid {

    @SerializedName("rendered")
    @Expose
    public String rendered;
    @SerializedName("raw")
    @Expose
    public String raw;

}