package com.inventaris.fams.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by mwildani on 19/09/2017.
 */

public interface APIService {
    @POST("api/create-asset")
    @FormUrlEncoded
    Call<ResponsePost> createAset(@Header("Authorization") String token,
                                  @Field("tag_rfid") String tag_rfid,
                                  @Field("barcode") String barcode,
                                  @Field("asset_origin") String asset_origin,
                                  @Field("id_location") String id_location,
                                  @Field("year") String year,
                                  @Field("id_asset_type_detail") String id_asset_type_detail,
                                  @Field("merk") String merk,
                                  @Field("image") String image);

}
