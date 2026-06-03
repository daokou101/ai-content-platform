package com.pzhu.controller;

import com.pzhu.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 * <p>
 * 【为什么需要这个类？】
 * 在员工管理中，需要上传员工头像。前端通过 el-upload 组件将图片文件发送到后端，
 * 后端接收文件并保存到服务器磁盘上，然后返回图片的访问 URL。
 * <p>
 * 【上传流程】
 * 前端选择图片 → 发送 POST /upload 请求（携带文件）→ 后端保存文件 → 返回图片 URL
 */
@Slf4j // Lombok：自动生成 log 日志对象
@RestController // RESTful 控制器
public class UploadController {

    /**
     * 文件保存路径
     *
     * 【@Value 注解】
     * 从 application.yml 配置文件中读取值。
     * ${file.upload-path} 表示读取配置文件中 file.upload-path 的值。
     * 如果配置文件没配，就用默认值 "E:/uploads/"
     */
    @Value("${file.upload-path:E:/uploads/}")
    private String uploadPath;

    /**
     * 上传文件
     *
     * 【@PostMapping 注解】
     * 处理 POST 请求，路径为 /upload
     *
     * 【MultipartFile 参数】
     * Spring MVC 会自动将前端上传的文件封装成 MultipartFile 对象。
     * 注意：参数名必须和前端的表单字段名一致（前端是 file，这里参数名也是 file）
     *
     * 前端 el-upload 默认的字段名就是 file，所以这里用 file 接收
     *
     * @param file 前端上传的文件
     * @return 包含图片访问 URL 的 Result 对象
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        // 1. 获取原始文件名（比如 "头像.jpg"）
        String originalFilename = file.getOriginalFilename();
        log.info("接收上传文件：{}", originalFilename);

        // 2. 获取文件扩展名（比如 ".jpg"）
        //    FilenameUtils 是 commons-io 提供的工具类
        String extension = FilenameUtils.getExtension(originalFilename);

        // 3. 生成新的唯一文件名，防止文件名冲突
        //    使用 UUID 生成唯一标识 + 原文件扩展名
        //    比如：a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
        String newFileName = UUID.randomUUID() + "." + extension;

        // 4. 创建目标文件对象（指定保存路径和文件名）
        File targetFile = new File(uploadPath, newFileName);

        // 5. 如果保存目录不存在，自动创建目录
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        // 6. 将上传的文件保存到目标位置
        try {
            file.transferTo(targetFile);
            log.info("文件保存成功：{}", targetFile.getAbsolutePath());

            // 7. 返回图片的访问 URL
            //    前端可以通过 http://localhost:8080/images/文件名 访问图片
            //    注意：需要配置静态资源映射才能访问（在 WebConfig 中配置）
            String url = "/images/" + newFileName;
            return Result.success(url);

        } catch (IOException e) {
            log.error("文件上传失败：", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
