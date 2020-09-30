package org.householdgoods.woocommerce.error;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WcError {

    @SerializedName("code")
    @Expose
    public String code;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("data")
    @Expose
    public WcErrorStatus wcErrorStatus;

}