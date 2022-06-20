package com.binance.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.binance.client.bo.SysUserBo;
import com.binance.client.mapper.SysUserMapper;
import com.binance.client.model.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl {

    @Resource
    private SysUserMapper sysUserMapper;

    public Integer addUser(SysUserBo sysUserBo) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserBo, sysUser);
        sysUserMapper.insert(sysUser);
        return sysUser.getId();
    }

    public Boolean updateState(String username, Integer state) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(username), SysUser::getUsername, username);
        List<SysUser> sysUsers = sysUserMapper.selectList(wrapper);
        sysUsers.forEach(s -> {
            s.setState(state);
            sysUserMapper.updateById(s);
        });
        return true;
    }
}
