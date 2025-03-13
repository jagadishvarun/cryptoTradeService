package com.crypto.trade.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResponseStatusEnum {

	INVALIDPRICEERROR(400, "You have entered invalid price"), NODATA(400, "No Data"),
	INSUFFICIENTBALANCE(400, "Insufficient Balance"),
	EXCEPTIONERROR(500, "Something went wrong. Please try after sometime."),
	VALIDATIONERROR(400, "One of the required field is empty or contains invalid data, please check your input(s)!"),
	DBERROR(500, "Internal DB Error"), INVALID_JWT_TOKEN(400, " Invalid access Token !"), SUCCESS(200, "Successful");

	private int code;
	private String message;

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "Status [code=" + code + ", message=" + message + "]";
	}

}
