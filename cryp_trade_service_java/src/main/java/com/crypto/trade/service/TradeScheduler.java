package com.crypto.trade.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.crypto.trade.aes.PayloadEncryptService;
import com.crypto.trade.common.CommonResponse;
import com.crypto.trade.dao.TradeDao;
import com.crypto.trade.dto.TradeDto;

@Service
@EnableScheduling
public class TradeScheduler {

	private static final Logger log = LoggerFactory.getLogger(TradeScheduler.class);

	@Autowired
	TradeDao tradeDao;

	@Autowired
	PayloadEncryptService payloadEncryptService;

	@Value("${spotmatching.base.url}")
	private String spotMatchingurl;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	TradeService tradeService;

	public static final String SYMBOL_REQ_STR = "\"symbol\":\"";
	public static final String AMOUNT_REQ_STR = "\"amount\":\"";
	public static final String PRICE_REQ_STR = "\"price\":\"";
	public static final String ACTION_REQ_STR = "\"action\":\"";
	public static final String PENDING_NEW = "pending_new";
	public static final String MESSAGE = "message";

	public HttpHeaders common() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.put("Content-Type", Arrays.asList("application/json"));
		return headers;
	}

	@SuppressWarnings("unused")
//	@Scheduled(fixedRate = 3600)
	public void executeStopLimitOrder() {
		List<Map<String, Object>> orders = tradeDao.getAllStopLimitOrderDetails();
		log.info("orders: {}", orders);

		for (Map<String, Object> order : orders) {
			String customerId = (String) order.get("customerId");
			int assetSymbol = (int) order.get("assetSymbol");
			double orderQuantity = (double) order.get("orderQuantity");
			String orderSide = (String) order.get("orderSide");
			String orderType = (String) order.get("orderType");
			double orderLimitPrice = (double) order.get("orderLimitPrice");
			double orderStopPrice = (double) order.get("orderStopPrice");
			String orderStatus = (String) order.get("orderStatus");
			String orderId = (String) order.get("orderId");
			String action = getActionForMatchingApis(orderSide);

			String getMarketApi = spotMatchingurl
					.concat("/getMarketPrice/" + assetSymbol + "/" + action + "/" + orderQuantity);
			log.info("getMarketApi {} ", getMarketApi);
			HttpHeaders getMarketApiRes = common();
			HttpEntity<String> entity = new HttpEntity<>(getMarketApiRes);
			ResponseEntity<String> getMarketApiResponse = restTemplate.exchange(getMarketApi, HttpMethod.GET, entity,
					String.class);
			log.info("getMarketApiResponse {}", getMarketApiResponse);

			JSONObject jsonObjectOrderStatus = new JSONObject(getMarketApiResponse.getBody());
			if (!jsonObjectOrderStatus.isNull("price")) {
				double currentPrice = jsonObjectOrderStatus.getDouble("price");

				log.info("currentPrice {} ", currentPrice);
				log.info("orderStopPrice {} ", orderStopPrice);
				log.info("orderLimitPrice {} ", orderLimitPrice);

				Map<String, Object> assetPairDtls = tradeDao.fetchAssetPairDtlsUsingSymbol(assetSymbol);
				log.info("assetPairDtls {}", assetPairDtls);

				if (currentPrice == orderStopPrice || currentPrice == orderLimitPrice) {
					TradeDto tradeDto = createTradeDtoFromOrder(order, currentPrice);
					CommonResponse<Object> commonRes = tradeService.processPlaceOrderApiResponse(assetPairDtls,
							tradeDto, assetPairDtls.get("BASEASSET").toString());

					int rowsUpdated = tradeDao.updateOrderStatusToExecuted(orderId);
					log.info("rowsUpdated {} ", rowsUpdated);
				} else {
					log.info("No Order Match");
				}

			}
		}

	}

	public TradeDto createTradeDtoFromOrder(Map<String, Object> order, double currentPrice) {
		TradeDto tradeDto = new TradeDto();

		tradeDto.setCustomerId((String) order.get("customerId"));
		tradeDto.setOrdAssetSymbol((int) order.get("assetSymbol"));
		tradeDto.setRequestOrdQty((String) order.get("orderQuantity"));
		tradeDto.setOrdSide((String) order.get("orderSide"));
		tradeDto.setOrdType((String) order.get("orderType"));
		tradeDto.setOrdLimitPrice((String) order.get("orderLimitPrice"));
		tradeDto.setOrdStopPrice((String) order.get("orderStopPrice"));
		tradeDto.setOrdStatus((String) order.get("orderStatus"));
		tradeDto.setOrdPrice(Double.toString(currentPrice));
		return tradeDto;

	}

	public String getActionForMatchingApis(String ordSide) {
		String action = null;
		if (ordSide.equalsIgnoreCase("BUY")) {
			action = "BID";
		} else if (ordSide.equalsIgnoreCase("SELL")) {
			action = "ASK";
		} else {
			log.info("Order side Should be Buy or Sell");
		}
		return action;
	}

}
