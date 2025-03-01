package com.mingri.train12306.biz.payservice.handler.base;

import com.mingri.train12306.biz.payservice.dto.base.RefundRequest;
import com.mingri.train12306.biz.payservice.dto.base.RefundResponse;

/**
 * 抽象退款组件
 */
public abstract class AbstractRefundHandler {

    /**
     * 支付退款接口
     *
     * @param payRequest 退款请求参数
     * @return 退款响应参数
     */
    public abstract RefundResponse refund(RefundRequest payRequest);
}
