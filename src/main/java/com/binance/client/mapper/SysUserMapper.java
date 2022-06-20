package com.binance.client.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.binance.client.model.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户 Mapper 接口
 * </p>
 *
 * @author nanxing
 * @since 2020-01-14
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
