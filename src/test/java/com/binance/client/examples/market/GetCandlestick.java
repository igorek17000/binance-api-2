package com.binance.client.examples.market;

import com.binance.client.model.enums.CandlestickInterval;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;

import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetCandlestick {
    public static void main(String[] args) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
        String symbol = "NEARUSDT";
        String[] symbols = new String[]{"BTCUSDT"};
        List<String> times = new ArrayList() {
            {
                add("3m");
                add("5m");
                add("15m");
                add("1h");
                add("4h");
            }
        };
        List<String> canTimes = new ArrayList() {
            {
                add("5m");
                add("15m");
                add("1h");
                add("4h");
            }
        };
        Integer value = 0;
        List<String> emaTimes = new ArrayList<>();
        for (String s : symbols) {
            for (String time : times) {
                List<Candlestick> candlesticks = syncRequestClient.getCandlestick(s, CandlestickInterval.getByCode(time), 1641440206000l, null, null);
                List<Double> prices = new ArrayList<>();
                for (Candlestick candlestick : candlesticks) {
                    prices.add(Double.parseDouble(candlestick.getClose().toString()));
                }
                Double ema21 = getEXPMA(prices, 21);
                System.out.println(time+"级别的ema21价格为:"+ema21);
            }
        }
    }

    public static String transDate(Long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date dt = new Date(time);
        String sDateTime = sdf.format(dt);
        return sDateTime;
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

}
