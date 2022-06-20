package com.binance.client.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 趋势
 */
@Data
@ToString
public class TrendBo implements Serializable {

    private BigDecimal startPrice;

    private BigDecimal endPrice;

    private Date startTime;

    private Date endTime;

    /**
     * 方向标记
     */
    private Integer flag;

    private BigDecimal difPrice;
}
