package com.crypto.trade.dto;


import lombok.Data;

@Data
public class SocketNotifyDto {
	private String customerId;
	private String alertMessage;
	private String alertHeader;
	private String alertTitle;
	private String phoneNumber;
	private String countryCode;

}
