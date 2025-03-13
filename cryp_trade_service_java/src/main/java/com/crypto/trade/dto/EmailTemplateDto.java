package com.crypto.trade.dto;

import java.util.Date;

import lombok.Data;

@Data
public class EmailTemplateDto {

	private String mailTo;
	private String email;
	private String userName;
	private String depositAmount;
	private String amount;
	private String fundTransferHeader;
	private String fundTransferBody;
	private String fundTransferBodyMethod;
	private String customerId;
	private String loginUserUrl;
	private String type;
	private String subject;
	private String dateTime;
	private String totalAmount;
	private String baseAsset;
	private String quoteAsset;
	private double quantity;
	private double price;
	private double sourceAssetbalance;
	private double targetAssetbalance;
	private String wallet;
	private String unitCount;
	private String purchaseAmount;
	private String baseAssetBalance;
	private String quoteAssetBalance;
	private String orderStatus;
	private String orderId;
}
