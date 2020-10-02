package org.householdgoods.woocommerce.error;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("code", code).append("message", message)
                .append("data", wcErrorStatus)
                .toString();
    }


}