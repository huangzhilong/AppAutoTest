package com.hago.startup;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class FileUtil {


    private static final String PACKAGE_NAME = "com.hago.startup";

    public static String getExternalDir() throws IOException{
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + PACKAGE_NAME);
        if (!file.mkdirs()) {
            file.createNewFile();
        }
        String savePath = file.getAbsolutePath();
        return savePath;
    }
}
