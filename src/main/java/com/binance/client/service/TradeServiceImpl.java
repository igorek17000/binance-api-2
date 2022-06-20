package com.binance.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.bo.StrategyBo;
import com.binance.client.bo.TrendBo;
import com.binance.client.constant.PrivateConfig;
import com.binance.client.mapper.StrategyMapper;
import com.binance.client.mapper.TradeRecordMapper;
import com.binance.client.model.Strategy;
import com.binance.client.model.SysUser;
import com.binance.client.model.TradeRecord;
import com.binance.client.model.enums.*;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.trade.AccountBalance;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.PositionRisk;
import com.binance.client.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TradeServiceImpl {

    @Resource
    private TradeRecordMapper tradeRecordMapper;

    @Resource
    private StrategyMapper strategyMapper;

    @Resource
    private UserServiceImpl userService;


    public Integer addStrategy(StrategyBo strategyBo) {
        Strategy strategy = new Strategy();
        BeanUtils.copyProperties(strategyBo, strategy);
        strategy.setCreateTime(new Date());
        strategyMapper.insert(strategy);
        return strategy.getId();
    }

    public Boolean start(Integer id) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY1, PrivateConfig.SECRET_KEY1,
                options);
        while (true) {
            try {
                Strategy strategy = strategyMapper.selectById(id);
                if (strategy.getState().equals(0)) {
                    break;
                }
                Boolean b = placeOrder(syncRequestClient, strategy);
                if (b = false) {
                    break;
                }
            } catch (Exception e) {
                log.info("底层下单异常");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }

    public Boolean updateState(Integer id, Integer state) {

        Strategy strategy = strategyMapper.selectById(id);
        strategy.setId(id);
        strategy.setState(state);
        return strategyMapper.updateById(strategy) > 0;
    }

    /**
     * 下单滚仓做空接口
     */
    //     * @param symbol 交易对
    //     * @param minNum 最小数量
    //     * @param scale  精确度
    //     * @param bao    回撤控制，如1.03，即高于爆仓价的70%，开始减仓
    private Boolean placeOrder(SyncRequestClient syncRequestClient, Strategy strategy) throws InterruptedException {
        BigDecimal xia = new BigDecimal(0);
        //盈利
        BigDecimal yin = new BigDecimal(0);
        //差额
        BigDecimal chajia = new BigDecimal(0);
        int i = 0;
        PositionRisk positionRisk = new PositionRisk();
        try {
            List<PositionRisk> risks = syncRequestClient.getUserDataRisk(strategy.getSymbol());
            positionRisk = risks.get(1);
            xia = positionRisk.getMarkPrice().multiply(strategy.getMinNum()).divide(positionRisk.getLeverage()).setScale(2, BigDecimal.ROUND_UP);
            if (yin.compareTo(new BigDecimal(0)) > 0) {
                chajia = positionRisk.getEntryPrice().subtract(positionRisk.getMarkPrice()).multiply(positionRisk.getPositionAmt()).subtract(yin);
            }
        } catch (Exception e) {
            log.error("获取仓位异常{}", e.getMessage());
        }
        if (positionRisk.getMarkPrice() != null && positionRisk.getMarkPrice().compareTo(strategy.getMinPrice()) < 0) {
            return false;
        }
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
        if (yin.compareTo(new BigDecimal(0)) <= 0) {
            chajia = accountBalance.getWithdrawAvailable();
        }
        if (chajia.compareTo(xia) > 0) {
            //下单
            try {
                BigDecimal num = chajia.multiply(positionRisk.getLeverage())
                        .divide(positionRisk.getMarkPrice(), strategy.getMinScale(), BigDecimal.ROUND_DOWN);
                //分批下单
                if (num.compareTo(strategy.getMinNum().multiply(new BigDecimal(3))) > 0) {
                    for (int j = 0; j < 3; j++) {
                        Order order = syncRequestClient.postOrder(strategy.getSymbol(), OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,
                                num.divide(new BigDecimal(3), strategy.getMinScale(), BigDecimal.ROUND_DOWN).toString(), null,
                                null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                        System.out.println(order);
                        Thread.sleep(200);
                    }
                } else {
                    //直接下单
                    Order order = syncRequestClient.postOrder(strategy.getSymbol(), OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,
                            strategy.getMinNum().toString(), null, null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                }
                yin = positionRisk.getEntryPrice().subtract(positionRisk.getMarkPrice()).multiply(new BigDecimal("-1").multiply(positionRisk.getPositionAmt()));
            } catch (Exception e) {
                log.error("下单异常{}", e.getMessage());
            }
            log.info("此时{}：价格为：{}开仓价格为:{},开仓数量为：{},盈利为：{}，强平价为：{},账户余额为：{}", positionRisk.getSymbol(), positionRisk.getMarkPrice(), positionRisk.getEntryPrice(), positionRisk.getPositionAmt()
                    , yin, positionRisk.getLiquidationPrice(), accountBalance.getBalance());
        }
        //风险控制  当强平价小于当前价的110%,平仓15%操作,理论上永不爆仓
        if (Objects.nonNull(positionRisk.getLiquidationPrice()) && positionRisk.getMarkPrice() != null) {
            try {
                if (positionRisk.getLiquidationPrice().compareTo(positionRisk.getMarkPrice().multiply(strategy.getReturnPercent())) < 0) {
                    syncRequestClient.postOrder(strategy.getSymbol(), OrderSide.BUY, PositionSide.SHORT, OrderType.MARKET, null,
                            positionRisk.getPositionAmt().multiply(new BigDecimal(-0.1)).setScale(1, BigDecimal.ROUND_UP).toString(), null,
                            null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    log.info("此时进行减仓,共减仓{}次", i++);
                }
            } catch (Exception e) {
                log.error("减仓异常{}", e.getMessage());
            }
        }
        Thread.sleep(100);
        return true;
    }


    public Integer addPriceRecord() {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
        String[] symbols = new String[]{"BTCUSDT"};
        List<String> times = new ArrayList() {
            {
                add("4h");
            }
        };
        Integer sum = 0;
        for (long init = 1640966400000l; init < 1654606800000l; ) {
            long end = init + 1000 * 60 * 60 * 5 * 4;
            for (String s : symbols) {
                for (String time : times) {
                    List<Candlestick> candlesticks = syncRequestClient.getCandlestick(s, CandlestickInterval.getByCode(time), init, end, 5);
                    for (Candlestick candlestick : candlesticks) {
                        TradeRecord tradeRecord = new TradeRecord();
                        tradeRecord.setPair(s)
                                .setStartTime(new Date(candlestick.getOpenTime()))
                                .setEndTime(new Date(candlestick.getCloseTime()))
                                .setTime(time)
                                .setMinPrice(candlestick.getLow())
                                .setMaxPrice(candlestick.getHigh())
                                .setOpen(candlestick.getOpen())
                                .setClose(candlestick.getClose())
                                .setQuoteAssetVolume(candlestick.getQuoteAssetVolume());
                        tradeRecordMapper.insert(tradeRecord);
                    }
                }
            }
            init = end + 1;
        }
        return 1;
    }

    //    public void orderRecord() {
//        List<String> canTimes = new ArrayList() {
//            {
//                add("1h");
//            }
//        };
//        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
//        wrapper.in(TradeRecord::getTime, canTimes);
//        wrapper.orderByAsc(TradeRecord::getStartTime);
//        List<TradeRecord> tradeRecords = tradeRecordMapper.selectList(wrapper);
//        List<Double> prices = new ArrayList<>();
//        Double init21 = 0d;
//        Double init55 = 0d;
//        Double yin = 0d;
//        //开仓价
//        BigDecimal price = new BigDecimal(0);
//        Date startTime = null;
//        Date endTime = null;
//        List<TrendBo> trendBos = new ArrayList<>();
//        for (TradeRecord tradeRecord : tradeRecords) {
//            prices.add(tradeRecord.getClose().doubleValue());
//            if (prices.size() > 50) {
//                Double ema21 = getEXPMA(prices, 21);
//                Double ema55 = getEXPMA(prices, 55);
////                Double ema120 = getEXPMA(prices, 120);
//                TrendBo trendBo = new TrendBo();
//                if (price.compareTo(new BigDecimal(0)) == 0) {
//                    price = tradeRecord.getClose();
//                    startTime = tradeRecord.getStartTime();
//                }
//                if (init21 > init55 && ema21 < ema55) {
//                    //平多做空
//                    trendBo.setStartPrice(price);
//                    yin = yin + tradeRecord.getClose().doubleValue() - price.doubleValue();
//                    log.info("{}时ema21为{},ema55为{}，开仓价为{},平仓价为{},盈利为{}", tradeRecord.getStartTime(), ema21, ema55, price, tradeRecord.getClose().doubleValue(), yin);
//                    price = tradeRecord.getClose();
//                    endTime = tradeRecord.getStartTime();
//                    trendBo.setEndPrice(price);
//                    trendBo.setStartTime(startTime);
//                    trendBo.setEndTime(endTime);
//                    trendBo.setFlag(1);
//                    trendBos.add(trendBo);
//                    startTime = endTime;
//                }
//                if (init21 < init55 && ema21 > ema55) {
//                    trendBo.setStartPrice(price);
//                    yin = yin + price.doubleValue() - tradeRecord.getClose().doubleValue();
//                    log.info("{}时ema21为{},ema55为{}，开仓价为{},平仓价为{},盈利为{}", tradeRecord.getStartTime(), ema21, ema55, price, tradeRecord.getClose().doubleValue(), yin);
//                    price = tradeRecord.getClose();
//                    trendBo.setEndPrice(price);
//                    endTime = tradeRecord.getStartTime();
//                    trendBo.setStartTime(startTime);
//                    trendBo.setEndTime(endTime);
//                    trendBo.setFlag(-1);
//                    trendBos.add(trendBo);
//                    startTime = endTime;
//                }
//                init21 = ema21;
//                init55 = ema55;
//            }
//        }
//        for (TrendBo trendBo : trendBos) {
//            System.out.println(trendBo.toString());
//        }
////        for (TrendBo trendBo : trendBos) {
////            count("5m", trendBo.getStartTime(), trendBo.getEndTime(), trendBo.getFlag());
////        }
//    }
    public void orderRecord() {
        String start = "2022-02-01";
        String end = "2022-06-01";
        StringBuilder sb = new StringBuilder();
        while (true) {
            String endDate = DateUtil.getBeforeDays(start, -15);
            if (DateUtil.formatString(endDate).compareTo(DateUtil.formatString(end)) > 0) {
                break;
            }
            sb.append(testOrder(start, endDate) + "\n");
            start = endDate;
        }
        System.out.println(sb);
    }

    private String testOrder(String start, String end) {
        List<String> canTimes = new ArrayList() {
            {
                add("5m");
            }
        };
        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TradeRecord::getTime, canTimes);
        wrapper.ge(TradeRecord::getStartTime, start);
        wrapper.le(TradeRecord::getStartTime, end);
        wrapper.orderByAsc(TradeRecord::getStartTime);
        List<TradeRecord> tradeRecords = tradeRecordMapper.selectList(wrapper);
        List<Double> prices = new ArrayList<>();
        Double init21 = 0d;
        Double init55 = 0d;
        Double init120 = 0d;
        Double yin = 0d;
        //开仓价
        BigDecimal price = new BigDecimal(0);
        Date startTime = null;
        Date endTime = null;
        List<TrendBo> trendBos = new ArrayList<>();
        int yinli = 0;
        int kuisun = 0;
        for (TradeRecord tradeRecord : tradeRecords) {
            prices.add(tradeRecord.getClose().doubleValue());
            if (prices.size() > 200) {
                Double ema21 = getEXPMA(prices, 21);
                Double ema55 = getEXPMA(prices, 55);
                Double ema120 = getEXPMA(prices, 120);
                TrendBo trendBo = new TrendBo();
                if (price.compareTo(new BigDecimal(0)) == 0) {
                    price = tradeRecord.getClose();
                    startTime = tradeRecord.getStartTime();
                }
                if (init55 > init120 && ema21 < ema55 && ema55 < ema120) {
                    //平多做空
                    trendBo.setStartPrice(price);
                    yin = yin + tradeRecord.getClose().doubleValue() - price.doubleValue();
                    log.info("{}时ema21为{},ema55为{}，开仓价为{},平仓价为{},盈利为{}", tradeRecord.getStartTime(), ema21, ema55, price, tradeRecord.getClose().doubleValue(), yin);
                    if (tradeRecord.getClose().doubleValue() - price.doubleValue() > 0) {
                        yinli += 1;
                    } else {
                        kuisun += 1;
                    }
                    price = tradeRecord.getClose();
                    endTime = tradeRecord.getStartTime();
                    trendBo.setEndPrice(price);
                    trendBo.setStartTime(startTime);
                    trendBo.setEndTime(endTime);
                    trendBo.setFlag(1);
                    trendBo.setDifPrice(trendBo.getEndPrice().subtract(trendBo.getStartPrice()));
                    trendBos.add(trendBo);
                    startTime = endTime;
                }
                if (init55 < init120 && ema21 > ema55 && ema55 > ema120) {
                    trendBo.setStartPrice(price);
                    yin = yin + price.doubleValue() - tradeRecord.getClose().doubleValue();
                    log.info("{}时ema21为{},ema55为{}，开仓价为{},平仓价为{},盈利为{}", tradeRecord.getStartTime(), ema21, ema55, price, tradeRecord.getClose().doubleValue(), yin);
                    if (tradeRecord.getClose().doubleValue() - price.doubleValue() > 0) {
                        kuisun += 1;
                    } else {
                        yinli += 1;
                    }
                    price = tradeRecord.getClose();
                    trendBo.setEndPrice(price);
                    endTime = tradeRecord.getStartTime();
                    trendBo.setStartTime(startTime);
                    trendBo.setEndTime(endTime);
                    trendBo.setFlag(-1);
                    trendBo.setDifPrice(trendBo.getStartPrice().subtract(trendBo.getEndPrice()));
                    trendBos.add(trendBo);
                    startTime = endTime;
                }
                init120 = ema120;
                init55 = ema55;
            }
        }
        BigDecimal yin1 = new BigDecimal(0);
        BigDecimal kui1 = new BigDecimal(0);
        yinli = 0;
        for (TrendBo trendBo : trendBos) {
            if (trendBo.getDifPrice().compareTo(new BigDecimal(0)) > 0) {
                yinli += 1;
                yin1 = yin1.add(trendBo.getDifPrice());
            } else {
                kui1 = kui1.add(trendBo.getDifPrice());
            }
            System.out.println(trendBo.toString());
        }
        String s = MessageFormat.format("在{0}-{1}期间总共开单{2}次，盈利次数{3}，盈利金额{4}，亏损金额{5},纯利{6}", new Object[]{start, end, trendBos.size(), yinli, yin1, kui1, yin1.add(kui1)});
        return s;
//        for (TrendBo trendBo : trendBos) {
//            count("5m", trendBo.getStartTime(), trendBo.getEndTime(), trendBo.getFlag());
//        }
    }

    public void count(String time, Date startTime, Date endTime, Integer flag) {
        List<String> canTimes = new ArrayList() {
            {
                add(time);
            }
        };
        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TradeRecord::getTime, canTimes);
        wrapper.orderByAsc(TradeRecord::getStartTime);
        List<TradeRecord> tradeRecords = tradeRecordMapper.selectList(wrapper);
        List<Double> prices = new ArrayList<>();
        Double init21 = 0d;
        Double init55 = 0d;
        Double yin = 0d;
        //开仓价
        BigDecimal price = new BigDecimal(0);
        for (TradeRecord tradeRecord : tradeRecords) {
            prices.add(tradeRecord.getClose().doubleValue());
            if (prices.size() > 50) {
                Double ema21 = getEXPMA(prices, 21);
                Double ema55 = getEXPMA(prices, 55);
//                Double ema120 = getEXPMA(prices, 120);
                if (price.compareTo(new BigDecimal(0)) == 0) {
                    price = tradeRecord.getClose();
                }
                if (tradeRecord.getStartTime().compareTo(startTime) > 0 &&
                        tradeRecord.getEndTime().compareTo(endTime) < 0) {
                    if (flag < 0 && init21 > init55 && ema21 < ema55) {
                        log.info("上一单为多单,开仓价格为{},平仓价格为{}", price, tradeRecord.getClose());
                        log.info("{}开始做空，开仓价格为{}", tradeRecord.getStartTime(), tradeRecord.getClose());
                        yin = yin + tradeRecord.getClose().doubleValue() - price.doubleValue();
                        price = tradeRecord.getClose();
                    }
                    if (flag > 0 && init21 < init55 && ema21 > ema55) {
                        log.info("上一单为空单,开仓价格为{},平仓价格为{}", price, tradeRecord.getClose());
                        log.info("{}开始做多，开仓价格为{}", tradeRecord.getStartTime(), tradeRecord.getClose());
                        yin = yin + price.doubleValue() - tradeRecord.getClose().doubleValue();
                        price = tradeRecord.getClose();
                    }
                }
                init21 = ema21;
                init55 = ema55;
            }
        }
    }

    public static final Double getEXPMA(final List<Double> list, final int number) {
        // 开始计算EMA值，
        Double k = 2.0 / (number + 1.0);// 计算出序数
        Double ema = list.get(0);// 第一天ema等于当天收盘价
        for (int i = 1; i < list.size(); i++) {
            // 第二天以后，当天收盘 收盘价乘以系数再加上昨天EMA乘以系数-1
            ema = list.get(i) * k + ema * (1 - k);
        }
        return ema;
    }

    public Boolean emaStart(String username, String num) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY1, PrivateConfig.SECRET_KEY1,
                options);
        Double init21 = 0d;
        Double yin = 0d;
        //开仓价
        BigDecimal price = new BigDecimal(0);
        Date startTime = null;
        Date endTime = null;
        List<TrendBo> trendBos = new ArrayList<>();
        Double init55 = 0d;
        Double init120 = 0d;
        while (true) {
            SysUser sysUser = userService.selectByUserName(username);
            if (Objects.nonNull(sysUser) && sysUser.getState().equals(1)) {
                break;
            }
            List<Double> prices = new ArrayList<>();
            List<Candlestick> candlesticks = new ArrayList<>();
            try {
                candlesticks = syncRequestClient.getCandlestick("BTCUSDT", CandlestickInterval.getByCode("5m"), null, null, 241);
            } catch (Exception e) {
                log.error("获取k线数据异常{}", e);
            }
            for (Candlestick candlestick : candlesticks) {
                prices.add(candlestick.getClose().doubleValue());
                if (prices.size() > 240) {
                    Double ema21 = getEXPMA(prices, 21);
                    Double ema55 = getEXPMA(prices, 55);
                    Double ema120 = getEXPMA(prices, 120);
                    log.info("{}时:init55为:{}--init120为:{}ema21为:{}--ema55为:{}--ema120为:{}", new Date(candlestick.getCloseTime()), init55, init120, ema21, ema55, ema120);
                    if (init55 > init120 && ema21 < ema55 && ema55 < ema120) {
                        //平多做空
                        try {
                            Order order2 = syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,
                                    num, null,
                                    null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                        } catch (Exception e) {
                            log.error("做空异常{}", e);
                        }
                        try {
                            Order order1 = syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.LONG, OrderType.MARKET, null,
                                    num, null,
                                    null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                        } catch (Exception e) {
                            log.error("平多异常{}", e);
                        }
                        log.info("{}平多做空，开仓价{}", new Date(candlestick.getCloseTime()), candlestick.getClose());

                    }
                    if (init55 < init120 && ema21 > ema55 && ema55 > ema120) {
                        try {
                            Order order1 = syncRequestClient.postOrder("BTCUSDT", OrderSide.BUY, PositionSide.LONG, OrderType.MARKET, null,
                                    num, null,
                                    null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                        } catch (Exception e) {
                            log.error("做多异常{}", e);
                        }
                        try {
                            Order order2 = syncRequestClient.postOrder("BTCUSDT", OrderSide.BUY, PositionSide.SHORT, OrderType.MARKET, null,
                                    num, null,
                                    null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                        } catch (Exception e) {
                            log.error("平空异常{}", e);
                        }
                        log.info("{}平空做多，开仓价{}", new Date(candlestick.getCloseTime()), candlestick.getClose());
                    }
                    init120 = ema120;
                    init55 = ema55;
                }
            }
            try {
                Thread.sleep(1000 * 298);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
