/**
 * Copyright 2016 JustWayward Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.is.androidServer.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author yuyh.
 * @date 16/4/9.
 */
public class FileUtils {

    public static String getChapterPath(String bookId, int chapter) {
        return Constant.PATH_TXT + bookId + File.separator + chapter + ".txt";
    }

    public static File getChapterFile(String bookId, int chapter) {
        File file = new File(getChapterPath(bookId, chapter));
        if (!file.exists())
            createFile(file);
        return file;
    }

    public static File getBookDir(String bookId) {
        return new File(Constant.PATH_TXT + bookId);
    }

    public static String getMerginBook(String bookName) {
        return Constant.PATH_TXT + bookName;
    }

    public static File createWifiTempFile() {
        String src = Constant.PATH_DATA + "/" + System.currentTimeMillis();
        File file = new File(src);
        if (!file.exists())
            createFile(file);
        return file;
    }

    public static File[] getBookDirFiles(String bookId) {
        File dir = getBookDir(bookId);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            try {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        Integer lPage = Integer.parseInt(getFileNameNotType(lhs));
                        int rPage = Integer.parseInt(getFileNameNotType(rhs));
                        return Integer.compare(lPage, rPage);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return files;
        }
        return null;
    }

    public static String getFileNameNotType(File file) {
        String path = file.getPath();
        int separatorIndex = path.lastIndexOf(File.separator);
        int lastTypeIndex = path.lastIndexOf(".");
        return (separatorIndex < 0) ? path.substring(0,  lastTypeIndex) : path.substring(separatorIndex + 1, lastTypeIndex);
    }


    /**
     * ??????Wifi??????????????????
     *
     * @param fileName
     * @return
     */
    public static File createWifiTranfesFile(String fileName) {
        LogUtils.i("wifi trans save " + fileName);
        // ??????????????????????????????bookid???
        String absPath = Constant.PATH_PICTURES + "/" + fileName;

        File file = new File(absPath);
        if (!file.exists())
            createFile(file);
        return file;
    }

    public static String getEpubFolderPath(String epubFileName) {
        return Constant.PATH_EPUB + "/" + epubFileName;
    }

    public static String getPathOPF(String unzipDir) {
        String mPathOPF = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(unzipDir
                    + "/META-INF/container.xml"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("full-path")) {
                    int start = line.indexOf("full-path");
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        mPathOPF = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            if (!mPathOPF.contains("/")) {
                return null;
            }

            int last = mPathOPF.lastIndexOf('/');
            if (last > -1) {
                mPathOPF = mPathOPF.substring(0, last);
            }

            return mPathOPF;
        } catch (NullPointerException | IOException e) {
            LogUtils.e(e.toString());
        }
        return mPathOPF;
    }

    public static boolean checkOPFInRootDirectory(String unzipDir) {
        String mPathOPF = "";
        boolean status = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(unzipDir
                    + "/META-INF/container.xml"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("full-path")) {
                    int start = line.indexOf("full-path");
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        mPathOPF = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            if (!mPathOPF.contains("/")) {
                status = true;
            } else {
                status = false;
            }
        } catch (NullPointerException | IOException e) {
            LogUtils.e(e.toString());
        }
        return status;
    }

    public static void unzipFile(String inputZip, String destinationDirectory) throws IOException {

        int buffer = 2048;
        List<String> zipFiles = new ArrayList<>();
        File sourceZipFile = new File(inputZip);
        File unzipDirectory = new File(destinationDirectory);

        createDir(unzipDirectory.getAbsolutePath());

        ZipFile zipFile;
        zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
        Enumeration zipFileEntries = zipFile.entries();

        while (zipFileEntries.hasMoreElements()) {

            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(unzipDirectory, currentEntry);

            if (currentEntry.endsWith(Constant.SUFFIX_ZIP)) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            File destinationParent = destFile.getParentFile();
            createDir(destinationParent.getAbsolutePath());

            if (!entry.isDirectory()) {

                if (destFile != null && destFile.exists()) {
                    LogUtils.i(destFile + "?????????");
                    continue;
                }

                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;
                // buffer for writing file
                byte[] data = new byte[buffer];

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, buffer);

                while ((currentByte = is.read(data, 0, buffer)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
        zipFile.close();

        for (Iterator iter = zipFiles.iterator(); iter.hasNext(); ) {
            String zipName = (String) iter.next();
            unzipFile(zipName, destinationDirectory + File.separatorChar
                    + zipName.substring(0, zipName.lastIndexOf(Constant.SUFFIX_ZIP)));
        }
    }

    /**
     * ??????Assets??????
     *
     * @param fileName
     * @return
     */
    public static byte[] readAssets(String fileName) {
        if (fileName == null || fileName.length() <= 0) {
            return null;
        }
        byte[] buffer = null;
        try {
            InputStream fin = AppUtils.getAppContext().getAssets().open("uploader" + fileName);
            int length = fin.available();
            buffer = new byte[length];
            fin.read(buffer);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return buffer;
        }
    }

    public static String createPublicRootPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }
    /**
     * ?????????????????????
     *
     * @return
     */
    public static String createRootPath(Context context) {
        String cacheRootPath = "";
        if (isSdCardAvailable()) {
            // /sdcard/Android/data/<application package>/cache
            cacheRootPath = context.getExternalCacheDir().getPath();
        } else {
            // /data/data/<application package>/cache
            cacheRootPath = context.getCacheDir().getPath();
        }
        return cacheRootPath;
    }

    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * ?????????????????????
     *
     * @param dirPath
     * @return ??????????????????""
     */
    public static String createDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (file.getParentFile().exists()) {
                LogUtils.i("----- ???????????????" + file.getAbsolutePath());
                file.mkdir();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                LogUtils.i("----- ???????????????" + file.getAbsolutePath());
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirPath;
    }

    /**
     * ?????????????????????
     *
     * @param file
     * @return ??????????????????""
     */
    public static String createFile(File file) {
        try {
            if (file.getParentFile().exists()) {
                LogUtils.i("----- ????????????" + file.getAbsolutePath());
                file.createNewFile();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                file.createNewFile();
                LogUtils.i("----- ????????????" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * ?????????????????????
     *
     * @param filePath eg:/mnt/sdcard/demo.txt
     * @param content  ??????
     * @param isAppend ????????????
     */
    public static void writeFile(String filePath, String content, boolean isAppend) {
        LogUtils.i("save:" + filePath);
        try {
            FileOutputStream fout = new FileOutputStream(filePath, isAppend);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String filePathAndName, String fileContent) {
        try {
            OutputStream outstream = new FileOutputStream(filePathAndName);
            OutputStreamWriter out = new OutputStreamWriter(outstream);
            out.write(fileContent);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????Raw??????????????????
     *
     * @param context
     * @param resId
     * @return ????????????
     */
    public static String getFileFromRaw(Context context, int resId) {
        if (context == null) {
            return null;
        }

        StringBuilder s = new StringBuilder();
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(resId));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                s.append(line);
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            for (int n; (n = stream.read(b)) != -1; ) {
                out.write(b, 0, n);
            }
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * ????????????
     *
     * @param src  ?????????
     * @param desc ????????????
     */
    public static void fileChannelCopy(File src, File desc) {
        //createFile(src);
        createFile(desc);
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(src);
            fo = new FileOutputStream(desc);
            FileChannel in = fi.getChannel();//???????????????????????????
            FileChannel out = fo.getChannel();//???????????????????????????
            in.transferTo(0, in.size(), out);//??????????????????????????????in???????????????????????????out??????
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fo != null) fo.close();
                if (fi != null) fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param fileLen ??????B
     * @return
     */
    public static String formatFileSizeToString(long fileLen) {
        DecimalFormat df = new DecimalFormat("0.00");
        String fileSizeString = "";
        if (fileLen < 1024) {
            fileSizeString = df.format((double) fileLen) + "B";
        } else if (fileLen < 1048576) {
            fileSizeString = df.format((double) fileLen / 1024) + "K";
        } else if (fileLen < 1073741824) {
            fileSizeString = df.format((double) fileLen / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileLen / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * ??????????????????
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(File file) throws IOException {
        return deleteFileOrDirectory(file);
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean deleteFileOrDirectory(File file) throws IOException {
        try {
            if (file != null && file.isFile()) {
                return file.delete();
            }
            if (file != null && file.isDirectory()) {
                File[] childFiles = file.listFiles();
                // ??????????????????
                if (childFiles == null || childFiles.length == 0) {
                    return file.delete();
                }
                // ????????????????????????????????????
                for (int i = 0; i < childFiles.length; i++) {
                    deleteFileOrDirectory(childFiles[i]);
                }
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ?????????????????????
     *
     * @return
     * @throws Exception
     */
    public static long getFolderSize(String dir) throws Exception {
        File file = new File(dir);
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // ????????????????????????
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i].getAbsolutePath());
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /***
     * ?????????????????????
     *
     * @param filename ?????????
     * @return
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * ??????????????????
     *
     * @param path
     * @return
     */
    public static String getFileOutputString(String path, String charset) {
        try {
            File file = new File(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset), 8192);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append("\n").append(line);
            }
            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param root
     * @param ext  ???????????????
     */
    private synchronized void getAllFiles(File root, String ext) {
        List<File> list = new ArrayList<>();
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    getAllFiles(f, ext);
                } else {
                    if (f.getName().endsWith(ext) && f.length() > 50)
                        list.add(f);
                }
            }
        }
    }

    public static String getCharset(String fileName) {
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
                    if (0x80 <= read && read <= 0xBF) // ????????????BF?????????????????????GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // ????????? (0xC0 - 0xDF)
                            // (0x80 - 0xBF),????????????GB?????????
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// ???????????????????????????????????????
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

    public static String getCharset1(String fileName) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();

        String code;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }
        return code;
    }

    public static void saveWifiTxt(String src, String desc) {
        byte[] LINE_END = "\n".getBytes();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(src), getCharset(src));
            BufferedReader br = new BufferedReader(isr);

            FileOutputStream fout = new FileOutputStream(desc, true);
            String temp;
            while ((temp = br.readLine()) != null) {
                byte[] bytes = temp.getBytes();
                fout.write(bytes);
                fout.write(LINE_END);
            }
            br.close();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}