package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 在线观众（前端契约：live-info 的 online_user 元素）
 * <p>
 * 前端 live-content.vue 模板为 {@code v-for="item in online_user"} +
 * {@code <image :src="item.avatar" />}，即期望<b>对象数组</b>；
 * 若返回裸字符串数组（{@code List<String>}），{@code item.avatar} 为 undefined，
 * 头像会渲染成空（background-image: none）且不报错。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveOnlineUserVO {

    /**
     * 观众头像地址
     */
    private String avatar;
}
