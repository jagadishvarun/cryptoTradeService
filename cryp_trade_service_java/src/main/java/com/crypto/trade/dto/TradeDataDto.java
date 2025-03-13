package com.crypto.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TradeDataDto {

	private String customerId;
	private String tradeId;
	private String tradePairName;
	private String quotePrice;
	private String quantity;
	private String totalPrice;
	private String buyerId;
	private String sellerId;
	private String orderType;
	private String tradeType;
	private String tradeFee;
	private String tradeStatus;
	private String tradeDirection;
	private String tradeSize;

}
