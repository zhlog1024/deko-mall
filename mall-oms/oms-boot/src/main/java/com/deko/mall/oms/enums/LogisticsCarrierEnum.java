package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 快递公司枚举
 */
@Getter
public enum LogisticsCarrierEnum implements IBaseEnum<String> {

    SF("shunfeng", "顺丰速运"),
    SF_KY("shunfengkuaiyun", "顺丰快运"),
    JT("jtexpress", "极兔速递"),
    YTO("yuantong", "圆通速递"),
    ZTO("zhongtong", "中通快递"),
    ZTO_KY("zhongtongkuaiyun", "中通快运"),
    YD("yunda", "韵达快递"),
    YD_KY("yundakuaiyun", "韵达快运"),
    STO("shentong", "申通快递"),
    POST("youzhengguonei", "邮政快递包裹"),
    POST_DS("youzhengdsbk", "邮政电商标快"),
    JD("jd", "京东物流"),
    JD_KY("jingdongkuaiyun", "京东快运"),
    EMS("ems", "EMS"),
    DB("debangkuaidi", "德邦快递"),
    POST_BK("youzhengbk", "邮政标准快递"),
    CN("danniao", "菜鸟速递"),
    DB_WL("debangwuliu", "德邦物流"),
    KY("kuayue", "跨越速运"),
    AN("annengwuliu", "安能快运"),
    JG("jinguangsudikuaijian", "京广速递"),
    BS("baishiwuliu", "百世快运"),
    SX("sxjdfreight", "顺心捷达"),
    AD("annto", "安得物流");

    private final String value;
    private final String label;

    LogisticsCarrierEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
