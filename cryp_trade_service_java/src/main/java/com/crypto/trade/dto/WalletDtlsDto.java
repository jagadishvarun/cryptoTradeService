package com.crypto.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WalletDtlsDto {

	private String walletDtlId;
	private String customerId;
	private String walletId;
	private String walletAddress;
	private String walletPubKey;
	private String walletPrivKey;
	private String statusId;
	private String createdDate;
	private String updatedDate;
}
