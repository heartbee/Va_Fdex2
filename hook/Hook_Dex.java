package com.lody.virtual.hook;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import me.weishu.epic.art.Epic;


public class Hook_Dex {
    public static void hook(final Application application){
        Log.e("wocao","the package is:"+application.getPackageName());
        final String packagename=application.getPackageName();
        ClassLoader classLoader=application.getClassLoader();
        try {
            Class<?> dex=classLoader.loadClass("com.android.dex.Dex");
            if(dex!=null){
                Log.e("wocao","find the class");
                DexposedBridge.findAndHookMethod(dex,"getBytes", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        byte[] dex_bytes=(byte[])param.getResult();
                        List<byte[]> list=new ArrayList<byte[]>();
                        list.add("wocao".getBytes());
                        if(dex_bytes!=null && !dexinlist(list,dex_bytes)){
                            String dex_file= Environment.getExternalStorageDirectory().getPath()+"/"+packagename+dex_bytes.length+".dex";
                            writeByte(dex_bytes,dex_file);
                            list.add(dex_bytes);
                        }
                    }
                });
            }else {
                Log.e("wocao","not find the class");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static boolean dexinlist(List<byte[]> list,byte[] dexs){
        for(byte[] dex:list){
            if(dex.equals(dexs)){
                return true;
            }
        }
        return false;

    }



    public static String bytesToHexString(byte[] bArray, int length)
    {
        StringBuffer sb = new StringBuffer(length);
        String sTemp;
        for (int i = 0; i < length; i++)
        {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static void writeByte(byte[] bArr, String str) {
        try {
            FileOutputStream fileOutputStream=new FileOutputStream(str);
            fileOutputStream.write(bArr);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Object getObjectField(Object object, String fieldName) {
        Class clazz = object.getClass();
        while (!clazz.getName().equals(Object.class.getName())) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static ArrayList<String> getClassNameList(ClassLoader classLoader) {
        ArrayList<String> classNameList = new ArrayList<>();
        try {
            Object pathList = getObjectField(classLoader, "pathList");
            Object dexElements = getObjectField(pathList, "dexElements");
            int dexElementsLength = Array.getLength(dexElements);
            for (int i = 0; i < dexElementsLength; i++) {
                Object dexElement = Array.get(dexElements, i);
                DexFile dexFile = (DexFile) getObjectField(dexElement, "dexFile");
                Enumeration<String> enumerations = dexFile.entries();
                while (enumerations.hasMoreElements()) {
                    String classname = enumerations.nextElement();
                    classNameList.add(classname);
                }
            }
        } catch (Exception e) {
            Log.e("wocao", e.getMessage());
        }
        Collections.sort(classNameList);
        return classNameList;
    }

    public static void loadClass(Application mInitialApplication){
        List<String> classNameList = getClassNameList(mInitialApplication.getBaseContext().getClassLoader());
        for(String name:classNameList){
            ClassLoader classLoader=mInitialApplication.getBaseContext().getClassLoader();
            try {
                if(name.contains("android"))
                    continue;
                classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }



}
