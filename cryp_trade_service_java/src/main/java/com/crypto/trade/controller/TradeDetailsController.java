package com.crypto.trade.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.crypto.trade.common.ApiURIConstants;
import com.crypto.trade.common.CommonResponse;
import com.crypto.trade.common.OpenApiDesc;
import com.crypto.trade.common.OpenApiRequestDesc;
import com.crypto.trade.common.ResponseStatusEnum;
import com.crypto.trade.configuration.ValidateTokenByUsername;
import com.crypto.trade.dto.TradeOrderDtlsDto;
import com.crypto.trade.service.TradeDtlsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RequestMapping("/trade")
@RestController
public class TradeDetailsController {

	@Autowired
	ValidateTokenByUsername isValidToken;

	@Autowired
	TradeDtlsService tradeDtlsService;

	@Operation(summary = "Get Customer All Order Details", description = OpenApiDesc.GET_CUSTOMER_ALL_ORDERS_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.GET_CUSTOMER_ALL_ORDERS_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.GET_CUSTOMER_ALL_ORDERS_DTLS)
	public ResponseEntity<Object> getCustomerAllOrders(@RequestBody TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {
		return tradeDtlsService.getCustomerAllOrders(tradeOrderDtlsDto, request);
	}

	@Operation(summary = "Get Customer All Trade Details", description = OpenApiDesc.GET_CUSTOMER_ALL_TRADE_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.GET_CUSTOMER_ALL_TRADE_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.GET_CUSTOMER_ALL_TRADE_DTLS)
	public ResponseEntity<Object> getCustomerAllTrades(@RequestBody TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {
		return tradeDtlsService.getCustomerAllTrades(tradeOrderDtlsDto, request);
	}

	@GetMapping(value = ApiURIConstants.GET_WALLET_DETAILS)
	public ResponseEntity<Object> getWalletDetails(@PathVariable String customerId, @PathVariable int assetId,
			HttpServletRequest request) {
		if (isValidToken.isValidToken(request, customerId)) {
			return tradeDtlsService.getWalletDetails(customerId, assetId);
		} else {
			return ResponseEntity.badRequest().body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
		}
	}

	@Operation(summary = "Get Customer Transaction Ledger Details", description = OpenApiDesc.GET_CUSTOMER_TRANS_LEDGER_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.GET_CUSTOMER_TRANS_LEDGER_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.GET_CUSTOMER_TRANS_LEDGER_DTLS)
	public ResponseEntity<Object> getCustomerTransLedger(@RequestBody TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {
		return tradeDtlsService.getCustomerTransLedger(tradeOrderDtlsDto, request);
	}

	@Operation(summary = "Get Open Orders", description = OpenApiDesc.GET_OPEN_ORDERS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.GET_OPEN_ORDERS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.GET_OPEN_ORDERS)
	public ResponseEntity<Object> getOpenOrders(@RequestBody TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {
		return tradeDtlsService.getOpenOrders(tradeOrderDtlsDto, request);
	}

	@GetMapping(value = { ApiURIConstants.GET_SPOT_WALLET_DETAILS, ApiURIConstants.GET_SPOT_WALLET_DETAILS_BY_TYPE })
	public ResponseEntity<Object> getSpotWalletDetails(@PathVariable String customerId,
			@PathVariable(required = false) Integer assetId, HttpServletRequest request) {
		if (isValidToken.isValidToken(request, customerId)) {
			return tradeDtlsService.getSpotWalletDetails(customerId, assetId);
		} else {
			return ResponseEntity.badRequest().body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
		}
	}

	@GetMapping(value = ApiURIConstants.GET_CUSTOMER_ORDER_DTLS)
	public ResponseEntity<Object> getCustomerOrderDetails(@PathVariable String customerId, @PathVariable String orderId,
			HttpServletRequest request) {

		if (isValidToken.isValidToken(request, customerId)) {
			return tradeDtlsService.getCustomerOrderDetails(customerId, orderId);
		} else {
			return ResponseEntity.badRequest().body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
		}
	}

	@GetMapping(value = { ApiURIConstants.GET_ASSET_BY_SEARCH, ApiURIConstants.GET_ASSET_BY_SEARCH_DTLS })
	public ResponseEntity<Object> getAssetBySearch(@PathVariable(required = false) String asset,
			HttpServletRequest request) {
		return tradeDtlsService.getAssetBySearch(asset);
	}

	@GetMapping(value = { ApiURIConstants.GET_ASSET_PAIRS_SEARCH, ApiURIConstants.GET_ASSET_PAIRS_SEARCH_DTLS })
	public ResponseEntity<Object> getAssetPairsSearch(@PathVariable(required = false) String searchString,
			HttpServletRequest request) {
		return tradeDtlsService.getAssetPairsSearch(searchString);
	}

}
