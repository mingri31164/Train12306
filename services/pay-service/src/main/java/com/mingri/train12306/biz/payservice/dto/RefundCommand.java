package com.mingri.train12306.biz.payservice.dto;

import lombok.Data;
import com.mingri.train12306.biz.payservice.dto.base.AbstractRefundRequest;

import java.math.BigDecimal;

/**
 * 退款请求命令
 */
@Data
public final class RefundCommand extends AbstractRefundRequest {

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 交易凭证号
     */
    private String tradeNo;
}
