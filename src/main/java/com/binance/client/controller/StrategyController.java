package com.binance.client.controller;

import com.binance.client.bo.StrategyBo;
import com.binance.client.service.TradeServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {
    @Resource
    private TradeServiceImpl tradeService;

    @GetMapping("/test")
    public String test() {
        return "hello test!";
    }

    @PostMapping("/add")
    public Integer addStrategy(StrategyBo strategyBo){
        Integer id=tradeService.addStrategy(strategyBo);
        return id;
    }

    @GetMapping("/start")
    public Boolean start(Integer id){
        return tradeService.start(id);
    }

    @GetMapping("/ema/start")
    public Boolean emaStart(Integer id){
        return tradeService.emaStart(id);
    }

    @GetMapping("/update/state")
    public Boolean updateSate(Integer id,Integer state){
        return tradeService.updateState(id,state);
    }
}
