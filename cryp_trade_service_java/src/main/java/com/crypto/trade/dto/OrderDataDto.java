package com.crypto.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OrderDataDto {
	
	private String customerId;
	private String orderId;
	private String baseAsset;
	private String quoteAsset;
	private String ordAssetPairName;
	private String requestOrdQty;
	private String ordSide;
	private String ordType;
	private String ordTimeInForce;
	private String ordFilledQuantity;
	private String ordAssetClass;
	private String actualOrdValue;
	private String ordLimitPrice;
	private String ordStopPrice;
	private String ordFilledAvgPrice;
	private String ordStatus;
	private String ordLimit;
	private String createdAt;
	private String updatedAt;
	private String ordPrice;
	private String replaceOrderId;
	private String actualOrdQty;
	private String feeAmount;
	private String stopPrice;
	private String trailPrice;
	private String trailPercent;
	private String assetType;

}
