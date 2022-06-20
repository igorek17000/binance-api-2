package com.binance.client.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Strategy implements Serializable {

    /**
     *   `id` int NOT NULL AUTO_INCREMENT,
     *   `symbol` varchar(20) DEFAULT '' COMMENT '交易对',
     *   `min_num` decimal(5,4) DEFAULT '0.0000' COMMENT '最小交易数量',
     *   `min_scale` int DEFAULT NULL COMMENT '精度',
     *   `return_percent` decimal(5,4) DEFAULT NULL COMMENT '回撤百分比',
     *   `min_price` decimal(5,4) DEFAULT NULL COMMENT '最小价格',
     *   `max_price` decimal(5,4) DEFAULT NULL COMMENT '最大价格',
     *   `state` tinyint DEFAULT '1' COMMENT '状态(0=不正常 1=正常)',
     *   `create_time` datetime DEFAULT NULL,
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String symbol;

    private BigDecimal minNum;

    private Integer minScale;

    private BigDecimal returnPercent;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer state;

    private Date createTime;
}
