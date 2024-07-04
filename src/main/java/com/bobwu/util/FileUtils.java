package com.bobwu.util;

import com.bobwu.bean.Ftp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
@Slf4j
@Component
public class FileUtils {

    @Autowired
    Ftp ftp;

    public String writeToFile(String content ,String filePath ,String directoryPath){
        File file = new File(filePath);
        try {
            // 创建目录
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            // 写入文件
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                log.info("成功写入文件");
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            log.error("写入文件时出现错误 = {}", e.getMessage());
            return null;
        }
    }

    public void zipDirectory(Path sourceDirPath, Path zipFilePath) throws IOException {
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

    public void uploadContentToFTP(String localFilePath ,String method) {
        String content = "";
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftp.getServer(), ftp.getPort());
            ftpClient.login(ftp.getUser(), ftp.getPass());
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
                    remoteFilePath = ftp.getRemotePath() + localFile.getName();
                    log.info("开始上传文件...");
                    done = ftpClient.storeFile(remoteFilePath, fileInputStream);
                    fileInputStream.close();
                    break;

                case "fileContent":
                    // 将内容转换为输入流
                    byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
                    log.info("开始上传文件...");
                    done = ftpClient.storeFile(remoteFilePath, byteArrayInputStream);
                    byteArrayInputStream.close();
                    break;
            }
            if (done) {
                log.info("文件成功上传到 FTP 服务器");
            } else {
                log.error("文件上传失败");
            }

        } catch (IOException ex) {
            log.error("上传到 FTP 时出现错误：" + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                log.error("关闭 FTP 连接时出现错误：" + ex.getMessage());
            }
        }
    }
}
