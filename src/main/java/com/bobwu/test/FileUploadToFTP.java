package com.bobwu.test;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.invoke.SwitchPoint;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUploadToFTP {
    private final static String SERVER = "10.25.12.171";
    private final static int PORT = 21; // FTP端口
    private final static String USER = "user1";
    private final static String PASS = "qwe123";
    private final static String REMOTEPATH = "/user1/certs/"; // 上传到FTP服务器上的文件路径和文件名

    // 取得指定档案上传ftp
    public static void main(String[] args) throws IOException {
        String content = "aaa";
        String daifuId = "daifu002"; // daifu
        String merchantNo = "70012"; // 商户号

        String currentDir = System.getProperty("user.dir");
        System.out.println("当前工作目录: " + currentDir);
        String directoryPath = "Temp/" + daifuId + "/" + merchantNo;
        String filePath = directoryPath + "/checkOrderKey.txt"; // 预设档名
        String zipFilePath = currentDir + "/Temp/" + daifuId;

        filePath = writeToFile(content ,filePath ,directoryPath); // 写入档案
        if(filePath != null){
            System.out.println("文件路径 = " + filePath);
            Path sourceDir = Paths.get(zipFilePath);
            zipFilePath = zipFilePath + ".zip";
            Path zipFile  = Paths.get(zipFilePath);
            zipDirectory(sourceDir ,zipFile); // zip
            String method = "localFilePath";
            uploadContentToFTP(zipFilePath ,method); // 上传 ftp
        }
    }

    public static String writeToFile(String content ,String filePath ,String directoryPath){
        try {
            // 创建目录
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            // 写入文件
            File file = new File(filePath);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                System.out.println("成功写入文件");
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            System.out.println("写入文件时出现错误：" + e.getMessage());
            return null;
        }
    }

    public static void zipDirectory(Path sourceDirPath, Path zipFilePath) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFilePath));
             Stream<Path> paths = Files.walk(sourceDirPath)) {

            paths.filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(path, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Failed to add file to zip: " + path);
                            e.printStackTrace();
                        }
                    });
        }
    }

    public static void uploadContentToFTP(String localFilePath ,String method) {
        String content = "";
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(SERVER, PORT);
            ftpClient.login(USER, PASS);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            FileInputStream fileInputStream = null;
            ByteArrayInputStream byteArrayInputStream = null;

            String remoteFilePath = null;
            boolean done = false;
            switch (method){
                case "localFilePath":
                    // 获取文件输入流
                    File localFile = new File(localFilePath);
                    fileInputStream = new FileInputStream(localFile);
                    // 指定远程文件路径
                    remoteFilePath = REMOTEPATH + localFile.getName();
                    System.out.println("开始上传文件...");
                    done = ftpClient.storeFile(remoteFilePath, fileInputStream);
                    fileInputStream.close();
                    break;

                case "fileContent":
                    // 将内容转换为输入流
                    byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
                    System.out.println("开始上传文件...");
                    done = ftpClient.storeFile(remoteFilePath, byteArrayInputStream);
                    byteArrayInputStream.close();
                    break;
            }
            if (done) {
                System.out.println("文件成功上传到 FTP 服务器");
            } else {
                System.out.println("文件上传失败");
            }

        } catch (IOException ex) {
            System.out.println("上传到 FTP 时出现错误：" + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                System.out.println("关闭 FTP 连接时出现错误：" + ex.getMessage());
            }
        }
    }
}