package com.mingri.train12306.biz.ticketservice.remote;

import com.mingri.train12306.biz.ticketservice.remote.dto.RefundReqDTO;
import com.mingri.train12306.biz.ticketservice.remote.dto.RefundRespDTO;
import com.mingri.train12306.framework.starter.convention.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 支付单远程调用服务
 */
@FeignClient(value = "train12306-pay${unique-name:}-service", url = "${remote.pay.url}")
public interface PayRemoteService {

    /**
     * 公共退款接口
     */
    @PostMapping("/api/pay-service/common/refund")
    Result<RefundRespDTO> commonRefund(@RequestBody RefundReqDTO requestParam);
}
