package org.householdgoods.data.product;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;


public class Collection {

    @SerializedName("href")
    @Expose
    public String href;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("href", href).toString();
    }

}