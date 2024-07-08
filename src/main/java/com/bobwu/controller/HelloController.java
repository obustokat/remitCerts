package com.bobwu.controller;

import com.bobwu.bean.DaifuRSA;
import com.bobwu.service.HelloService;
import com.bobwu.util.FTPUtils;
import com.bobwu.util.FileUtils;
import com.bobwu.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    HelloService helloService;

    @Autowired
    FileUtils fileUtils;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    FTPUtils ftpUtils;

    @Value("${ftp.remotePath}")
    private String FTP_REMOTEPATH;

    /**
     * 寫入文件 -> 壓縮 -> 上傳FTP -> 取得ngrok forward (映射localhost的外網) -> 返回下載連結
     * 文件寫入、zip位置: D:\workspace\remitCerts\temp
     * ftp位置: (virtual path= / ,Native path= D:/FTP)
     * @param data
     * @return
     * @throws IOException
     */
    @PostMapping("/getDaifuRSAFile")
    public ResponseEntity<Map<String, String>>  getDaifuRSAFile(@RequestBody DaifuRSA data) throws IOException {
        String rsaThirdPubKey = data.getRsaThirdPubKey();// 三方平台公钥
        String daifuId = data.getDaifuId(); // daifu
        String merchantNo = data.getMerchantNo(); // 商户号
        String rsaLength = data.getRsaLength(); // 密钥长度
        String rsaFormat = data.getRsaFormat(); // 密钥格式

        String d4 = "remitCerts/temp/" + daifuId;
        String currentDir = System.getProperty("user.dir"); // 当前工作目录
        String directoryPath = d4 + "/" + merchantNo;
        String filePath = directoryPath + "/checkOrderKey.txt"; // 预设档名
        String zipFilePath = currentDir + "/" + d4;
        log.info("currentDir = {} ,directoryPath = {} ,filePath = {} ,zipFilePath = {}" ,currentDir ,directoryPath ,filePath ,zipFilePath);
        String result = helloService.getDaifuRSAFile(rsaThirdPubKey ,merchantNo ,rsaLength ,rsaFormat); //产出服务器资料内容

        // 存本地:
        // TODO: 通过流的方式可以直接将数据从输入流上传到FTP服务器，从而避免将文件临时存储在本地
        filePath = fileUtils.writeToFile(result ,filePath ,directoryPath); // 建立资料夹、档案，写入档案

        Map<String, String> response = new HashMap<>();

        if (StringUtils.isNotBlank(filePath)) {
            log.info("文件路径 = {}", filePath);
            Path sourceDir = Paths.get(zipFilePath);
            zipFilePath = zipFilePath + ".zip";
            Path zipFile  = Paths.get(zipFilePath);
            fileUtils.zipDirectory(sourceDir ,zipFile); // zip 该资料夹
            // 上传FTP:

            String method = "localFilePath";
            String downloadUrl = ftpUtils.uploadContentToFTP(zipFilePath ,method);
            if(StringUtils.isNotBlank(downloadUrl)){
                response.put("message", "File uploaded successfully");
                downloadUrl = httpUtils.getNgrokForward() + "/api/ftp_files/" + downloadUrl;
                response.put("fileName", downloadUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.add("Custom-Header", "FileUploadSuccess");

                return new ResponseEntity<>(response, headers, HttpStatus.OK);
            } else {
                response.put("message", "Error uploading file");
                HttpHeaders headers = new HttpHeaders();
                headers.add("Custom-Header", "FileUploadError");

                return new ResponseEntity<>(response, headers, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else{
            response.put("message", "Error file path");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Custom-Header", "FileUploadError");

            return new ResponseEntity<>(response, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *  下載FTP 文件
     * @param filename
     * @return
     * @throws IOException
     */
    @GetMapping("/ftp_files/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) throws IOException {
        FTPClient ftpClient = ftpUtils.connectFTP();
        // Connect to FTP server, login, etc.

        // Example: Download file as byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ftpClient.retrieveFile(FTP_REMOTEPATH + filename, baos);

        byte[] fileBytes = baos.toByteArray();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(fileBytes);
    }
}