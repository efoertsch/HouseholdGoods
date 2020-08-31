package org.householdgoods.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HouseholdGoodsServerApi {
    @GET("/wp-json/wc/v3")
    Call<ResponseBody> getHouseGoodsApiList();
}
