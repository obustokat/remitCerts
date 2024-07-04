package com.bobwu.controller;

import com.bobwu.bean.DaifuRSA;
import com.bobwu.service.HelloService;
import com.bobwu.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    HelloService helloService;

    @Autowired
    FileUtils fileUtils;

    @PostMapping("/getDaifuRSAFile")
    public void getDaifuRSAFile(@RequestBody DaifuRSA data) throws IOException {
        String rsaThirdPubKey = data.getRsaThirdPubKey();// 三方平台公钥
        String daifuId = data.getDaifuId(); // daifu
        String merchantNo = data.getMerchantNo(); // 商户号
        String rsaLength = data.getRsaLength(); // 密钥长度
        String rsaFormat = data.getRsaFormat(); // 密钥格式

        String currentDir = System.getProperty("user.dir"); // 当前工作目录
        String directoryPath = "Temp/" + daifuId + "/" + merchantNo;
        String filePath = directoryPath + "/checkOrderKey.txt"; // 预设档名
        String zipFilePath = currentDir + "/Temp/" + daifuId;
        log.info("currentDir = {} ,directoryPath = {} ,filePath = {} ,zipFilePath = {}" ,currentDir ,directoryPath ,filePath ,zipFilePath);
        String result = helloService.getDaifuRSAFile(rsaThirdPubKey ,merchantNo ,rsaLength ,rsaFormat); //产出服务器资料内容

        // 存本地:
        filePath = fileUtils.writeToFile(result ,filePath ,directoryPath); // 建立资料夹、档案，写入档案
        if (filePath != null) {
            log.info("文件路径 = {}", filePath);
            Path sourceDir = Paths.get(zipFilePath);
            zipFilePath = zipFilePath + ".zip";
            Path zipFile  = Paths.get(zipFilePath);
            fileUtils.zipDirectory(sourceDir ,zipFile); // zip 该资料夹
            // 上传FTP:
            String method = "localFilePath";
            fileUtils.uploadContentToFTP(zipFilePath ,method);
        }
    }
}