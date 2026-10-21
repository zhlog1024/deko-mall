package com.deko.mall.oms.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.oms.annotation.OrderLog;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderLog;
import com.deko.mall.oms.model.form.OrderSubmitForm;
import com.deko.mall.oms.service.order.OrderFacadeService;
import com.deko.mall.oms.service.order.OrderLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 订单日志切面
 * 统一记录订单操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OrderLogAspect {

    private final OrderLogService orderLogService;
    private final OrderFacadeService orderFacadeService;

    @Around("@annotation(orderLog)")
    public Object around(ProceedingJoinPoint point, OrderLog orderLog) throws Throwable {
        // 1. 执行原方法
        Object result = point.proceed();

        try {
            // 2. 获取订单信息
            String orderNo = null;

            // 2.1 如果返回值是订单号（订单提交场景）
            if (result instanceof String) {
                orderNo = (String) result;
            } else {
                // 2.2 从参数中获取订单号（订单状态变更场景）
                orderNo = getOrderNo(point);
            }

            if (StrUtil.isBlank(orderNo)) {
                return result;
            }

            // 3. 获取订单对象
            OmsOrder order = orderFacadeService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return result;
            }

            // 4. 构建操作日志
            OmsOrderLog log = new OmsOrderLog();
            log.setOrderId(order.getId());
            log.setOrderNo(orderNo);
            log.setOrderStatus(order.getStatus());
            log.setOperateUser(SecurityUtils.getUsername());

            // 5. 获取操作描述
            String detail = orderLog.detail();
            if (StrUtil.isBlank(detail)) {
                detail = point.getSignature().getName();
            }

            // 6. 对于订单提交场景，记录提交参数
            if ("submitOrder".equals(point.getSignature().getName())) {
                Object[] args = point.getArgs();
                if (args != null && args.length > 0 && args[0] instanceof OrderSubmitForm) {
                    OrderSubmitForm form = (OrderSubmitForm) args[0];
                    detail += String.format("，提交参数：%s", JSONUtil.toJsonStr(form));
                }
            }

            log.setDetail(detail);
            orderLogService.save(log);

            return result;
        } catch (Exception e) {
            log.error("记录订单日志失败", e);
            return result;
        }
    }

    /**
     * 获取订单号
     * 支持以下参数形式：
     * 1. 直接传入订单号(String)
     * 2. 传入订单对象(OmsOrder)
     * 3. 第一个参数名为orderNo
     */
    private String getOrderNo(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        // 1. 检查是否直接传入订单号
        if (args[0] instanceof String) {
            return (String) args[0];
        }

        // 2. 检查是否传入订单对象
        if (args[0] instanceof OmsOrder) {
            return ((OmsOrder) args[0]).getOrderNo();
        }

        // 3. 检查参数名是否为orderNo
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] parameterNames = signature.getParameterNames();
        if (parameterNames != null && parameterNames.length > 0
                && "orderNo".equals(parameterNames[0])) {
            return args[0].toString();
        }

        return null;
    }
}
