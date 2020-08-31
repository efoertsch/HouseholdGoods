package org.householdgoods.data.product;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("self", self).append("collection", collection).toString();
    }

}