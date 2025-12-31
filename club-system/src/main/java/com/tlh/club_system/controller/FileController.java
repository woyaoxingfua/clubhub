package com.tlh.club_system.controller;

import com.tlh.club_system.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    private static final String UPLOAD_BASE_DIR = System.getProperty("user.home") + "/club-system-uploads/";
    private static final String URL_PREFIX = "http://localhost:8080/club_system_war_exploded/uploads/";

    @PostMapping("/upload/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "logos/", null, 0);
    }
    
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "files/", null, 0);
    }
    
    @PostMapping("/upload/resume")
    public Result<String> uploadResume(@RequestParam("file") MultipartFile file) {
        String[] allowedExtensions = {".pdf", ".doc", ".docx"};
        return uploadFile(file, "resumes/", allowedExtensions, 10 * 1024 * 1024);
    }
    
    @PostMapping("/upload/proof")
    public Result<String> uploadProof(@RequestParam("file") MultipartFile file) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".pdf"};
        return uploadFile(file, "proofs/", allowedExtensions, 5 * 1024 * 1024);
    }
    
    private Result<String> uploadFile(MultipartFile file, String subDir, String[] allowedExtensions, long maxSize) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("无效的文件名");
        }
        
        if (maxSize > 0 && file.getSize() > maxSize) {
            return Result.error("文件大小超过限制");
        }
        
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        if (allowedExtensions != null) {
            boolean allowed = false;
            String lowerExt = extension.toLowerCase();
            for (String ext : allowedExtensions) {
                if (lowerExt.equals(ext)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                return Result.error("不支持的文件类型");
            }
        }
        
        try {
            String targetDir = UPLOAD_BASE_DIR + subDir;
            File uploadDir = new File(targetDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 对于简历，保留部分原始文件名以便识别
            String filename;
            if (subDir.contains("resumes")) {
                String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                baseFilename = baseFilename.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
                filename = "resume_" + baseFilename + "_" + System.currentTimeMillis() + extension;
            } else {
                filename = (subDir.contains("logos") ? "logo_" : "") + UUID.randomUUID().toString() + extension;
            }
            
            Path filePath = Paths.get(targetDir + filename);
            Files.write(filePath, file.getBytes());
            
            return Result.success(URL_PREFIX + subDir + filename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
