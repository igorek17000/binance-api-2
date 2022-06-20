package com.binance.client.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserBo implements Serializable {

    private Integer id;

    private String username;

    private String apiKey;

    private String apiSecret;

    //状态 （是否停用 0=否 1=是）
    private Integer state;
}
