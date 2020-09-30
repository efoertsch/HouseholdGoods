package org.householdgoods.woocommerce.error;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WcErrorStatus {

    @SerializedName("status")
    @Expose
    public Integer status;

}
