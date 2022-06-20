package com.binance.client.examples;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.AccountBalance;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.PositionRisk;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Test1 {

    public static void main(String[] args) throws InterruptedException {
        //获取爆仓价
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY1, PrivateConfig.SECRET_KEY1,
                options);
        List<BigDecimal> bigDecimals = new ArrayList<>();
        bigDecimals.add(new BigDecimal(0));
        bigDecimals.add(new BigDecimal(0));
        while (true) {
            try {
                bigDecimals = placeOrder(syncRequestClient, "BNXUSDT", new BigDecimal("0.3"), 1, new BigDecimal("1.03"), bigDecimals.get(0), bigDecimals.get(1));
            } catch (Exception e) {
                log.info("底层下单异常");
                Thread.sleep(1000);
            }
        }
    }

    /**
     * 下单接口
     *
     * @param symbol 交易对
     * @param minNum 最小数量
     * @param scale  精确度
     * @param bao    回撤控制，如1.03，即高于爆仓价的70%，开始减仓
     */
    private static List<BigDecimal> placeOrder(SyncRequestClient syncRequestClient, String symbol, BigDecimal minNum, Integer scale, BigDecimal bao, BigDecimal yin, BigDecimal amt) throws InterruptedException {
        BigDecimal xia = new BigDecimal(0);
        BigDecimal fomo = new BigDecimal(0.5);
        //差额
        BigDecimal chajia = new BigDecimal(0);
        int i = 0;
        PositionRisk positionRisk = new PositionRisk();
        try {
            List<PositionRisk> risks = syncRequestClient.getUserDataRisk(symbol);
            positionRisk = risks.get(1);
            if (!positionRisk.getPositionAmt().equals(amt)) {
                yin = positionRisk.getUnrealizedProfit();
                amt=positionRisk.getPositionAmt();
            }
            xia = positionRisk.getMarkPrice().multiply(minNum).divide(positionRisk.getLeverage()).setScale(2, BigDecimal.ROUND_UP);
            chajia = positionRisk.getUnrealizedProfit().subtract(yin);
        } catch (Exception e) {
            log.error("获取仓位异常{}", e.getMessage());
        }
//            if (positionRisk.getMarkPrice()!=null&&positionRisk.getMarkPrice().compareTo(new BigDecimal(112))<0){
//                break;
//            }
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setWithdrawAvailable(new BigDecimal(0));
        try {
            List<AccountBalance> accountBalances = syncRequestClient.userBalance();
            accountBalance = accountBalances.stream().filter(e -> e.getAsset().equals("USDT")).collect(Collectors.toList()).get(0);
        } catch (Exception e) {
            log.error("获取余额异常{}", e.getMessage());
        }
        System.out.println(accountBalance);
        System.out.println(positionRisk);
        if (chajia.compareTo(xia) > 0) {
            //下单
            try {
                BigDecimal num = chajia.multiply(positionRisk.getLeverage())
                        .divide(positionRisk.getMarkPrice(), scale, BigDecimal.ROUND_DOWN);
                //分批下单
                if (num.compareTo(minNum.multiply(new BigDecimal(3))) > 0) {
                    for (int j = 0; j < 3; j++) {
                        Order order = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,
                                num.divide(new BigDecimal(3), scale, BigDecimal.ROUND_DOWN).toString(), null,
                                null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                        System.out.println(order);
                        Thread.sleep(200);
                    }
                } else {
                    //直接下单
                    Order order = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,
                            minNum.toString(), null, null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                }
            } catch (Exception e) {
                log.error("下单异常{}", e.getMessage());
            }
            log.info("此时{}：价格为：{}开仓价格为:{},开仓数量为：{},盈利为：{}，强平价为：{},账户余额为：{}", positionRisk.getSymbol(), positionRisk.getMarkPrice(), positionRisk.getEntryPrice(), positionRisk.getPositionAmt()
                    , yin, positionRisk.getLiquidationPrice(), accountBalance.getBalance());
        }
        //风险控制  当强平价小于当前价的110%,平仓15%操作,理论上永不爆仓
        if (Objects.nonNull(positionRisk.getLiquidationPrice()) && positionRisk.getMarkPrice() != null) {
            try {
                if (positionRisk.getLiquidationPrice().compareTo(positionRisk.getMarkPrice().multiply(bao)) < 0) {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.SHORT, OrderType.MARKET, null,
                            positionRisk.getPositionAmt().multiply(new BigDecimal(-0.1)).setScale(1, BigDecimal.ROUND_UP).toString(), null,
                            null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    log.info("此时进行减仓,共减仓{}次", i++);
                }
            } catch (Exception e) {
                log.error("减仓异常{}", e.getMessage());
            }
        }
        Thread.sleep(100);
        List<BigDecimal> bigDecimals=new ArrayList<>();
        bigDecimals.add(yin);
        bigDecimals.add(amt);
        return bigDecimals;
    }
}
