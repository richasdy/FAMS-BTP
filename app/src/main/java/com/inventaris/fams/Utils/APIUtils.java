package com.inventaris.fams.Utils;

import com.inventaris.fams.Config;

/**
 * Created by mwildani on 19/09/2017.
 */

public class APIUtils {
    private APIUtils() {
    }

    public static final String BASE_URL = Config.URL;

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
