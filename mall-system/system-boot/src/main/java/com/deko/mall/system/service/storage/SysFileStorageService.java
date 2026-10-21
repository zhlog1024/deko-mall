package com.deko.mall.system.service.storage;

import com.deko.mall.system.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 */
public interface SysFileStorageService {

    /**
     * 上传文件
     *
     * @param file 表单文件对象
     * @return 文件信息
     */
    FileInfoVO uploadFile(MultipartFile file);

    /**
     * 删除文件
     *
     * @param filePath 文件完整URL
     * @return 删除结果
     */
    boolean deleteFile(String filePath);

    /**
     * 获取存储渠道编码
     *
     * @return 渠道编码
     */
    String getChannelCode();
}
