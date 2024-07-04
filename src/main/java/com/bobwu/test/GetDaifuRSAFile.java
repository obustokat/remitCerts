package com.bobwu.test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GetDaifuRSAFile {

    public static void main(String[] args) throws IOException, Exception {
        String rsaThirdPubKey = "123";// 三方平台公钥
        String daifuId = "daifu001"; // daifu
        String merchantNo = "70012"; // 商户号
        String directoryPath = daifuId + "/" + merchantNo;
        String filePath = directoryPath + "/checkOrderKey.txt"; // 预设档名
        String result = result(rsaThirdPubKey); //产出服务器资料内容
        filePath = writeToFile(result ,filePath ,directoryPath); // 建立资料夹、档案，写入档案
        if (filePath != null) {
            System.out.println("文件路径：" + filePath);
            zipDirectory(daifuId); // zip 该资料夹
        }
    }

    public static String writeToFile(String content ,String filePath ,String directoryPath){
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
                System.out.println("成功写入文件");
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            System.out.println("写入文件时出现错误：" + e.getMessage());
            return null;
        }
    }

    public static void zipDirectory(String dirPath) {
        try (FileOutputStream fos = new FileOutputStream(dirPath + ".zip");
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            Path sourcePath = Paths.get(dirPath);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.out.println("压缩文件时出现错误：" + e.getMessage());
                        }
                    });
            System.out.println("成功压缩文件夹");
        } catch (IOException e) {
            System.out.println("压缩文件夹时出现错误：" + e.getMessage());
        }
    }

    // 服务器资料内容
    public static String result(String rsaThirdPubKey){
        System.out.println("－－－－－－－－－－－－－－－服务器资料内容 start");
        JSONObject jsonObject = getRsaKey("1024" ,"PKCS#1");
        String rsaPrivateKey = jsonObject.getString("private")
                .replace("-----BEGIN PRIVATE KEY-----","")
                .replace("-----END PRIVATE KEY-----","")
                .replaceAll("\n","");
        String rsaPubKey = jsonObject.getString("public")
                .replace("-----BEGIN PUBLIC KEY-----","")
                .replace("-----END PUBLIC KEY-----","")
                .replaceAll("\n","");

        String md5Key = md5Ajax("123").getString("32位小写");

        // RSA 服务器资料
        StringBuilder content = new StringBuilder();
        content.append("rsaThirdPubKey").append("=").append(rsaThirdPubKey).append("\n");
        content.append("rsaPrivateKey").append("=").append(rsaPrivateKey).append("\n");
        content.append("rsaPubKey").append("=").append(rsaPubKey).append("\n");
        content.append("md5Key").append("=").append(md5Key);

        System.out.println();
        System.out.println(content.toString());
        System.out.println("－－－－－－－－－－－－－－－服务器资料内容 end");
        return content.toString();
    }

    /**
     * 打 md5 api
     * POST application/x-www-form-urlencoded
     * curl -d "str=123" -X POST https://www.iamwawa.cn/home/md5/ajax
     */
    private static final String MD5URL = "https://www.iamwawa.cn/home/md5/ajax";

    public static JSONObject md5Ajax(String str){
        System.out.println("－－－－－－－－－－－－－－－md5 start ,gateway = " + MD5URL);
        String urlParameters = "str=" + str;
        String result = doPostFormData(MD5URL ,urlParameters);
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
        System.out.println("md5Str = " + jsonObject2.toString());
        System.out.println("－－－－－－－－－－－－－－－md5 end");
        return jsonObject2;
    }


    /**
     * 打 rsa api
     * POST application/x-www-form-urlencoded
     * curl -d "rsaLength=2048&rsaFormat=PKCS#1" -X POST https://www.bejson.com/Bejson/Api/Rsa/getRsaKey
     */
    private static final String RSAURL = "https://www.bejson.com/Bejson/Api/Rsa/getRsaKey";
    public static JSONObject getRsaKey(String rsaLength ,String rsaFormat){
        System.out.println("－－－－－－－－－－－－－－－rsa start ,gateway = " + RSAURL);
        String urlParameters = "rsaLength=" + rsaLength + "&rsaFormat=" + rsaFormat;
        String result = doPostFormData(RSAURL ,urlParameters);
        JSONObject jsonObject = new JSONObject(result);
        // 获取 JSON 对象中的值
        int code = jsonObject.getInt("code");
        String msg = jsonObject.getString("msg");
        System.out.println("code = " + code + ", msg = " + msg);
        JSONObject data = jsonObject.getJSONObject("data");
//        String privateKey = data.getString("private");
//        String publicKey = data.getString("public");
//        System.out.println("privateRsa = ", privateKey);
//        System.out.println("publicRsa = ", publicKey);
        System.out.println("－－－－－－－－－－－－－－－rsa end");
        return data;
    }

    public static String doPostFormData(String spec ,String urlParameters) {
        StringBuilder response = new StringBuilder();
        try {
            URL obj = new URL(spec);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);

            // 设置请求参数
            System.out.println("请求参数 = " + urlParameters);
            OutputStream os = con.getOutputStream();
            os.write(urlParameters.getBytes());
            os.flush();
            os.close();

            // 获取响应代码
            int responseCode = con.getResponseCode();
            System.out.println("响应代码 = " + responseCode);

            // 读取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 打印响应内容
            System.out.println("响应内容 = " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}