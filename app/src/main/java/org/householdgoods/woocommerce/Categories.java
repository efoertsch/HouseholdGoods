package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

public class Categories {

    @SerializedName("categories")
    @Expose
    public ArrayList<Category> categories = null;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("categories", categories).toString();
    }

}