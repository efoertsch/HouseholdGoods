package org.householdgoods.woocommerce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class _links {

    @SerializedName("self")
    @Expose
    public List<Self> self = null;
    @SerializedName("collection")
    @Expose
    public List<Collection> collection = null;
    @SerializedName("up")
    @Expose
    public List<Up> up = null;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("self", self).append("collection", collection).append("up", up).toString();
    }

}