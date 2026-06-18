package com.ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /** 上传文件到 OSS，返回可访问的 URL */
    String upload(MultipartFile file);
}
