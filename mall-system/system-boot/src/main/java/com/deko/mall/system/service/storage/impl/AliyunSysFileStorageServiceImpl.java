package com.deko.mall.system.service.storage.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.deko.mall.system.service.storage.SysFileStorageService;
import com.deko.mall.system.vo.FileInfoVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 阿里云文件存储服务实现
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "storage.type", havingValue = "aliyun")
@ConfigurationProperties(prefix = "storage.aliyun")
@RequiredArgsConstructor
@RefreshScope
@Data
public class AliyunSysFileStorageServiceImpl implements SysFileStorageService {
    /**
     * 服务Endpoint
     */
    private String endpoint;
    /**
     * 访问凭据
     */
    private String accessKeyId;
    /**
     * 凭据密钥
     */
    private String accessKeySecret;
    /**
     * 存储桶名称
     */
    private String bucketName;

    private OSS aliyunOssClient;

    @PostConstruct
    public void init() {
        try {
            aliyunOssClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 判断存储桶是否存在，不存在则创建
            if (!aliyunOssClient.doesBucketExist(bucketName)) {
                aliyunOssClient.createBucket(bucketName);
            }
        } catch (Exception e) {
            log.error("阿里云OSS客户端初始化失败", e);
            throw new RuntimeException("文件存储服务初始化失败");
        }
    }

    @PreDestroy
    public void destroy() {
        if (aliyunOssClient != null) {
            aliyunOssClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    @Override
    @SneakyThrows
    public FileInfoVO uploadFile(MultipartFile file) {
        Assert.notNull(file, "上传文件不能为空");

        // 生成文件名(日期文件夹)
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        String uuid = IdUtil.simpleUUID();
        String fileName = DateUtil.format(LocalDateTime.now(), "yyyyMMdd") + "/" + uuid + "." + suffix;

        try (InputStream inputStream = file.getInputStream()) {
            // 设置上传文件的元信息，例如Content-Type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            // 创建PutObjectRequest对象，指定Bucket名称、对象名称和输入流
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
            // 上传文件
            aliyunOssClient.putObject(putObjectRequest);

            // 获取文件访问路径
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            FileInfoVO fileInfo = new FileInfoVO();
            fileInfo.setName(fileName);
            fileInfo.setUrl(fileUrl);
            return fileInfo;
        } catch (Exception e) {
            log.error("阿里云OSS文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        Assert.notBlank(filePath, "删除文件路径不能为空");
        try {
            String fileHost = "https://" + bucketName + "." + endpoint;
            String fileName = filePath.substring(fileHost.length() + 1);
            aliyunOssClient.deleteObject(bucketName, fileName);
            return true;
        } catch (Exception e) {
            log.error("阿里云OSS文件删除失败", e);
            return false;
        }
    }

    @Override
    public String getChannelCode() {
        return "ALIYUN_OSS";
    }
}
