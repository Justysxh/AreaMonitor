package com.sxhsoft.leave.tool;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sxh on 2016/9/6.
 */
public class MobileTool
{
    private static final String DEFAULT_SHARE_FILE_NAME = "Monitor.Config";
    private static Context sContext;
    public static void setContext(Context context)
    {
        sContext = context;
    }


    public static boolean setShareValue(String name, String key, Object obj)
    {
        SharedPreferences sp = sContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (obj instanceof Boolean)
        {
            editor.putBoolean(key, (Boolean) obj);
        } else if (obj instanceof String)
        {
            editor.putString(key, (String) obj);
        } else if (obj instanceof Integer)
        {
            editor.putInt(key, (Integer) obj);
        } else if (obj instanceof Float)
        {
            editor.putFloat(key, (Float) obj);
        } else if (obj instanceof Long)
        {
            editor.putLong(key, (Long) obj);
        }
        return editor.commit();
    }

    public static boolean setShareValue(String key, Object obj)
    {
        return setShareValue(DEFAULT_SHARE_FILE_NAME, key, obj);
    }


    /**
     * 获取SharedPreferences数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public static Object getShareValue(String spName, String key, Object defValue)
    {
        SharedPreferences sp = sContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        if (defValue instanceof Boolean)
        {
            return sp.getBoolean(key, (Boolean) defValue);
        }

        if (defValue instanceof Integer)
        {
            return sp.getInt(key, (Integer) defValue);
        }

        if (defValue instanceof Float)
        {
            return sp.getFloat(key, (Float) defValue);
        }

        if (defValue instanceof Long)
        {
            return sp.getLong(key, (Long) defValue);
        }
        if (defValue instanceof String)
        {
            return sp.getString(key, (String) defValue);
        }
        return defValue;
    }

    /**
     * 使用默认配置文件保存共享数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public static Object getShareValue(String key, Object defValue)
    {
        return getShareValue(DEFAULT_SHARE_FILE_NAME, key, defValue);
    }
}
