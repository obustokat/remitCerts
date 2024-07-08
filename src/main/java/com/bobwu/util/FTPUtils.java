package com.bobwu.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Component
public class FTPUtils {

    @Value("${ftp.server}")
    private String FTP_SERVER;

    @Value("${ftp.port}")
    private int FTP_PORT;

    @Value("${ftp.user}")
    private String FTP_USER;

    @Value("${ftp.pass}")
    private String FTP_PASS;

    @Value("${ftp.remotePath}")
    private String FTP_REMOTEPATH;

    public FTPClient connectFTP() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(FTP_SERVER, FTP_PORT);
        ftpClient.login(FTP_USER, FTP_PASS);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        return ftpClient;
    }

    public void disconnectFTP(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ex) {
            log.error("关闭 FTP 连接时出现错误：" + ex.getMessage());
        }
    }

    public String uploadContentToFTP(String localFilePath ,String method) throws IOException {
        String content = "";
        String fileName = "";
        FTPClient ftpClient = connectFTP();
        try {

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
                    fileName = localFile.getName();
                    remoteFilePath = FTP_REMOTEPATH + fileName;
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
                log.info("文件成功上传到 FTP 服务器 ,fileName is = {}" ,fileName);
            } else {
                log.error("文件上传失败");
            }

        } catch (IOException ex) {
            log.error("上传到 FTP 时出现错误：" + ex.getMessage());
        } finally {
            disconnectFTP(ftpClient);
        }
        return fileName;
    }
}
