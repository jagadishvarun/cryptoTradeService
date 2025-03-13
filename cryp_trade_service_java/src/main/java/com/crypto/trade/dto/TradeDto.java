package com.crypto.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TradeDto {

	private String customerId;
	private String orderId;
	private String baseAsset;
	private String quoteAsset;
	private String ordAssetPairName;
	private String requestOrdQty;
	private String ordSide;
	private String ordType;
	private String ordPrice;
	private String ordTimeInforce;
	private String ordStatus;
	private String ordName;
	private String flagValue;
	private int ordAssetSymbol;
	private String excOrderId;
    private int assetId;
    private String assetName;
    private String tradeId;
    private int trdAssetSymbol;
    private double trdPrice;
    private double trdQty;
    private int statusCode;
    private double remainingQty;
    private double reqQty;
    private double reqPrice;
    private String tradeSide;
    private double tradeQty;
    private double tradePrice;
    private String asset;
    private String searchAsset;
    private int wallAssetId; 
    private String ordLimitPrice;
    private String ordStopPrice;
    private int assetPairId;
    private int assetWatchStatus;
    private String assetCode;
    private int symbol;
}
