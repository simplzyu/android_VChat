package com.vchat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by simplzy on 2017/5/21.
 */

public class Common {

    public static String getIdCode(){
        String id_code = (int) (10000*Math.random()) + "";
        int len = id_code.length();
        for(int i = 0; i < 4-len; i++){
            id_code = "0" + id_code;
        }
        return id_code;
    }

    public static boolean checkEmail(String email){
        boolean flag = false;
        try{
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

    public static boolean getNetworkState(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null)
            return false;
        return true;

    }
}
