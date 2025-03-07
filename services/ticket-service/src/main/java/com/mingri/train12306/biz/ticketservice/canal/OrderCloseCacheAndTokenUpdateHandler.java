package com.mingri.train12306.biz.ticketservice.canal;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mingri.train12306.biz.ticketservice.common.enums.CanalExecuteStrategyMarkEnum;
import com.mingri.train12306.biz.ticketservice.mq.event.CanalBinlogEvent;
import com.mingri.train12306.biz.ticketservice.remote.TicketOrderRemoteService;
import com.mingri.train12306.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO;
import com.mingri.train12306.biz.ticketservice.remote.dto.TicketOrderPassengerDetailRespDTO;
import com.mingri.train12306.biz.ticketservice.service.SeatService;
import com.mingri.train12306.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import com.mingri.train12306.biz.ticketservice.service.handler.ticket.tokenbucket.TicketAvailabilityTokenBucket;
import com.mingri.train12306.framework.starter.common.toolkit.BeanUtil;
import com.mingri.train12306.framework.starter.convention.result.Result;
import com.mingri.train12306.framework.starter.designpattern.strategy.AbstractExecuteStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 订单关闭或取消后置处理组件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCloseCacheAndTokenUpdateHandler implements AbstractExecuteStrategy<CanalBinlogEvent, Void> {

    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final SeatService seatService;
    private final TicketAvailabilityTokenBucket ticketAvailabilityTokenBucket;

    @Override
    public void execute(CanalBinlogEvent message) {
        List<Map<String, Object>> messageDataList = message.getData().stream()
                .filter(each -> each.get("status") != null)
                .filter(each -> Objects.equals(each.get("status"), "30"))
                .toList();
        if (CollUtil.isEmpty(messageDataList)) {
            return;
        }
        for (Map<String, Object> each : messageDataList) {
            Result<TicketOrderDetailRespDTO> orderDetailResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(each.get("order_sn").toString());
            TicketOrderDetailRespDTO orderDetailResultData = orderDetailResult.getData();
            if (orderDetailResult.isSuccess() && orderDetailResultData != null) {
                String trainId = String.valueOf(orderDetailResultData.getTrainId());
                List<TicketOrderPassengerDetailRespDTO> passengerDetails = orderDetailResultData.getPassengerDetails();
                seatService.unlock(trainId, orderDetailResultData.getDeparture(), orderDetailResultData.getArrival(), BeanUtil.convert(passengerDetails, TrainPurchaseTicketRespDTO.class));
                ticketAvailabilityTokenBucket.rollbackInBucket(orderDetailResultData);
            }
        }
    }

    @Override
    public String mark() {
        return CanalExecuteStrategyMarkEnum.T_ORDER.getActualTable();
    }

    @Override
    public String patternMatchMark() {
        return CanalExecuteStrategyMarkEnum.T_ORDER.getPatternMatchTable();
    }
}
