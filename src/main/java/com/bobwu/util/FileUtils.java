package com.bobwu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class FileUtils {

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
                            log.error("Failed to add file to zip: " + path);
                        }
                    });
        }
    }
}
