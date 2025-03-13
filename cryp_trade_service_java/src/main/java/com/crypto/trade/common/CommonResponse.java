package com.crypto.trade.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse <T> {
	
	private int status;
	private String message;
	private T data;
	
	public CommonResponse(ResponseStatusEnum status, T data) {
		this.status = status.getCode();
		this.message = status.getMessage();
		this.data = data;
	}
}
