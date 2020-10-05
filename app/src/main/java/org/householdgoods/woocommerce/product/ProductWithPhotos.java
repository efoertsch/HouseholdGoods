package org.householdgoods.woocommerce.product;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.householdgoods.woocommerce.Image;

import java.util.List;

public class ProductWithPhotos {

    @SerializedName("id")
    @Expose
    public Integer id = 0;

    @SerializedName("images")
    @Expose
    public List<Image> images = null;


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id)
                .append("images", images)
                .toString();
    }

}
