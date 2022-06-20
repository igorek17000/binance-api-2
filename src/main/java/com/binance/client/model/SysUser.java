package com.binance.client.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUser implements Serializable {

    private Integer id;

    private String username;

    private String apiKey;

    private String apiSecret;

    private Integer state;
}
