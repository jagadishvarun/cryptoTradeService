package com.crypto.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransLedgerDtlsDto {

	private String transactionId;
	private String customerId;
	private String tradeId;
	private String orderId;
	private double requestAmount;
	private String action;
	private double instantAmount;
	private String transactionStatus;
	private double feeAmount;
	private String feeAmountMethod;
	private String transactionMessage;
	private double actualAmount;
	private double openingBalance;
	private double closingBalance;
	private double currentBalance;
	private String transactionType;
	private String transactionTimestamp;
	private String updatedTimestamp;
	private String assetPair;
	private String baseAsset;
	private String quoteAsset;
	private String assetType;

}
