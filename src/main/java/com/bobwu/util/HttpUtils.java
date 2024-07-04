package com.bobwu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
public class HttpUtils {
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
            log.info("请求参数 = {}", urlParameters);
            OutputStream os = con.getOutputStream();
            os.write(urlParameters.getBytes());
            os.flush();
            os.close();

            // 获取响应代码
            int responseCode = con.getResponseCode();
            log.info("响应代码 = {}", responseCode);

            // 读取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 打印响应内容
            log.info("响应内容 = {}", response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}
