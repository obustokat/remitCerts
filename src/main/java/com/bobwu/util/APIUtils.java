package com.bobwu.util;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * 其他错误: 没资料可能是RSA或MD5网站挂了
 */
@Slf4j
@Component
public class APIUtils {

    /**
     * 打 md5 api
     * POST application/x-www-form-urlencoded
     * curl -d "str=123" -X POST https://www.iamwawa.cn/home/md5/ajax
     */
    private static final String MD5URL = "https://www.iamwawa.cn/home/md5/ajax";

    public JSONObject md5Ajax(String str){
        String urlParameters = "str=" + str;
        String result = HttpUtils.doPostFormData(MD5URL ,urlParameters);
        JSONObject jsonObject = new JSONObject(result);
        // 获取 JSON 对象中的值
        int status = jsonObject.getInt("status");
        String info = jsonObject.getString("info");
        JSONArray data = jsonObject.getJSONArray("data");
        // 创建一个新的 JSONObject
        // 遍历 JSONArray 并将每个 JSONObject 的键值对放入新的 JSONObject 中
        JSONObject jsonObject2 = new JSONObject();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            jsonObject2.put(item.getString("name") ,item.getString("value"));
        }
        log.info("gateway = {} ,data = {}",MD5URL , jsonObject2);
        return jsonObject2;
    }

    /**
     * 打 rsa api
     * POST application/x-www-form-urlencoded
     * curl -d "rsaLength=2048&rsaFormat=PKCS#1" -X POST https://www.bejson.com/Bejson/Api/Rsa/getRsaKey
     */
    private static final String RSAURL = "https://www.bejson.com/Bejson/Api/Rsa/getRsaKey";
    public JSONObject getRsaKey(String rsaLength ,String rsaFormat){
        String urlParameters = "rsaLength=" + rsaLength + "&rsaFormat=" + rsaFormat;
        String result = HttpUtils.doPostFormData(RSAURL ,urlParameters);
        JSONObject jsonObject = new JSONObject(result);
        // 获取 JSON 对象中的值
//        int code = jsonObject.getInt("code");
//        String msg = jsonObject.getString("msg");
        log.info("gateway = {} ,data = {}",RSAURL , jsonObject);
        JSONObject data = jsonObject.getJSONObject("data");
//        String privateKey = data.getString("private");
//        String publicKey = data.getString("public");
//        log.info("privateRsa = ", privateKey);
//        log.info("publicRsa = ", publicKey);
        return data;
    }
}
