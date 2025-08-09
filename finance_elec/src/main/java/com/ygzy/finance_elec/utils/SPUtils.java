package com.ygzy.finance_elec.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * 说明：SharedPreferences操作工具类
 */

public final class SPUtils {

    private static Map<String, SharedPreferences> spMap = new HashMap<>();
    private static Map<String, SPUtils> spUtilsMap = new HashMap<>();
    private String fileName;
    private static Context mContext;

    /**
     * 说明：禁止实例化
     */
    private SPUtils(String name) {
        fileName = name;
    }
    public static void  init(Context context){
        mContext = context;
    }

    public static SPUtils getInstance(String fileName) {
        SPUtils spUtils;
        if (TextUtils.isEmpty(fileName)){
            throw  new RuntimeException("SPUtils this fileName is null");
        }
        if (spMap.get(fileName) == null) {
            spUtils = new SPUtils(fileName);
            spMap.put(fileName, mContext.getSharedPreferences(fileName,
                    Context.MODE_PRIVATE));
            spUtilsMap.put(fileName,spUtils);
        }else {
            spUtils = spUtilsMap.get(fileName);
        }
        return spUtils;
    }

    /*********************写方法*********************************/

    /**
     * 说明：long
     * @param key
     * @param value
     */
    public void write(String key, long value){
        write(key, String.valueOf(value));
    }

    /**
     * 说明：int
     * @param key
     * @param value
     */
    public void write(String key, int value) {
        write(key, String.valueOf(value));
    }

    /**
     * 说明：boolean
     * @param key
     * @param value
     */
    public void write(String key, boolean value) {
        write(key, String.valueOf(value));
    }

    /**
     * 说明：String
     * @param key
     * @param value
     */
    public void write(String key, String value) {
        spMap.get(fileName).edit().putString(key, value).commit();
    }

    /*********************读方法*********************************/

    /**
     * 说明：读int
     * @param key
     * @return
     */
    public int readInt(String key) {
        return readInt(key, 0);
    }

    /**
     * 说明：读int
     * @param key
     * @return
     */
    public int readInt(String key, int defaultValue) {
        return Integer.parseInt(readString(key, String.valueOf(defaultValue)));
    }

    /**
     * 说明：读boolean
     * @param key
     * @return
     */
    public boolean readBoolean(String key) {
        return readBoolean(key, false);
    }

    /**
     * 说明：读boolean
     */
    public boolean readBoolean(String key,
                               boolean defaultBoolean) {
        return Boolean.parseBoolean(readString(key, String.valueOf(defaultBoolean)));
    }

    /**
     * 说明：读long
     * @param key
     * @return
     */
    public long readLong(String key){
        return readLong(key, 0);
    }

    /**
     * 说明：读long
     * @param key
     * @param defalut
     * @return
     */
    public long readLong(String key, long defalut){
        return Long.parseLong(readString(key, String.valueOf(defalut)));
    }

    /**
     * 说明：读String
     * @param key
     * @return
     */
    public String readString(String key) {
        return readString(key, "");
    }

    /**
     * 说明：读String
     * @param key
     * @param defaultValue
     * @return
     */
    public String readString(String key, String defaultValue) {
        String text = spMap.get(fileName).getString(key, null);
        if (!TextUtils.isEmpty(text)){
            return text;
        }else {
            return defaultValue;
        }
    }

    /**
     * 说明：删除
     * @param key
     */
    public void remove(String key) {
        spMap.get(fileName).edit().remove(key).remove(key).apply();
    }

    /**
     * 说明：清空
     */
    public void clear() {
        spMap.get(fileName).edit().clear().apply();
    }
}