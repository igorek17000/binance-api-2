package com.binance.client.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.binance.client.model.Strategy;
import com.binance.client.model.TradeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 城市 Mapper 接口
 * </p>
 *
 * @author nanxing
 * @since 2020-01-14
 */
@Mapper
public interface StrategyMapper extends BaseMapper<Strategy> {

}
