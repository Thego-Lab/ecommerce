package com.ecommerce.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ecommerce.config.OssProperties;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final OssProperties ossProperties;

    public FileServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String upload(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = "product/" + UUID.randomUUID().toString().replace("-", "") + ext;

        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );

        try {
            ossClient.putObject(ossProperties.getBucketName(), fileName, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        } finally {
            ossClient.shutdown();
        }

        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + fileName;
    }
}
