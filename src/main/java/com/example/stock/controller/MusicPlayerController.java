package com.example.stock.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

/**
 * Cyber Music Player 官网 + 下载
 */
@Controller
@RequestMapping("/api/music-player")
public class MusicPlayerController {

    @Value("${music-player.exe-path:/opt/hermes-study/downloads/Music-player.exe}")
    private String exePath;

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadExe() {
        File file = new File(exePath);

        if (!file.exists()) {
            // 如果配置路径不存在，尝试从 classpath 同级目录查找
            File fallback = new File("downloads/Music-player.exe");
            if (fallback.exists()) {
                file = fallback;
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        FileSystemResource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Music-player.exe\"")
                .body(resource);
    }
}
