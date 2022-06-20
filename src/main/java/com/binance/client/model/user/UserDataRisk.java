package com.binance.client.model.user;

import lombok.Data;

@Data
public class UserDataRisk {
    private String entryPrice;// 开仓均价
    private String marginType;    // 逐仓模式或全仓模式
    private String isAutoAddMargin;
    private String isolatedMargin;   // 逐仓保证金
    private String leverage;   // 当前杠杆倍数
    private String liquidationPrice;  // 参考强平价格
    private String markPrice;   // 当前标记价格
    private String maxNotionalValue;  // 当前杠杆倍数允许的名义价值上限
    private String positionAmt;   // 头寸数量，符号代表多空方向, 正数为多，负数为空
    private String symbol;   // 交易对
    private String unRealizedProfit;   // 持仓未实现盈亏
    private String positionSide;  // 持仓方向
}
