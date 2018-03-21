package com.inventaris.fams;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by mwildani on 23/08/2017.
 */

public class Config {
//    public static final String URL = "http://128.199.115.183:8002/";
    public static final String URL = "http://api.asset.technopark.or.id/";
    public final static String CLIENT_SECRET = "krrUUmbDtyxGVew2d1qaoAVaBPEZH2tguhRIN06o";
    public final static String TOKEN_SHARED_PREF = "token";
    public final static String URL_GENERATE_TOKEN = URL + "oauth/token";
    public final static String GRANT_TYPE = "password";
    public final static String CLIENT_ID = "4";
    public static final UUID RFIDUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String URL_GET_LOCATION = URL + "api/index-location";
    public static final String URL_GET_GEDUNG = URL + "api/index-gedung";
    public static final String URL_GET_TIPEASSET = URL + "api/index-type-detail";
    public static final String URL_GET_ALL_ASSET = URL + "api/asset";
    public static final String URL_SEARCH_ASSET = URL + "api/search?q=";
    public static final String URL_CREATE_ASSET = URL + "api/create-asset";
    public static final String URL_DELETE_ASSET = URL + "api/delete-asset/";
    public static final Locale locale = new Locale( "id" , "ID" );
}
