package com.deko.mall.system.service.storage.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.deko.mall.system.service.storage.SysFileStorageService;
import com.deko.mall.system.vo.FileInfoVO;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * MinIO文件存储服务实现
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "storage.type", havingValue = "minio")
@ConfigurationProperties(prefix = "storage.minio")
@RequiredArgsConstructor
@Data
public class MinioSysFileStorageServiceImpl implements SysFileStorageService {

    /**
     * 服务Endpoint(http://localhost:9000)
     */
    private String endpoint;
    /**
     * 访问凭据
     */
    private String accessKey;
    /**
     * 凭据密钥
     */
    private String secretKey;
    /**
     * 存储桶名称
     */
    private String bucketName;
    /**
     * 自定义域名
     */
    private String customDomain;

    private MinioClient minioClient;

    /**
     * PUBLIC桶策略
     */
    private static String publicBucketPolicy(String bucketName) {
        return "{\"Version\":\"2012-10-17\","
                + "\"Statement\":[{\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:ListBucketMultipartUploads\",\"s3:GetBucketLocation\",\"s3:ListBucket\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "\"]},"
                + "{\"Effect\":\"Allow\"," + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:ListMultipartUploadParts\",\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
    }

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // 判断存储桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                // 创建存储桶并设置为PUBLIC读写
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // 设置存储桶策略
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(publicBucketPolicy(bucketName))
                        .build());
            }
        } catch (Exception e) {
            log.error("MinIO客户端初始化失败", e);
            throw new RuntimeException("文件存储服务初始化失败");
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
            // 创建PutObject请求
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 获取文件访问路径
            String fileUrl;
            if (StrUtil.isNotBlank(customDomain)) {
                fileUrl = customDomain + "/" + fileName;
            } else {
                fileUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .method(Method.GET)
                        .build());
            }

            FileInfoVO fileInfo = new FileInfoVO();
            fileInfo.setName(fileName);
            fileInfo.setUrl(fileUrl);
            return fileInfo;
        } catch (Exception e) {
            log.error("MinIO文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    @SneakyThrows
    public boolean deleteFile(String filePath) {
        Assert.notBlank(filePath, "删除文件路径不能为空");

        try {
            String fileHost = StrUtil.isNotBlank(customDomain) ? customDomain : endpoint;
            String fileName = filePath.substring(fileHost.length() + 1);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("MinIO文件删除失败", e);
            return false;
        }
    }

    @Override
    public String getChannelCode() {
        return "MINIO";
    }
}
