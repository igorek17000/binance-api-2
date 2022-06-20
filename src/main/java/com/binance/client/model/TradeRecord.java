package com.binance.client.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
public class TradeRecord implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String time;

    private String pair;

    /**
     * 开盘价
     */
    private BigDecimal open;
    /**
     * 收盘价
     */
    private BigDecimal close;

    private Date startTime;

    private Date endTime;

    /**
     * 成交额
     */
    private BigDecimal quoteAssetVolume;
}
