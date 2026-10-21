package com.deko.mall.system.config;

import com.deko.mall.common.result.IResultCode;
import lombok.Getter;

/**
 * SYS系统服务错误码枚举
 * <p>
 * 码段前缀: SYS_001 ~ SYS_999
 * 枚举值命名: 全大写下划线 (如 DICT_TYPE_NOT_FOUND)
 */
@Getter
public enum SysResultCode implements IResultCode {

    // 字典错误 (SYS_001 ~ SYS_009)
    DICT_TYPE_NOT_FOUND("SYS_001", "字典类型不存在"),
    DICT_ITEM_NOT_FOUND("SYS_002", "字典项不存在"),

    // 文件错误 (SYS_010 ~ SYS_019)
    FILE_UPLOAD_FAILED("SYS_010", "文件上传失败"),
    FILE_NOT_FOUND("SYS_011", "文件不存在"),
    FILE_SIZE_EXCEED("SYS_012", "文件大小超出限制"),

    // 短信错误 (SYS_020 ~ SYS_029)
    SMS_SEND_FAILED("SYS_020", "短信发送失败"),
    SMS_TEMPLATE_NOT_FOUND("SYS_021", "短信模板不存在"),

    // 消息错误 (SYS_070 ~ SYS_079)
    MSG_CHANNEL_NOT_FOUND("SYS_070", "消息渠道不存在"),
    MSG_TEMPLATE_NOT_FOUND("SYS_071", "模板不存在"),
    MSG_TEMPLATE_CODE_EXISTS("SYS_072", "模板编码已存在"),
    MSG_TEMPLATE_RENDER_FAILED("SYS_073", "模板渲染失败"),

    // 行政区划错误 (SYS_030 ~ SYS_039)
    REGION_NOT_FOUND("SYS_030", "行政区划不存在"),

    // 菜单/权限错误 (SYS_040 ~ SYS_049)
    MENU_NOT_FOUND("SYS_040", "菜单不存在"),
    PERMISSION_DENIED("SYS_041", "权限不足"),

    // 角色/用户错误 (SYS_050 ~ SYS_059)
    ROLE_NOT_FOUND("SYS_050", "角色不存在"),
    USER_NOT_FOUND("SYS_051", "用户不存在"),

    // 配置错误 (SYS_060 ~ SYS_069)
    CONFIG_NOT_FOUND("SYS_060", "配置不存在"),

    // 通用操作错误 (SYS_080 ~ SYS_089)
    OPERATION_FAILED("SYS_080", "操作失败"),
    DATA_NOT_FOUND("SYS_081", "数据不存在"),
    PARAM_INVALID("SYS_082", "参数非法"),

    // 系统保留错误码 (SYS_900 ~ SYS_999)
    SYSTEM_ERROR("SYS_900", "系统错误"),
    UNKNOWN_ERROR("SYS_999", "未知错误");

    private final String code;
    private final String msg;

    SysResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
