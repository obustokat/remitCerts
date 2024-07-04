package com.bobwu.service;

import com.bobwu.util.APIUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HelloService {

    @Autowired
    APIUtils apiUtils;

    /**
     * 打api 组合服务器资料内容
     * rsaThirdPubKey = 三方平台公钥
     * rsaPrivateKey = 商户私钥
     * rsaPubKey = 商户公钥
     * md5Key
     */
    public String getDaifuRSAFile(String rsaThirdPubKey ,String merchantNo ,String rsaLength ,String rsaFormat){
        log.info("密钥长度 = {} ,密钥格式 = {}", rsaLength ,rsaFormat);
        rsaLength = StringUtils.isNotEmpty(rsaLength) ? (Integer.parseInt(rsaLength) % 512 == 0 ? rsaLength : "1024") : "1024";
        rsaFormat = StringUtils.isNotEmpty(rsaFormat) ? (rsaFormat.equals("PKCS#1") || rsaFormat.equals("PKCS#8") ? rsaFormat : "PKCS#1") : "PKCS#1";
        JSONObject jsonObject = apiUtils.getRsaKey(rsaLength ,rsaFormat);
        String rsaPrivateKey = jsonObject.getString("private")
                .replace("-----BEGIN PRIVATE KEY-----","")
                .replace("-----END PRIVATE KEY-----","")
                .replaceAll("\n","");
        String rsaPubKey = jsonObject.getString("public")
                .replace("-----BEGIN PUBLIC KEY-----","")
                .replace("-----END PUBLIC KEY-----","")
                .replaceAll("\n","");

        String md5Key = apiUtils.md5Ajax(merchantNo).getString("32位小写");

        // RSA 服务器资料
        StringBuilder content = new StringBuilder();
        content.append("rsaThirdPubKey").append("=").append(rsaThirdPubKey).append("\n");
        content.append("rsaPrivateKey").append("=").append(rsaPrivateKey).append("\n");
        content.append("rsaPubKey").append("=").append(rsaPubKey).append("\n");
        content.append("md5Key").append("=").append(md5Key);

        log.info("服务器资料 = {}", content.toString());
        return content.toString();
    }
}
