package com.bobwu.bean;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Ftp {
    @Value("${ftp.server}")
    private String server;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.user}")
    private String user;

    @Value("${ftp.pass}")
    private String pass;

    @Value("${ftp.remotePath}")
    private String remotePath;

}
