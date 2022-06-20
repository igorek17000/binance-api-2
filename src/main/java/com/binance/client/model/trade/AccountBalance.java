package com.binance.client.model.trade;

import com.binance.client.constant.BinanceApiConstants;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

@Data
public class AccountBalance {

    private String asset;

    private BigDecimal balance;

    private BigDecimal withdrawAvailable;

    private BigDecimal availableBalance;

    private BigDecimal crossWalletBalance;
}
