package com.ssi.musicplayer2.utils;


import android.content.Context;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();


    public static int getIndexInList(List<MediaBrowser.MediaItem> list, String mediaId) {
        if(list == null || mediaId == null) {
            Logger.e(TAG, "getIndexInList return -1 for null!!!");
            return -1;
        }
        final int N = list.size();
        String itemId = null;
        for(int i=0;i<N;i++) {
            itemId = list.get(i).getMediaId();
            if(mediaId.equals(itemId)) {
                return i;
            }
        }
        return -1;
    }


    private static final int ONE_MILLISECOND = 1000;


    public static String formatTime(long time) {
        long second = time / ONE_MILLISECOND;
        long min = time / (ONE_MILLISECOND * 60);

        long hour = time / (ONE_MILLISECOND * 60 * 60);
        String strTime = "";

        if(hour >= 1) {
            min = (time - hour * (ONE_MILLISECOND * 60 * 60)) / (ONE_MILLISECOND * 60);
            min = min >= 1 ? min : 0;
            second = (time - hour * (ONE_MILLISECOND * 60 * 60) - (ONE_MILLISECOND * 60) * min) / ONE_MILLISECOND;
            second = second >= 1 ? second : 0;
            strTime = (hour < 10 ? ("0" + hour) : hour) + ":" + (min < 10 ? ("0" + min) : min) + ":" + (second < 10 ? ("0" + second) : second);
        }else if(min >= 1) {
            second = (time - min * ONE_MILLISECOND * 60) / ONE_MILLISECOND;
            second = second >= 1 ? second : 0;
            strTime = (min < 10 ? ("0" + min) : min) + ":" + (second < 10 ? ("0" + second) : second);
        }else if(second >= 1){
            strTime = "00" + ":" + (second < 10 ? ("0" + second) : second);
        }else {
            strTime = "00:00";
        }
        return strTime;


    }


    public static String byteArrayToString(byte[] bytes) {
        if(bytes == null) {
            return "NULL";
        }
        StringBuilder ss = new StringBuilder(String.valueOf("bytes size -" + bytes.length + ":"));
        for(int i=0;i<bytes.length;i++) {
            ss.append("bytes[").append(i).append("]").append(bytes[i]);
        }
        return ss.toString();
    }


    public static String listToString(List<Float> floats) {
        String s = "listToString:";
        for(int i = 0;i<floats.size();i++) {
            s += "[" + i+"]:" + floats.get(i) + ";";
        }
        return s;
    }

    public static String getCharset(String fileName){
        BufferedInputStream bis = null;
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(fileName));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.mark(0);
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return charset;
    }

    public static List getUSBPaths(Context con) {//获取usb路径

        String[] paths = null;

        List data = new ArrayList(); // include sd and usb devices

        StorageManager storageManager = (StorageManager) con.getSystemService(Context.STORAGE_SERVICE);

        try {

            paths = (String[]) StorageManager.class.getMethod("getVolumePaths", null).invoke(storageManager, null);

            for (String path : paths) {

                String state = (String) StorageManager.class.getMethod("getVolumeState", String.class).invoke(storageManager, path);

                if (state.equals(Environment.MEDIA_MOUNTED) && !path.contains("emulated")) {
                    data.add(path);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
    public static long getFileDuration(String path){
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);  //recordingFilePath（）为音频文件的路径
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long duration= player.getDuration();//获取音频的时间
        Log.d("ACETEST", "### duration: " + duration);
        player.release();//记得释放资源
        return duration;
    }

}

