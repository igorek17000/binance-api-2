package com.binance.client.controller;

import com.binance.client.bo.SysUserBo;
import com.binance.client.service.UserServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
public class SysUserController {
    @Resource
    private UserServiceImpl userService;

    @PostMapping("/add")
    public Integer addUser(@RequestBody SysUserBo sysUserBo){
        Integer id=userService.addUser(sysUserBo);
        return id;
    }

    @PostMapping("/update/state")
    //状态 （是否停用 0=否 1=是）
    public Boolean updateState(String username,Integer state){
        return userService.updateState(username,state);
    }
}
