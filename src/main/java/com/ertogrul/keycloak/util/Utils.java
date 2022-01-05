package com.ertogrul.keycloak.util;

public class Utils {

    public static boolean isNullOrEmpty(final String value){
        if(value==null || value.isEmpty()){
            return true;
        }else{
            return false;
        }
    }
}
