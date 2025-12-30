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

/**
 * 文件上传Controller
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    // 文件上传基础路径（相对于用户主目录，跨平台兼容）
    private static final String UPLOAD_BASE_DIR = System.getProperty("user.home") + "/club-system-uploads/";
    
    // 可访问的URL前缀
    private static final String URL_PREFIX = "http://localhost:8080/club_system_war_exploded/uploads/";
    
    /**
     * 上传Logo图片
     */
    @PostMapping("/upload/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        System.out.println("=== 开始上传Logo ===");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize());
        
        if (file.isEmpty()) {
            System.out.println("错误: 文件为空");
            return Result.error("上传文件不能为空");
        }
        
        try {
            // Logo专用子目录
            String logoDir = UPLOAD_BASE_DIR + "logos/";
            File uploadDir = new File(logoDir);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("创建Logo上传目录: " + logoDir + ", 结果: " + created);
            }
            
            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 生成唯一文件名
            String filename = "logo_" + UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(logoDir + filename);
            
            System.out.println("保存路径: " + filePath.toString());
            
            // 保存文件
            Files.write(filePath, file.getBytes());
            
            // 返回完整的访问URL（包含logos子目录）
            String fileUrl = URL_PREFIX + "logos/" + filename;
            
            System.out.println("上传成功，URL: " + fileUrl);
            System.out.println("=== 上传完成 ===");
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            System.out.println("上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传普通文件
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            // 普通文件使用files子目录
            String filesDir = UPLOAD_BASE_DIR + "files/";
            File uploadDir = new File(filesDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(filesDir + filename);
            
            Files.write(filePath, file.getBytes());
            
            String fileUrl = URL_PREFIX + "files/" + filename;
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传简历文件（专用于招新申请）
     */
    @PostMapping("/upload/resume")
    public Result<String> uploadResume(@RequestParam("file") MultipartFile file) {
        System.out.println("=== 开始上传简历 ===");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize());
        
        if (file.isEmpty()) {
            System.out.println("错误: 文件为空");
            return Result.error("上传文件不能为空");
        }
        
        // 验证文件类型（PDF、DOC、DOCX）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("无效的文件名");
        }
        
        String lowercaseFilename = originalFilename.toLowerCase();
        if (!lowercaseFilename.endsWith(".pdf") && 
            !lowercaseFilename.endsWith(".doc") && 
            !lowercaseFilename.endsWith(".docx")) {
            System.out.println("错误: 不支持的文件类型");
            return Result.error("仅支持上传 PDF、DOC、DOCX 格式的简历文件");
        }
        
        // 验证文件大小（限制10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            System.out.println("错误: 文件过大");
            return Result.error("简历文件大小不能超过 10MB");
        }
        
        try {
            // 简历专用子目录
            String resumeDir = UPLOAD_BASE_DIR + "resumes/";
            File uploadDir = new File(resumeDir);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("创建简历上传目录: " + resumeDir + ", 结果: " + created);
            }
            
            // 获取文件扩展名
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 生成唯一文件名（保留原始文件名部分以便识别）
            String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            // 移除特殊字符，防止路径问题
            baseFilename = baseFilename.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
            String filename = "resume_" + baseFilename + "_" + System.currentTimeMillis() + extension;
            Path filePath = Paths.get(resumeDir + filename);
            
            System.out.println("保存路径: " + filePath.toString());
            
            // 保存文件
            Files.write(filePath, file.getBytes());
            
            // 返回完整的访问URL（包含resumes子目录）
            String fileUrl = URL_PREFIX + "resumes/" + filename;
            
            System.out.println("上传成功，URL: " + fileUrl);
            System.out.println("=== 上传完成 ===");
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            System.out.println("上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("简历上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传凭证图片（用于财务管理）
     */
    @PostMapping("/upload/proof")
    public Result<String> uploadProof(@RequestParam("file") MultipartFile file) {
        System.out.println("=== 开始上传凭证 ===");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize());
        
        if (file.isEmpty()) {
            System.out.println("错误: 文件为空");
            return Result.error("上传文件不能为空");
        }
        
        // 验证文件类型（JPG、PNG、PDF）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("无效的文件名");
        }
        
        String lowercaseFilename = originalFilename.toLowerCase();
        if (!lowercaseFilename.endsWith(".jpg") && 
            !lowercaseFilename.endsWith(".jpeg") && 
            !lowercaseFilename.endsWith(".png") && 
            !lowercaseFilename.endsWith(".pdf")) {
            System.out.println("错误: 不支持的文件类型");
            return Result.error("仅支持上传 JPG、PNG、PDF 格式的凭证文件");
        }
        
        // 验证文件大小（限制5MB）
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            System.out.println("错误: 文件过大");
            return Result.error("凭证文件大小不能超过 5MB");
        }
        
        try {
            // 凭证专用子目录
            String proofDir = UPLOAD_BASE_DIR + "proofs/";
            File uploadDir = new File(proofDir);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("创建凭证上传目录: " + proofDir + ", 结果: " + created);
            }
            
            // 获取文件扩展名
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 生成唯一文件名
            String filename = "proof_" + UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(proofDir + filename);
            
            System.out.println("保存路径: " + filePath.toString());
            
            // 保存文件
            Files.write(filePath, file.getBytes());
            
            // 返回完整的访问URL（包含proofs子目录）
            String fileUrl = URL_PREFIX + "proofs/" + filename;
            
            System.out.println("上传成功，URL: " + fileUrl);
            System.out.println("=== 上传完成 ===");
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            System.out.println("上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("凭证上传失败: " + e.getMessage());
        }
    }
}
