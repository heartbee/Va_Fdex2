package com.lody.virtual.hook;

import android.app.Application;
import android.os.Environment;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Fdex {
    Class Dex;
    Method Dex_getBytes;
    Method getDex = null;
    public Fdex(){
        try {
            Dex = Class.forName("com.android.dex.Dex");
            Dex_getBytes = Dex.getDeclaredMethod("getBytes", new Class[0]);
            getDex = Class.forName("java.lang.Class").getDeclaredMethod("getDex", new Class[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public  void writeByte(byte[] bArr, String str) {
        try {
            OutputStream outputStream = new FileOutputStream(str);
            outputStream.write(bArr);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  void fdex(Application application){
        if(application!=null){
            final String packagename=application.getPackageName();
            try {
                Class<?> clazz=application.getBaseContext().getClassLoader().loadClass("java.lang.ClassLoader");
                DexposedBridge.findAndHookMethod(clazz, "loadClass", String.class, Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Class cls = (Class) param.getResult();
                        if (cls == null) {
                            //XposedBridge.log("cls == null");
                            return;
                        }
                        String name = cls.getName();
                        DexposedBridge.log("当前类名：" + name);
                        byte[] bArr = (byte[]) Dex_getBytes.invoke(getDex.invoke(cls, new Object[0]), new Object[0]);
                        if (bArr == null) {
                            DexposedBridge.log("数据为空：返回");
                            return;
                        }
                        DexposedBridge.log("开始写数据");
                        //String dex_path = "/data/data/" + packagename + "/" + packagename + "_" + bArr.length + ".dex";
                        List<byte[]> list=new ArrayList<byte[]>();
                        list.add("wocao".getBytes());
                        if(bArr!=null && !Hook_Dex.dexinlist(list,bArr)){
                            String dex_path= Environment.getExternalStorageDirectory().getPath()+"/fdex/"+packagename+bArr.length+".dex";
                            DexposedBridge.log(dex_path);
                            File file = new File(dex_path);
                            //if (file.exists()) return;
                            writeByte(bArr, file.getAbsolutePath());
                            list.add(bArr);
                        }


                    }
                });

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }
}
