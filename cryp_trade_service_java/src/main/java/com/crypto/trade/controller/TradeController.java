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
import com.crypto.trade.common.OpenApiDesc;
import com.crypto.trade.common.OpenApiRequestDesc;
import com.crypto.trade.dto.TradeDto;
import com.crypto.trade.service.TradeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RequestMapping("/trade")
@RestController
public class TradeController {

	@Autowired
	TradeService orderService;

	@Operation(summary = "Insert Customer Order Details", description = OpenApiDesc.INSERT_CUST_ORDER_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.INSERT_CUST_ORDER_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.INSERT_CUST_ORDER_DTLS)
	public ResponseEntity<Object> insertOrderDtls(@RequestBody TradeDto tradeDto, HttpServletRequest request) {
		return orderService.insertOrderDtls(tradeDto, request);
	}

	@Operation(summary = "Update delete Order Details", description = OpenApiDesc.UPDATE_DELETE_ORDER_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.UPDATE_DELETE_ORDER_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.UPDATE_DELETE_ORDER_DTLS)
	public ResponseEntity<Object> updateDeleteOrderDtls(@RequestBody TradeDto tradeDto, HttpServletRequest request) {
		return orderService.updateDeleteOrderDtls(tradeDto, request);
	}

	@Operation(summary = "Get Asset Symbols", description = OpenApiDesc.GET_ASSET_SYMBOLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.GET_ASSET_SYMBOLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.GET_ASSET_SYMBOLS)
	public ResponseEntity<Object> getAssetSymbols(@RequestBody TradeDto tradeDto , HttpServletRequest request) {
		return orderService.getAssetSymbols(tradeDto, request);
	}
	
	@GetMapping(value = ApiURIConstants.GET_BALANCE)
	public ResponseEntity<Object> getBalance(@PathVariable String customerId, @PathVariable int assetSymbol, HttpServletRequest request) {
		return orderService.getBalance(customerId,assetSymbol, request);
	}
	
	@Operation(summary = "Insert Stop Limit Order", description = OpenApiDesc.INSERT_STOP_LIMIT_ORDER_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.INSERT_STOP_LIMIT_ORDER_DESC, required = true)
	@PostMapping(value = ApiURIConstants.INSERT_STOP_LIMIT_ORDER)
	public ResponseEntity<Object> insertStopLimitOrder(@RequestBody TradeDto tradeDto, HttpServletRequest request) {
		return orderService.insertStopLimitOrder(tradeDto, request);
	}
	
	@Operation(summary = "Insert WatchList Details", description = OpenApiDesc.INSERT_WATCHLIST_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.INSERT_WATCHLIST_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.INSERT_WATCHLIST_DTLS)
	public ResponseEntity<Object> insertWatchlistDetails(@RequestBody TradeDto tradeDto, HttpServletRequest request) {
		return orderService.insertWatchlistDetails(tradeDto, request);
	}
	
	@PostMapping(value = ApiURIConstants.ADD_ASSET_SYMBOLS)
	public ResponseEntity<Object> addAssetSymbols(@RequestBody TradeDto tradeDto, HttpServletRequest request) {
		return orderService.addAssetSymbols(tradeDto, request);
	}

	@Operation(summary = "Insert Customer Order Details", description = OpenApiDesc.INSERT_CUST_ORDER_DTLS_DESC)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful"),
			@ApiResponse(responseCode = "400", description = "One of the required field is empty or contains invalid data, please check your input(s)."),
			@ApiResponse(responseCode = "500", description = "Something went wrong. Please try after sometime.") })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = OpenApiRequestDesc.INSERT_CUST_ORDER_DTLS_DESC, required = true)
	@PostMapping(value = ApiURIConstants.CREATE_BOT_ORDER)
	public ResponseEntity<Object> createBotOrder(@RequestBody TradeDto tradeDto) {
		return orderService.createBotOrder(tradeDto);
	}
}
