package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;


public class Dimensions {

    @SerializedName("length")
    @Expose
    public String length;
    @SerializedName("width")
    @Expose
    public String width;
    @SerializedName("height")
    @Expose
    public String height;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("length", length).append("width", width).append("height", height).toString();
    }

}