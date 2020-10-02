package org.householdgoods.woocommerce.photo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WcRawRendered {
    @SerializedName("raw")
    @Expose
    public String raw;
    @SerializedName("rendered")
    @Expose
    public String rendered;
}
