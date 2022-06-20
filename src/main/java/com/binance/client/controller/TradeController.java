package com.binance.client.controller;

import com.binance.client.service.TradeServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
    @Resource
    private TradeServiceImpl tradeService;

    /**
     * 增加历史价格记录
     * @return
     */
    @GetMapping("/add/price")
    public Integer addPriceRecord(){
        Integer size=tradeService.addPriceRecord();
        return size;
    }

    /**
     * 统计下单情况
     */
    @GetMapping("/count")
    public Integer count(){
        tradeService.orderRecord();
        return 1;
    }
}
