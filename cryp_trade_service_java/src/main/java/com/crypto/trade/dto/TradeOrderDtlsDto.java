package com.crypto.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TradeOrderDtlsDto {
	
	private String customerId;
	private int pageNo;
	private int pageSize;
	
	private String orderId;
	private String baseAsset;
	private String quoteAsset;
	private String ordAssetPairName;
	private double requestOrdQty;
	private String ordSide;
	private String ordType;
	private double ordPrice;
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
    private String searchAsset;
	private String createdDate;
	private String updatedDate;
	private int ordDetId;
	
	private String assetType;
	private String transactionId;
	private String type;
	private String daysOrYear;
	private String asset;
	private String status;
	private int rowCount;
	private double ordFilledQuantity;
	private double ordLimitPrice;
	private double ordStopPrice;
	private double ordFilledAvgPrice;
	private double ordLimit;
	private String ordCreatedAt;
	private String ordUpdatedAt;
	private double requestedOrdPrice;
	private double ordRemainingQty;
	private double actualOrdValue;
	private String fromDate;
	private String toDate;
	private String tradeStatus;
	private String assetPair;
	private String assetPairName;
	private String ordCreatedDate;
	private double total;
}
