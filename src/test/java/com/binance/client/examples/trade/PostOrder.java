package com.binance.client.examples.trade;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PostOrder {
    //    public static void main(String[] args) {
//        RequestOptions options = new RequestOptions();
//        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
//                options);
////        System.out.println(syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
////                "1", "1", null, null, null, null));
//
//        // place dual position side order.
//        // Switch between dual or both position side, call: com.binance.client.examples.trade.ChangePositionSide
//        System.out.println(syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.SHORT, OrderType.LIMIT, TimeInForce.GTC,
//                "1", "9000", null, null, null, null, NewOrderRespType.RESULT));//   }
//    public static void main(String[] args) {
//        //初始价格
//        double init = 1.696;
//        //初始数量
//        double num = 600;
//        //全仓杠杆倍数
//        double fomo = 2;
//        //初始保证金
//        double count = 500;
//        double total = count;
//        //下跌差价
//        double xia = 0.005;
//        while (init > 1.5) {
//            init = init - xia;
//            total = total + num * xia;
//            double bao = init + total / num;
//            if ((total - count) / init > 4) {
//                //加仓
//                num = num + (total - count) / init;
//                count = num * init / fomo;
//            }
//            log.info("此时价格为:{},开仓数量为{},仓位为{}，强平价为{}", init, num
//                    , total, bao);
//        }
//    }

    public static void main(String[] args) {
        //初始价格
        double init = 124.6;
        //初始数量
        double num = 41;
        //全仓杠杆倍数
        double fomo = 10;
        //初始保证金
        double count = 800;
        double total = count;
        //下跌差价
        double xia = 0.1;
        while (init > 70) {
            init = init - xia;
            total = total + num * xia;
            double bao = init + total / num;
            if ((total - count) / init > 0.2) {
                //加仓
                num = num + (total - count) / init;
                count = num * init / fomo;
            }
            log.info("此时价格为:{},开仓数量为{},仓位为{}，强平价为{}", init, num
                    , total, bao);
        }
    }
}