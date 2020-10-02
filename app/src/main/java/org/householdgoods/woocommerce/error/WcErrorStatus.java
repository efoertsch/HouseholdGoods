package org.householdgoods.woocommerce.error;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class WcErrorStatus {

    @SerializedName("status")
    @Expose
    public Integer status;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status.toString())
                .toString();
    }

}
