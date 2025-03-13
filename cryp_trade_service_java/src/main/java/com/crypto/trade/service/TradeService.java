package com.crypto.trade.service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.crypto.trade.aes.PayloadEncryptService;
import com.crypto.trade.common.CommonMethods;
import com.crypto.trade.common.CommonResponse;
import com.crypto.trade.common.ResponseStatusEnum;
import com.crypto.trade.configuration.ValidateTokenByUsername;
import com.crypto.trade.dao.TradeDao;
import com.crypto.trade.dto.EmailTemplateDto;
import com.crypto.trade.dto.SocketNotifyDto;
import com.crypto.trade.dto.TradeDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TradeService {

	@Autowired
	TradeDao tradeDao;

	@Autowired
	ValidateTokenByUsername isValidToken;

	@Autowired
	PayloadEncryptService payloadEncryptService;

	@Value("${spotmatching.base.url}")
	private String spotMatchingurl;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	NotificationService notificationService;

	@Autowired
	CommonMethods commonMethods;

	@Autowired
	EmailServiceApi emailServiceApi;

	public static final String SYMBOL_REQ_STR = "\"symbol\":\"";
	public static final String AMOUNT_REQ_STR = "\"amount\":\"";
	public static final String PRICE_REQ_STR = "\"price\":\"";
	public static final String ACTION_REQ_STR = "\"action\":\"";
	public static final String PENDING_NEW = "pending_new";
	public static final String MESSAGE = "message";
	public static final String BASE_ASSET = "baseAsset";
	public static final String QUOTE_ASSET = "quoteAsset";
	public static final String WALLET_CURRENT_BALANCE = "waltCurrentBalance";
	public static final String RESPONSE_PLACE_ORDER_API = "responsePlaceOrderApi {}";
	public static final String JSONOBJ_PLACE_ORDER_ARRAY = "jsonObjectPlaceOrderArray {}";
	public static final String JSONOBJ_PLACE_ORDER = "jsonObjectPlaceOrder {}";
	public static final String TRANSACTION = "Transaction";
	public static final String STATUSCODE = "statusCode";
	public static final String RETURNID = "returnId";
	public static final String YOUR_STR = "Your ";
	public static final String FOR_STR = " for ";
	public static final String RESMAP = "resMap {}";
	public static final String ORDERID = "orderId";
	public static final String TRADEID = "tradeId";
	public static final String OFFERID = "offerId";
	public static final String ASSETPAIR_NAME = "assetPairName";
	public static final String SOCKETNOTIFYDTO = "socketNotifyDto {}";
	public static final String PLACEORDER_URL = "/placeOrder";
	public static final String PLACEORDER_URL_STR = "placeOrderApiUrl {} ";
	public static final String JSONREQUEST = "jsonRequest {} ";
	public static final String ALERT_NOTIFICATION_SUCCESS_STR = " has been successfully updated to ";
	public static final String ORDER_OF_STR = " order of ";
	public static final String ORDERSTATUS_STR = "orderStatus {}";
	public static final String PRICE_MODIFY = "price_modify";
	public static final String AMT_MODIFY = "amt_modify";

	public HttpHeaders common() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.put("Content-Type", Arrays.asList("application/json"));
		return headers;
	}

	public ResponseEntity<Object> insertOrderDtls(TradeDto tradeDto, HttpServletRequest request) {

		CommonResponse<Object> commonRes = null;
		try {

//		ObjectMapper objectMapper = new ObjectMapper();
//		TradeDto tradeDto = objectMapper.readValue(payloadEncryptService.getDecryptValue(payload),
//				new TypeReference<TradeDto>() {
//				});
//			log.info("Decrypted payload insertOrderDtls: {}", tradeDto);
			if (StringUtils.hasText(tradeDto.getCustomerId())
					&& isValidToken.isValidToken(request, tradeDto.getCustomerId())) {

				if (StringUtils.hasText(tradeDto.getCustomerId()) && (tradeDto.getOrdAssetSymbol() != 0)
						&& StringUtils.hasText(tradeDto.getRequestOrdQty())
						&& Double.valueOf(tradeDto.getRequestOrdQty()) != 0
						&& (StringUtils.hasText(tradeDto.getOrdSide())) && (StringUtils.hasText(tradeDto.getOrdType()))
						&& StringUtils.hasText(tradeDto.getOrdPrice()) && Double.valueOf(tradeDto.getOrdPrice()) != 0
						&& (StringUtils.hasText(tradeDto.getOrdName()))) {

					boolean price = isValidNumberForOrder(tradeDto.getRequestOrdQty(), tradeDto.getOrdPrice());
					if (price) {

						/** To check balance in db before placing order */

						Map<String, Object> assetPairDtls = tradeDao
								.fetchAssetPairDtlsUsingSymbol(tradeDto.getOrdAssetSymbol());

						String assetToSend = null;
						double requireAmount = 0;
						if (tradeDto.getOrdSide().equals("BUY")) {
							assetToSend = String.valueOf(assetPairDtls.get("quoteAsset"));
							requireAmount = Double.valueOf(tradeDto.getRequestOrdQty())
									* Double.valueOf(tradeDto.getOrdPrice());

						} else {
							assetToSend = String.valueOf(assetPairDtls.get("baseAsset"));
							requireAmount = Double.valueOf(tradeDto.getRequestOrdQty());
						}
						log.info("assetToSend: {}", assetToSend);

						Map<String, Object> fetchBalance = tradeDao.fetchBalance(tradeDto.getCustomerId(), assetToSend);
						log.info("fetchBalance: {}", fetchBalance);
						log.info("requireAmount: {}", requireAmount);

						if (tradeDto.getOrdType().equalsIgnoreCase("MARKET")) {
							// For Market orders, check if the balance is sufficient
							if (fetchBalance.containsKey(WALLET_CURRENT_BALANCE) && requireAmount < Double
									.valueOf(String.valueOf(fetchBalance.get(WALLET_CURRENT_BALANCE)))) {
								commonRes = processPlaceOrderApiResponse(assetPairDtls, tradeDto, assetToSend);
							} else {
								commonRes = new CommonResponse<>(ResponseStatusEnum.INSUFFICIENTBALANCE, null);
							}
						} else {
							if (fetchBalance.containsKey(WALLET_CURRENT_BALANCE) && requireAmount < Double
									.valueOf(String.valueOf(fetchBalance.get(WALLET_CURRENT_BALANCE)))) {
								// For Limit orders, check if the balance is sufficient and check for price
								boolean isPriceValid = checkForPrice(tradeDto);
								if (isPriceValid) {
									commonRes = processPlaceOrderApiResponse(assetPairDtls, tradeDto, assetToSend);
								} else {
									commonRes = new CommonResponse<>(ResponseStatusEnum.INVALIDPRICEERROR, null);
								}
							} else {
								commonRes = new CommonResponse<>(ResponseStatusEnum.INSUFFICIENTBALANCE, null);
							}
						}
					} else {
						commonRes = new CommonResponse<>(400, "Invalid price or quantity", null);
					}

				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
				}

			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}

		} catch (Exception e) {
			log.info("Error in insertOrderDtls:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

	public boolean checkForPrice(TradeDto tradeDto) {

		log.info("symbol {}", tradeDto.getOrdAssetSymbol());
		Map<String, Object> fetchPrice = tradeDao.fetchPrice(tradeDto.getOrdAssetSymbol());
		Map<String, Object> fetchDetails = tradeDao.fetchDetails();

		String action = getActionForMatchingApis(tradeDto.getOrdSide());

		Double bidPrice = (Double) fetchPrice.get("bidPrice");
		Double askPrice = (Double) fetchPrice.get("askPrice");
		Double bidSpread = (Double) fetchDetails.get("bidSpread");
		Double askSpread = (Double) fetchDetails.get("askSpread");

		log.info("bidPrice {}", bidPrice);
		log.info("askPrice {}", askPrice);
		double minPrice = 0;
		double maxPrice = 0;

		if ("BID".equals(action)) {
			// maxPrice = bidPrice + (bidPrice * bidSpread);
			minPrice = bidPrice - (bidPrice * bidSpread);

			// log.info("maxPrice {}", maxPrice);
			log.info("minPrice {}", minPrice);
			return Double.valueOf(tradeDto.getOrdPrice()) >= minPrice;
		} else if ("ASK".equals(action)) {
			maxPrice = askPrice + (askPrice * askSpread);
//			minPrice = bidPrice - (bidPrice * bidSpread);

			log.info("maxPrice {}", maxPrice);
//			log.info("minPrice {}", minPrice);
			return Double.valueOf(tradeDto.getOrdPrice()) <= maxPrice;
		} else {
			return false;
		}

	}

	public CommonResponse<Object> processPlaceOrderApiResponse(Map<String, Object> assetPairDtls, TradeDto tradeDto,
			String assetToSend) {
		CommonResponse<Object> commonRes = null;
		ResponseEntity<String> responsePlaceOrderApi = placeOrderAndProcessResponse(tradeDto, spotMatchingurl);

		// log.info(RESPONSE_PLACE_ORDER_API, responsePlaceOrderApi);

		JSONArray jsonObjectPlaceOrderArray = new JSONArray(responsePlaceOrderApi.getBody());
		log.info("jsonObjectPlaceOrderArray of processPlaceOrderApiResponse", jsonObjectPlaceOrderArray);

		log.info("jsonObjectPlaceOrderArray Length of processPlaceOrderApiResponse {}",
				jsonObjectPlaceOrderArray.length());

		/**
		 * jsonObjectPlaceOrderArray length greater than 1 it will be either Order
		 * Fullfilled or Order Partially Fullfilled
		 */
		if (jsonObjectPlaceOrderArray.length() > 1) {
			commonRes = handleOrderFullfilledOrPartiallyFullfilled(jsonObjectPlaceOrderArray, assetPairDtls, tradeDto,
					assetToSend);
		} else {
			commonRes = handleNewOrder(jsonObjectPlaceOrderArray, assetPairDtls, tradeDto);
		}

		return commonRes;
	}

	public CommonResponse<Object> handleOrderFullfilledOrPartiallyFullfilled(JSONArray jsonObjectPlaceOrderArray,
			Map<String, Object> assetPairDtls, TradeDto tradeDto, String assetToSend) {

		CommonResponse<Object> commonRes = null;

		// to calculate maker side order details, fetch entire array instead of the last
		// position
		log.info("For Order Fullfilled & Order Partially Fullfilled");
		JSONObject jsonObjectPlaceOrder = jsonObjectPlaceOrderArray
				.getJSONObject(jsonObjectPlaceOrderArray.length() - 1);
		log.info(JSONOBJ_PLACE_ORDER, jsonObjectPlaceOrder);

		// order calculation and db insertion for maker side order
		orderFullfilledOrPartiallyFullfilledCommon(jsonObjectPlaceOrder, jsonObjectPlaceOrderArray, assetPairDtls,
				tradeDto, assetToSend);

		// To insert Order Fullfilled Or Order Partially Fullfilled Order details to db
		// for taker side
		Map<String, Object> resMap = tradeDao.orderFullfilledPartiallyFullfilled(tradeDto);
		log.info("instant order match procedure  response : {}", resMap);

		if (!CollectionUtils.isEmpty(resMap)) {
			int returnId = (Integer) resMap.get(RETURNID);
			String message = (String) resMap.get(MESSAGE);

			if (returnId == 1) {

				Map<String, Object> response = new HashMap<>();
				response.put(ORDERID, resMap.get(ORDERID).toString());
				response.put(TRADEID, resMap.get(TRADEID).toString());
				commonRes = new CommonResponse<>(200, message, response);

				// send mail and socket notification to taker person
				buySellMailSenderTemplate(tradeDto, assetPairDtls, jsonObjectPlaceOrder, response, assetToSend);
			} else {
				commonRes = new CommonResponse<>(400, message, null);
			}
		}
		return commonRes;
	}

	public void buySellMailSenderTemplate(TradeDto tradeDto, Map<String, Object> assetPairDtls,
			JSONObject jsonObjectPlaceOrder, Map<String, Object> response, String assetToSend) {
		Map<String, Object> userDetails = tradeDao.getContactDetails(tradeDto.getCustomerId());

		try {
			if (!CollectionUtils.isEmpty(userDetails)) {
				String assetName = tradeDao.getAssetNameByAssetId(tradeDto.getAssetId());
				log.info("assetName: {}", assetName);
				/** get dynamic precisionValue */
				String precisionFormat = commonMethods.getDynamicPrecisionValue(assetToSend);
				log.info("precisionFormat: {}", precisionFormat);
				String totalAmount = tradeDao.getBalance(tradeDto.getAssetId(), tradeDto.getCustomerId());
				log.info("totalAmount: {}", totalAmount);

				double baseAssetBalance = tradeDao.spotBalance(tradeDto.getCustomerId(),
						assetPairDtls.get(BASE_ASSET).toString());

				double quoteAssetBalance = tradeDao.spotBalance(tradeDto.getCustomerId(),
						assetPairDtls.get(QUOTE_ASSET).toString());

				String filledOrFullfilled = (jsonObjectPlaceOrder.getString(STATUSCODE).equalsIgnoreCase("3")
						? " has been filled"
						: " has been partially fullfilled");
				log.info("senderFilledOrFullfilled {}", filledOrFullfilled);

				String formattedValue = new DecimalFormat("0." + precisionFormat)
						.format(Double.valueOf(tradeDto.getRequestOrdQty()));
				log.info("formattedValue: {}", formattedValue);

				String precisionAmount = commonMethods.getPrecisionAmount(formattedValue);
				log.info("precisionAmount: {}", precisionAmount);

				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss");

				double ordPrice = Double.parseDouble(tradeDto.getOrdPrice());
				String formattedPrice = String.format("%.8f", ordPrice);
				log.info("formattedPrice {}", formattedPrice);

				EmailTemplateDto emailTemplateDto = new EmailTemplateDto();
				emailTemplateDto.setCustomerId(tradeDto.getCustomerId());
				emailTemplateDto.setUserName(userDetails.get("custName").toString().replaceAll("\\s+", " "));
				emailTemplateDto.setMailTo(userDetails.get("custEmail").toString());
				emailTemplateDto.setType(tradeDto.getOrdSide());
				emailTemplateDto.setSubject("Order excecuted successfully");
				emailTemplateDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
				emailTemplateDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
				emailTemplateDto.setUnitCount(precisionAmount);
				emailTemplateDto.setPurchaseAmount(formattedPrice);
				emailTemplateDto.setOrderStatus(filledOrFullfilled);
				emailTemplateDto.setOrderId(response.get(ORDERID).toString());
				emailTemplateDto.setDateTime(LocalDateTime.now().format(outputFormatter));

				String basebalance = new DecimalFormat("0." + precisionFormat).format(Double.valueOf(baseAssetBalance));
				String basePrecision = commonMethods.getPrecisionAmount(basebalance);
				emailTemplateDto.setBaseAssetBalance(basePrecision);
				String quotebalance = new DecimalFormat("0." + precisionFormat)
						.format(Double.valueOf(quoteAssetBalance));
				String quotePrecision = commonMethods.getPrecisionAmount(quotebalance);
				emailTemplateDto.setQuoteAssetBalance(quotePrecision);

				log.info("emailTemplateDto: {}", emailTemplateDto);
				String buySellMailStatus = emailServiceApi.commonEmail(emailTemplateDto);
				log.info("Email Status: {}", buySellMailStatus);

				// send socket alerts to taker person
				String alertMessage = YOUR_STR + tradeDto.getOrdSide() + " order of  " + tradeDto.getRequestOrdQty()
						+ " " + tradeDto.getBaseAsset() + FOR_STR + formattedPrice + " " + tradeDto.getQuoteAsset()
						+ filledOrFullfilled;
				log.info("alertMessage {}", alertMessage);

				/**
				 * Socket alert notification for order Fullfilled or Partially Fullfilled to
				 * taker person
				 */
				SocketNotifyDto socketNotifyDto = new SocketNotifyDto();
				socketNotifyDto.setCustomerId(tradeDto.getCustomerId());
				socketNotifyDto.setAlertHeader(TRANSACTION);
				socketNotifyDto.setAlertTitle(tradeDto.getOrdType());
				socketNotifyDto.setAlertMessage(alertMessage);
				notificationMethod(socketNotifyDto);

			} else {
				log.info("Email or Name could not be found while sending E-mail !");
			}

		} catch (Exception e) {
			log.error("Error while sending the email to user ->", e);
		}
	}

	public void buySellRecieverMailTemplate(TradeDto tradeDto, Map<String, Object> assetPairDtls, String assetToSend) {

		String orderId = "EXCORD" + String.format("%010d", Integer.parseInt(tradeDto.getOrderId()));

		Map<String, Object> orderDetails = tradeDao.getOrderDetails(tradeDto.getOrderId());
		log.info("orderDetails {}", orderDetails);
		try {

			if (!CollectionUtils.isEmpty(orderDetails)) {
				String assetName = tradeDao.getAssetNameByAssetId(tradeDto.getAssetId());
				log.info("assetName: {}", assetName);
				/** get dynamic precisionValue */
				String precisionFormat = commonMethods.getDynamicPrecisionValue(assetToSend);
				log.info("precisionFormat: {}", precisionFormat);
				Map<String, Object> customerdetails = tradeDao.getCustomerId(tradeDto.getOrderId());
				log.info("customerdetails {}", customerdetails);
				String totalAmount = tradeDao.getBalance(tradeDto.getAssetId(),
						customerdetails.get("customerId").toString());
				log.info("totalAmount: {}", totalAmount);

				double baseAssetBalance = tradeDao.spotBalance(customerdetails.get("customerId").toString(),
						assetPairDtls.get(BASE_ASSET).toString());

				double quoteAssetBalance = tradeDao.spotBalance(customerdetails.get("customerId").toString(),
						assetPairDtls.get(QUOTE_ASSET).toString());

				String formattedValue = new DecimalFormat("0." + precisionFormat)
						.format(Double.valueOf(tradeDto.getTradeQty()));
				String precisionAmount = commonMethods.getPrecisionAmount(formattedValue);

				double remQty = tradeDao.getRemQty(customerdetails.get("customerId").toString(), tradeDto.getOrderId());
				String filledOrFullfilled = remQty == 0 ? " has been filled" : " has been partially filled";
				log.info("recieverFilledOrFullfilled {}", filledOrFullfilled);

				String quotebalance = new DecimalFormat("0." + precisionFormat)
						.format(Double.valueOf(quoteAssetBalance));
				String quotePrecision = commonMethods.getPrecisionAmount(quotebalance);

				String basebalance = new DecimalFormat("0." + precisionFormat).format(Double.valueOf(baseAssetBalance));
				String basePrecision = commonMethods.getPrecisionAmount(basebalance);

				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss");

				String formattedPrice = String.format("%.8f", Double.valueOf(tradeDto.getTradePrice()));
				log.info("formattedPrice {}", formattedPrice);

				EmailTemplateDto emailTemplateDto = new EmailTemplateDto();
				emailTemplateDto.setCustomerId(customerdetails.get("customerId").toString());
				emailTemplateDto.setUserName(orderDetails.get("custName").toString().replaceAll("\\s+", " "));
				emailTemplateDto.setMailTo(orderDetails.get("emailId").toString());
				emailTemplateDto.setType(customerdetails.get("ordSide").toString());
				emailTemplateDto.setSubject("Order excecuted successfully");
				emailTemplateDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
				emailTemplateDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
				emailTemplateDto.setUnitCount(precisionAmount);
				emailTemplateDto.setPurchaseAmount(formattedPrice);
				emailTemplateDto.setBaseAssetBalance(basePrecision);
				emailTemplateDto.setQuoteAssetBalance(quotePrecision);
				emailTemplateDto.setOrderStatus(filledOrFullfilled);
				emailTemplateDto.setOrderId(orderId);
				emailTemplateDto.setDateTime(LocalDateTime.now().format(outputFormatter));
				log.info("emailTemplateDto: {}", emailTemplateDto);
				String buySellMailStatus = emailServiceApi.commonEmail(emailTemplateDto);
				log.info("Email Status: {}", buySellMailStatus);

				// send socket alerts to maker persons
				String alertMessage = YOUR_STR + customerdetails.get("ordSide").toString() + " order of  "
						+ tradeDto.getTradeQty() + " " + emailTemplateDto.getBaseAsset() + FOR_STR + formattedPrice
						+ " " + emailTemplateDto.getQuoteAsset() + filledOrFullfilled;
				log.info("alertMessage {}", alertMessage);

				/**
				 * Socket alert notification for order Fullfilled or Partially Fullfilled to
				 * taker person
				 */
				SocketNotifyDto socketNotifyDto = new SocketNotifyDto();
				socketNotifyDto.setCustomerId(customerdetails.get("customerId").toString());
				socketNotifyDto.setAlertHeader(TRANSACTION);
				socketNotifyDto.setAlertTitle(customerdetails.get("ordSide").toString());
				socketNotifyDto.setAlertMessage(alertMessage);
				notificationMethod(socketNotifyDto);
			} else {
				log.info("Email or Name could not be found while sending E-mail !");
			}

		} catch (Exception e) {
			log.error("Error while sending the email to user ->", e);
		}
	}

	public CommonResponse<Object> handleNewOrder(JSONArray jsonObjectPlaceOrderArray, Map<String, Object> assetPairDtls,
			TradeDto tradeDto) {
		CommonResponse<Object> commonRes = null;
		/** if jsonObjectPlaceOrder is 1 it will Create a new Order */
		log.info("Create a new Order");

		JSONObject jsonObjectPlaceOrder = jsonObjectPlaceOrderArray.getJSONObject(0);
		log.info(JSONOBJ_PLACE_ORDER, jsonObjectPlaceOrder);

		if (jsonObjectPlaceOrder.getString(STATUSCODE).equalsIgnoreCase("1"))
			tradeDto.setOrdStatus("NEW");

		if (!StringUtils.hasText(tradeDto.getOrderId()))
			tradeDto.setOrderId(jsonObjectPlaceOrder.getString(OFFERID));

		tradeDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
		tradeDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
		tradeDto.setOrdAssetPairName(assetPairDtls.get(ASSETPAIR_NAME).toString());

		/** To Insert New Order details to db */
		Map<String, Object> resMap = tradeDao.insertCustOrderDtls(tradeDto);
		log.info(RESMAP, resMap);

		if (!CollectionUtils.isEmpty(resMap)) {
			int returnId = (Integer) resMap.get(RETURNID);
			String message = (String) resMap.get(MESSAGE);

			if (returnId == 1) {

				Map<String, Object> response = new HashMap<>();
				response.put(ORDERID, resMap.get(ORDERID));
				String alertMessage = null;

				/** Socket Notification for new order */
				SocketNotifyDto socketNotifyDto = new SocketNotifyDto();

				socketNotifyDto.setAlertTitle(tradeDto.getOrdType());
				alertMessage = YOUR_STR + tradeDto.getOrdSide() + " order of  " + tradeDto.getRequestOrdQty() + " "
						+ tradeDto.getBaseAsset() + FOR_STR + tradeDto.getOrdPrice() + " " + tradeDto.getQuoteAsset()
						+ " has been placed.";

				socketNotifyDto.setCustomerId(tradeDto.getCustomerId());
				socketNotifyDto.setAlertHeader(TRANSACTION);

				socketNotifyDto.setAlertMessage(alertMessage);
				log.info(SOCKETNOTIFYDTO, socketNotifyDto);
				notificationMethod(socketNotifyDto);

				commonRes = new CommonResponse<>(200, message, response);
			} else {
				commonRes = new CommonResponse<>(400, message, null);
			}
		}
		return commonRes;
	}

	public String buildOrderRequest(TradeDto tradeDto, String action) {
		StringBuilder requestString = new StringBuilder("{").append(SYMBOL_REQ_STR).append(tradeDto.getOrdAssetSymbol())
				.append("\",").append(AMOUNT_REQ_STR).append(tradeDto.getRequestOrdQty()).append("\",")
				.append(PRICE_REQ_STR).append(tradeDto.getOrdPrice()).append("\",").append(ACTION_REQ_STR)
				.append(action).append("\"}");

		return requestString.toString();
	}

	public ResponseEntity<String> placeOrderAndProcessResponse(TradeDto tradeDto, String spotMatchingurl) {

		String placeOrderApiUrl = spotMatchingurl.concat(PLACEORDER_URL);
		log.info(PLACEORDER_URL_STR, placeOrderApiUrl);

		String action = getActionForMatchingApis(tradeDto.getOrdSide());
		String jsonRequest = buildOrderRequest(tradeDto, action);

		log.info(JSONREQUEST, jsonRequest);

		HttpHeaders headers = common();
		HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
		ResponseEntity<String> responsePlaceOrderApi = restTemplate.exchange(placeOrderApiUrl, HttpMethod.POST, entity,
				String.class);

		log.info(RESPONSE_PLACE_ORDER_API, responsePlaceOrderApi);

		return responsePlaceOrderApi;
	}

	private String getActionForMatchingApis(String ordSide) {
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

	public ResponseEntity<Object> updateDeleteOrderDtls(TradeDto tradeDto, HttpServletRequest request) {

		CommonResponse<Object> commonRes = null;
		try {

//		ObjectMapper objectMapper = new ObjectMapper();
//		TradeDto tradeDto = objectMapper.readValue(payloadEncryptService.getDecryptValue(payload),
//				new TypeReference<TradeDto>() {
//				});
			log.info("Decrypted payload updateDeleteOrderDtls: {}", tradeDto);
			if (StringUtils.hasText(tradeDto.getCustomerId())
					&& isValidToken.isValidToken(request, tradeDto.getCustomerId())) {

				boolean isStopLimitOrder = "StopLimit".equalsIgnoreCase(tradeDto.getOrdType());
				boolean isMarketOrder = ("Market".equalsIgnoreCase(tradeDto.getOrdType())
						|| "SPOT".equalsIgnoreCase(tradeDto.getOrdType()));

				if (StringUtils.hasText(tradeDto.getCustomerId()) && tradeDto.getOrdAssetSymbol() != 0
						&& (StringUtils.hasText(tradeDto.getFlagValue())
								&& StringUtils.hasText(tradeDto.getRequestOrdQty())
								&& StringUtils.hasText(tradeDto.getOrdSide())
								&& StringUtils.hasText(tradeDto.getOrdType())
								&& StringUtils.hasText(tradeDto.getOrdPrice()) && (!isMarketOrder || !isStopLimitOrder)
								|| StringUtils.hasText(tradeDto.getOrdStatus())
										&& StringUtils.hasText(tradeDto.getOrdName())
										&& StringUtils.hasText(tradeDto.getOrderId())
										&& StringUtils.hasText(tradeDto.getOrdStopPrice())
										&& StringUtils.hasText(tradeDto.getOrdLimitPrice())
										&& Double.valueOf(tradeDto.getOrdStopPrice()) != 0
										&& Double.valueOf(tradeDto.getOrdLimitPrice()) != 0)) {

					if (tradeDto.getOrdType().equalsIgnoreCase("stop-limit")) {
						boolean stopPrice = isValidNumberForOrder(tradeDto.getOrdStopPrice(),
								tradeDto.getOrdLimitPrice());
						if (stopPrice) {

							Map<String, Object> resMap = tradeDao.updateOrDeleteStopOrderDtls(tradeDto);
							if (!CollectionUtils.isEmpty(resMap)) {
								int returnId = (Integer) resMap.get(RETURNID);
								String message = (String) resMap.get(MESSAGE);
								if (returnId == 1 || returnId == 2) {
									commonRes = new CommonResponse<>(200, message, null);
								} else {
									commonRes = new CommonResponse<>(400, message, null);
								}
							}
						} else {
							commonRes = new CommonResponse<>(400, "Invalid stop-price or limit-price", null);
						}

					} else {
						boolean price = isValidNumberForOrder(tradeDto.getRequestOrdQty(), tradeDto.getOrdPrice());
						if (price) {

							Map<String, Object> assetPairDtls = tradeDao
									.fetchAssetPairDtlsUsingSymbol(tradeDto.getOrdAssetSymbol());
							// log.info("assetPairDtls {}", assetPairDtls);

							String matchingEngineOrderId = tradeDto.getOrderId().replaceAll("\\D", ""); // Remove
																										// non-digits
							matchingEngineOrderId = matchingEngineOrderId.replaceFirst("^0+", ""); // Remove leading
																									// zeros
							log.info("matchingEngineOrderId {}", matchingEngineOrderId);
							String checkOrderStatusUrl = spotMatchingurl.concat(
									"/checkOrderStatus/" + tradeDto.getOrdAssetSymbol() + "/" + matchingEngineOrderId);
							// log.info("checkOrderStatusUrl {} ", checkOrderStatusUrl);
							HttpHeaders headersOrderStatus = common();
							HttpEntity<String> entity = new HttpEntity<>(headersOrderStatus);
							ResponseEntity<String> responseOrderStatus = restTemplate.exchange(checkOrderStatusUrl,
									HttpMethod.GET, entity, String.class);
							// log.info("responseOrderStatus {}", responseOrderStatus);

							JSONObject jsonObjectOrderStatus = new JSONObject(responseOrderStatus.getBody());
							log.info("jsonObjectOrderStatus for update and delete order : {}", jsonObjectOrderStatus);

							if (tradeDto.getFlagValue().equalsIgnoreCase("cancel")) {
								/** To cancel order */
								commonRes = deleteOrderDetails(tradeDto, assetPairDtls, jsonObjectOrderStatus,
										matchingEngineOrderId);

							} else {
								/** To update order */
								commonRes = updateOrders(tradeDto, assetPairDtls, jsonObjectOrderStatus,
										matchingEngineOrderId);
							}
						} else {
							commonRes = new CommonResponse<>(400, "Invalid price or quantity", null);
						}
					}

				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
				}

			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}

		} catch (Exception e) {
			log.info("Error in updateDeleteOrderDtls:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

	public CommonResponse<Object> deleteOrderDetails(TradeDto tradeDto, Map<String, Object> assetPairDtls,
			JSONObject jsonObjectOrderStatus, String matchingEngineOrderId) {
		CommonResponse<Object> commonRes = null;
		log.info("jsonObjectOrderStatus for delete order : {} ", jsonObjectOrderStatus);

		if (jsonObjectOrderStatus.get(STATUSCODE).toString().equalsIgnoreCase("1")) {

			/** To call place order api here & set response data in dto to insert in db */
			String placeOrderApiUrl = spotMatchingurl.concat(PLACEORDER_URL);
			// log.info(PLACEORDER_URL_STR, placeOrderApiUrl);

			StringBuilder requestString = reqBodyForPlaceOrderApi(tradeDto,
					getActionForMatchingApis(tradeDto.getOrdSide()));
			String jsonRequest = requestString.toString();
			log.info("jsonRequest for delete order : {}", jsonRequest);

			HttpHeaders headers = common();
			HttpEntity<String> entityCustodialAcc = new HttpEntity<>(jsonRequest, headers);
			ResponseEntity<String> responsePlaceOrderApi = restTemplate.exchange(placeOrderApiUrl, HttpMethod.POST,
					entityCustodialAcc, String.class);
			// log.info(RESPONSE_PLACE_ORDER_API, responsePlaceOrderApi);

			JSONArray jsonObjectPlaceOrderArray = new JSONArray(responsePlaceOrderApi.getBody());
			log.info("jsonObjectPlaceOrderArray for delete order : {}", jsonObjectPlaceOrderArray);

			log.info("array length for delete order {}", jsonObjectPlaceOrderArray.length());

			/** if jsonObjectPlaceOrder is 1 it will delete a Order */
			if (jsonObjectPlaceOrderArray.length() == 1) {

				JSONObject jsonObjectPlaceOrder = jsonObjectPlaceOrderArray.getJSONObject(0);
				// log.info(JSONOBJ_PLACE_ORDER, jsonObjectPlaceOrder);

				String orderStatus = setOrderStatusBasedOnStatusCode((jsonObjectPlaceOrder.getString(STATUSCODE)),
						jsonObjectPlaceOrder.getString(MESSAGE));

				if (orderStatus != null) {
					tradeDto.setOrdStatus(orderStatus);
					log.info("order status for delete order : {}", orderStatus);

					if (!StringUtils.hasText(tradeDto.getOrderId()))
						tradeDto.setOrderId(jsonObjectPlaceOrder.getString(OFFERID));

					tradeDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
					tradeDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
					tradeDto.setOrdAssetPairName(assetPairDtls.get(ASSETPAIR_NAME).toString());
					tradeDto.setOrderId(tradeDto.getOrderId());

					Map<String, Object> assetName = tradeDao.fetchAssetName(matchingEngineOrderId);
					// log.info("assetName {}", assetName);

					String asset = setAssetBasedBuyOrSell(assetName, tradeDto);

					tradeDto.setAssetName(asset);

					/** To Insert Order details to db to delete order */
					Map<String, Object> resMap = tradeDao.updateOrDeleteOrderDtls(tradeDto);
					log.info("update delete cust order procedure response : {}", resMap);

					commonRes = getResponseForUpdateOrDeleteOrderDtls(resMap, tradeDto);

				}
			} else {
				commonRes = new CommonResponse<>(400, "Error! While fetching previous order Details", null);
			}
		} else {
			commonRes = new CommonResponse<>(400, "Error! While fetching offer Details", null);
		}

		return commonRes;
	}

	private String setAssetBasedBuyOrSell(Map<String, Object> assetName, TradeDto tradeDto) {

		return (tradeDto.getOrdSide().equalsIgnoreCase("BUY")) ? assetName.get(QUOTE_ASSET).toString()
				: assetName.get(BASE_ASSET).toString();
	}

	private CommonResponse<Object> getResponseForUpdateOrDeleteOrderDtls(Map<String, Object> resMap,
			TradeDto tradeDto) {
		CommonResponse<Object> commonRes = null;
		if (!CollectionUtils.isEmpty(resMap)) {
			int returnId = (Integer) resMap.get(RETURNID);
			String message = (String) resMap.get(MESSAGE);

			if (returnId == 4) {

				SocketNotifyDto socketNotifyDto = new SocketNotifyDto();
				socketNotifyDto.setCustomerId(tradeDto.getCustomerId());
				socketNotifyDto.setAlertHeader(TRANSACTION);
				socketNotifyDto.setAlertTitle(tradeDto.getOrdType());
				socketNotifyDto.setAlertMessage(YOUR_STR + tradeDto.getOrdSide() + ORDER_OF_STR
						+ tradeDto.getBaseAsset() + FOR_STR + tradeDto.getRequestOrdQty() + " "
						+ tradeDto.getQuoteAsset() + " has been cancelled.");

				notificationMethod(socketNotifyDto);
				commonRes = new CommonResponse<>(200, message, null);
			} else {
				commonRes = new CommonResponse<>(400, message, null);
			}
		}
		return commonRes;
	}

	public CommonResponse<Object> updateOrders(TradeDto tradeDto, Map<String, Object> assetPairDtls,
			JSONObject jsonObjectOrderStatus, String matchingEngineOrderId) {
		CommonResponse<Object> commonRes = null;

		if (jsonObjectOrderStatus.get(STATUSCODE).toString().equalsIgnoreCase("1")) {

			Map<String, Object> prevDtls = tradeDao.fetchPreviousOrderDtls(matchingEngineOrderId);
			log.info("fetch prevDtls from db : {}", prevDtls);

			Map<String, Object> check = tradeDao.checkDetails(tradeDto.getOrderId());
			double currentQty = Double.parseDouble(check.get("currentQty").toString());
			double currentPrice = Double.parseDouble(check.get("currentPrice").toString());
//			log.info("currentQty {}", currentQty);
//			log.info("currentPrice {}", currentPrice);

			String assetToSend = tradeDto.getOrdSide().equals("BUY") ? String.valueOf(assetPairDtls.get(QUOTE_ASSET))
					: String.valueOf(assetPairDtls.get(BASE_ASSET));

			Map<String, Object> checkBalance = tradeDao.fetchBalance(tradeDto.getCustomerId(), assetToSend);
			/** To check previous order details are same or not */

			if (!CollectionUtils.isEmpty(prevDtls)) {
				if ((double) prevDtls.get("orderQty") == Double.valueOf(tradeDto.getRequestOrdQty())
						&& (double) prevDtls.get("orderPrice") == Double.valueOf(tradeDto.getOrdPrice())) {
					commonRes = new CommonResponse<>(400, "Error! Previous order & New order details can't be same",
							null);
				} else if (Double.valueOf(tradeDto.getRequestOrdQty()) > currentQty
						|| Double.valueOf(tradeDto.getOrdPrice()) > currentPrice) {
					/** To check if order details are greater than previous order details */

					commonRes = getResponseForUpdateQtyOrPrice(tradeDto, checkBalance, currentPrice, currentQty,
							assetPairDtls, matchingEngineOrderId, assetToSend);

				} else {
					/** To check if order details are lesser than previous order details */

					boolean isPriceValid = checkForPrice(tradeDto);

					commonRes = getResponseForUpdateOrderDtls(isPriceValid, tradeDto, assetPairDtls,
							matchingEngineOrderId, assetToSend);

				}

			} else {
				commonRes = new CommonResponse<>(400, "Error! While fetching previous order Details", null);
			}

		} else {
			commonRes = new CommonResponse<>(400, "Error! While fetching offer Details", null);
		}

		return commonRes;
	}

	private CommonResponse<Object> getResponseForUpdateOrderDtls(boolean isPriceValid, TradeDto tradeDto,
			Map<String, Object> assetPairDtls, String matchingEngineOrderId, String assetToSend) {
		CommonResponse<Object> commonRes = null;

		if (tradeDto.getOrdType().equalsIgnoreCase("MARKET") && !isPriceValid) {
			commonRes = new CommonResponse<>(400, "Market order cannot be updated", null);
		} else if (isPriceValid) {
			commonRes = updateOrderDetails(tradeDto, assetPairDtls, matchingEngineOrderId, assetToSend);
		} else {
			commonRes = new CommonResponse<>(ResponseStatusEnum.INVALIDPRICEERROR, null);
		}
		return commonRes;
	}

	private CommonResponse<Object> getResponseForUpdateQtyOrPrice(TradeDto tradeDto, Map<String, Object> checkBalance,
			double currentPrice, double currentQty, Map<String, Object> assetPairDtls, String matchingEngineOrderId,
			String assetToSend) {
		CommonResponse<Object> commonRes = null;
		double requireAmount = 0;
		boolean flag = false;

		// for price modify calculation in buy side
		if (tradeDto.getOrdSide().equals("BUY")) {
			if (tradeDto.getFlagValue().equalsIgnoreCase(PRICE_MODIFY)) {
				requireAmount = ((Double.valueOf(tradeDto.getOrdPrice()) - currentPrice)
						* Double.valueOf(tradeDto.getRequestOrdQty()));
			} else {
				requireAmount = ((Double.valueOf(tradeDto.getRequestOrdQty()) - currentQty)
						* Double.valueOf(tradeDto.getOrdPrice()));
			}
		} else {
			// for amount modify calculation in sell side
			if (tradeDto.getFlagValue().equalsIgnoreCase(AMT_MODIFY)) {
				requireAmount = (Double.valueOf(tradeDto.getRequestOrdQty()) - currentQty);
			} else {
				flag = true;
			}
		}

		/** To check balance */
		log.info("balance of update order : {}", requireAmount);
		log.info("waltCurrentBalance for update order : {}",
				Double.valueOf(String.valueOf(checkBalance.get(WALLET_CURRENT_BALANCE))));
		if ((checkBalance.containsKey(WALLET_CURRENT_BALANCE)
				&& requireAmount < Double.valueOf(String.valueOf(checkBalance.get(WALLET_CURRENT_BALANCE)))) || flag) {
			boolean isPriceValid = checkForPrice(tradeDto);

			if (tradeDto.getOrdType().equalsIgnoreCase("MARKET") && !isPriceValid) {
				commonRes = new CommonResponse<>(400, "Market order cannot be updated", null);
			} else if (isPriceValid) {
				commonRes = updateOrderDetails(tradeDto, assetPairDtls, matchingEngineOrderId, assetToSend);
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.INVALIDPRICEERROR, null);
			}

		} else {
			commonRes = new CommonResponse<>(ResponseStatusEnum.INSUFFICIENTBALANCE, null);
		}
		return commonRes;
	}

	public CommonResponse<Object> updateOrderDetails(TradeDto tradeDto, Map<String, Object> assetPairDtls,
			String matchingEngineOrderId, String assetToSend) {
		CommonResponse<Object> commonRes = null;
		String placeOrderApiUrl = spotMatchingurl.concat(PLACEORDER_URL);
		// log.info(PLACEORDER_URL_STR, placeOrderApiUrl);

		StringBuilder requestString = reqBodyForPlaceOrderApi(tradeDto,
				getActionForMatchingApis(tradeDto.getOrdSide()));
		String jsonRequest = requestString.toString();
		log.info("jsonRequest of the update order : {}", jsonRequest);

		HttpHeaders headers = common();
		HttpEntity<String> entityCustodialAcc = new HttpEntity<>(jsonRequest, headers);
		ResponseEntity<String> responsePlaceOrderApi = restTemplate.exchange(placeOrderApiUrl, HttpMethod.POST,
				entityCustodialAcc, String.class);
		// log.info(RESPONSE_PLACE_ORDER_API, responsePlaceOrderApi);

		JSONArray jsonObjectPlaceOrderArray = new JSONArray(responsePlaceOrderApi.getBody());
		log.info("jsonObjectPlaceOrderArray of the update order : {}", jsonObjectPlaceOrderArray);

		log.info("length of the array for the update order {}", jsonObjectPlaceOrderArray.length());

		// if length is 1 it will update the order length greater than 1 it will be
		// partially filled or filled
		if (jsonObjectPlaceOrderArray.length() == 1) {
			commonRes = updateOrder(jsonObjectPlaceOrderArray.getJSONObject(0), tradeDto, assetPairDtls,
					matchingEngineOrderId);
		} else {
			commonRes = orderFilledOrPartiallyFullfilled(jsonObjectPlaceOrderArray, tradeDto, assetPairDtls,
					assetToSend);
		}

		return commonRes;
	}

	public CommonResponse<Object> updateOrder(JSONObject jsonObjectPlaceOrder, TradeDto tradeDto,
			Map<String, Object> assetPairDtls, String matchingEngineOrderId) {

		CommonResponse<Object> commonRes = null;
		log.info("jsonObjectPlaceOrder for update order : {}", jsonObjectPlaceOrder);
		String orderStatus = setOrderStatusBasedOnStatusCode(jsonObjectPlaceOrder.getString(STATUSCODE),
				jsonObjectPlaceOrder.getString(MESSAGE));
//		log.info(ORDERSTATUS_STR, orderStatus);
		if (orderStatus != null) {
			tradeDto.setOrdStatus(orderStatus);
			log.info("orderStatus for update order : {}", orderStatus);

			if (!StringUtils.hasText(tradeDto.getOrderId()))
				tradeDto.setOrderId(jsonObjectPlaceOrder.getString(OFFERID));
			tradeDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
			tradeDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
			tradeDto.setOrdAssetPairName(assetPairDtls.get(ASSETPAIR_NAME).toString());
			tradeDto.setExcOrderId(tradeDto.getOrderId());

			Map<String, Object> assetName = tradeDao.fetchAssetName(matchingEngineOrderId);
			// log.info("assetName {}", assetName);

			String asset = (tradeDto.getOrdSide().equalsIgnoreCase("BUY")) ? assetName.get(QUOTE_ASSET).toString()
					: assetName.get(BASE_ASSET).toString();
			tradeDto.setAssetName(asset);

			Map<String, Object> resMap = tradeDao.updateOrDeleteOrderDtls(tradeDto);
			log.info("update delete cust order details procedure response", resMap);

			if (!CollectionUtils.isEmpty(resMap)) {

				commonRes = getUpdateOrDeleteOrderDtls(resMap, tradeDto);

			}

		} else {
			commonRes = new CommonResponse<>(400, "Please give a proper flagvalue", null);
		}
		return commonRes;
	}

	private CommonResponse<Object> getUpdateOrDeleteOrderDtls(Map<String, Object> resMap, TradeDto tradeDto) {
		CommonResponse<Object> commonRes = null;
		int returnId = (Integer) resMap.get(RETURNID);
		String message = (String) resMap.get(MESSAGE);

		if (returnId == 1) {

			SocketNotifyDto socketNotifyDto = new SocketNotifyDto();
			socketNotifyDto.setCustomerId(tradeDto.getCustomerId());
			socketNotifyDto.setAlertHeader(TRANSACTION);
			socketNotifyDto.setAlertTitle(tradeDto.getOrdType());
			if (tradeDto.getFlagValue().equalsIgnoreCase(PRICE_MODIFY)) {
				socketNotifyDto.setAlertMessage("Price of your " + tradeDto.getOrdSide() + " order "
						+ tradeDto.getRequestOrdQty() + " " + tradeDto.getBaseAsset() + ALERT_NOTIFICATION_SUCCESS_STR
						+ tradeDto.getOrdPrice() + " " + tradeDto.getQuoteAsset() + ".");
			} else if (tradeDto.getFlagValue().equalsIgnoreCase(AMT_MODIFY)) {
				socketNotifyDto.setAlertMessage("Unit count of your " + tradeDto.getOrdSide() + ORDER_OF_STR
						+ tradeDto.getOrdPrice() + " " + tradeDto.getQuoteAsset() + ALERT_NOTIFICATION_SUCCESS_STR
						+ tradeDto.getRequestOrdQty() + " " + tradeDto.getBaseAsset() + ".");
			}

			log.info(SOCKETNOTIFYDTO, socketNotifyDto);
			notificationMethod(socketNotifyDto);

			commonRes = new CommonResponse<>(200, message, null);
		} else {
			commonRes = new CommonResponse<>(400, message, null);
		}
		return commonRes;
	}

	public CommonResponse<Object> orderFilledOrPartiallyFullfilled(JSONArray jsonObjectPlaceOrderArray,
			TradeDto tradeDto, Map<String, Object> assetPairDtls, String assetToSend) {
		CommonResponse<Object> commonRes = null;
		log.info("For Order filled & Order Partially filled");
		JSONObject jsonObjectPlaceOrder = jsonObjectPlaceOrderArray
				.getJSONObject(jsonObjectPlaceOrderArray.length() - 1);
		log.info("jsonObjectPlaceOrder of the order partally filled or filled", jsonObjectPlaceOrder);

		SocketNotifyDto socketNotifyDto = new SocketNotifyDto();
		socketNotifyDto.setCustomerId(tradeDto.getCustomerId());
		socketNotifyDto.setAlertHeader(TRANSACTION);
		socketNotifyDto.setAlertTitle(tradeDto.getOrdType());

		if (tradeDto.getFlagValue().equalsIgnoreCase(PRICE_MODIFY)) {
			socketNotifyDto.setAlertMessage(
					"Price of your " + tradeDto.getOrdSide() + " order " + tradeDto.getRequestOrdQty() + " "
							+ assetPairDtls.get(BASE_ASSET) + ALERT_NOTIFICATION_SUCCESS_STR + tradeDto.getOrdPrice()
							+ " " + assetPairDtls.get(QUOTE_ASSET) + " and the order has been fullfilled.");
		} else if (tradeDto.getFlagValue().equalsIgnoreCase(AMT_MODIFY)) {
			socketNotifyDto.setAlertMessage("Unit count of your " + tradeDto.getOrdSide() + ORDER_OF_STR
					+ tradeDto.getOrdPrice() + " " + assetPairDtls.get(QUOTE_ASSET) + ALERT_NOTIFICATION_SUCCESS_STR
					+ tradeDto.getRequestOrdQty() + " " + assetPairDtls.get(BASE_ASSET) + ".");
		}

		log.info(SOCKETNOTIFYDTO, socketNotifyDto);

		orderFullfilledOrPartiallyFullfilledCommon(jsonObjectPlaceOrder, jsonObjectPlaceOrderArray, assetPairDtls,
				tradeDto, assetToSend);

		Map<String, Object> resMap = tradeDao.orderFullfilledPartiallyFullfilled(tradeDto);
		log.info(RESMAP, resMap);

		if (!CollectionUtils.isEmpty(resMap)) {
			int returnId = (Integer) resMap.get(RETURNID);
			String message = (String) resMap.get(MESSAGE);

			if (returnId == 1) {
				Map<String, Object> response = new HashMap<>();
				response.put(ORDERID, resMap.get(ORDERID).toString());
				response.put(TRADEID, resMap.get(TRADEID).toString());

				notificationMethod(socketNotifyDto);

				commonRes = new CommonResponse<>(200, message, response);
			} else {
				commonRes = new CommonResponse<>(400, message, null);
			}
		}
		return commonRes;
	}

	private void orderFullfilledOrPartiallyFullfilledCommon(JSONObject jsonObjectPlaceOrder,
			JSONArray jsonObjectPlaceOrderArray, Map<String, Object> assetPairDtls, TradeDto tradeDto,
			String assetToSend) {

		tradeDto.setTradeId(jsonObjectPlaceOrder.getString(TRADEID));
		for (int i = 0; i < jsonObjectPlaceOrderArray.length() - 1; i++) {

			JSONObject jsonObject = jsonObjectPlaceOrderArray.getJSONObject(i);
			log.info("match orders - jsonObject {}", jsonObject);
			tradeDto.setOrderId(jsonObject.getString("matchedOfferId"));
			tradeDto.setTradeQty(jsonObject.getDouble("volume"));
			tradeDto.setTradePrice(jsonObject.getDouble("matchedOfferPrice"));

			Map<String, Object> resMap = tradeDao.updateMatchedOrders(tradeDto);
			log.info("match offer Id : {} - returnId : {}", jsonObject.getString("matchedOfferId"),
					(Integer) resMap.get(RETURNID));
			log.info("message {}", (String) resMap.get(MESSAGE));

			// send mail and socket alerts to maker ( matching order persons)
			buySellRecieverMailTemplate(tradeDto, assetPairDtls, assetToSend);
		}
		if (jsonObjectPlaceOrder.getString(STATUSCODE).equalsIgnoreCase("3")) {
			log.info("For Order filled ");
			tradeDto.setOrdStatus("filled");
			tradeDto.setOrderId(jsonObjectPlaceOrder.get(TRADEID).toString());

		} else if (jsonObjectPlaceOrder.getString(STATUSCODE).equalsIgnoreCase("4")) {
			log.info("Order Partially filled");
			tradeDto.setOrdStatus("partially filled");
			tradeDto.setOrderId(jsonObjectPlaceOrder.get(OFFERID).toString());

		}
		tradeDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
		tradeDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
		tradeDto.setRemainingQty(jsonObjectPlaceOrder.getDouble("remaningQty"));
		tradeDto.setTrdPrice(jsonObjectPlaceOrder.getDouble("tradePrice"));
		tradeDto.setReqQty(jsonObjectPlaceOrder.getDouble("requestAmount"));
		tradeDto.setReqPrice(jsonObjectPlaceOrder.getDouble("requestPrice"));
		tradeDto.setTrdAssetSymbol(jsonObjectPlaceOrder.getInt("symbol"));
		String ordSide = jsonObjectPlaceOrder.getString("action").equals("BID") ? "BUY" : "SELL";
		tradeDto.setOrdSide(ordSide);
		tradeDto.setTradeId(jsonObjectPlaceOrder.getString(TRADEID));
		tradeDto.setStatusCode(jsonObjectPlaceOrder.getInt(STATUSCODE));
		tradeDto.setOrdType(tradeDto.getOrdType());

	}

	private String setOrderStatusBasedOnStatusCode(String statusCodeResponse, String messageResponse) {
		String orderStatus = null;
		if (statusCodeResponse.equalsIgnoreCase("1") || (statusCodeResponse.equalsIgnoreCase("2")
				&& (messageResponse.contains("amount") || messageResponse.contains("price")))) {
			orderStatus = PENDING_NEW;
		} else if (statusCodeResponse.equalsIgnoreCase("5")) {
			orderStatus = "Cancel";
		} else {
			orderStatus = null;
		}
		return orderStatus;
	}

	private StringBuilder reqBodyForPlaceOrderApi(TradeDto tradeDto, String action) {
		StringBuilder requestString = null;

		String matchingEngineOrderId = tradeDto.getOrderId().replaceAll("\\D", ""); // Remove non-digits
		matchingEngineOrderId = matchingEngineOrderId.replaceFirst("^0+", ""); // Remove leading zeros
		log.info("extractedNumber {}", matchingEngineOrderId);

		if (StringUtils.hasText(tradeDto.getFlagValue()) && (tradeDto.getFlagValue().equalsIgnoreCase("cancel")
				|| tradeDto.getFlagValue().equalsIgnoreCase(AMT_MODIFY))) {
			log.info("Update Amount or Delete Order");

			/** Update Order for amount update */
			requestString = new StringBuilder("{").append(SYMBOL_REQ_STR + tradeDto.getOrdAssetSymbol() + "\",")
					.append(AMOUNT_REQ_STR + tradeDto.getRequestOrdQty() + "\",")
					.append(PRICE_REQ_STR + tradeDto.getOrdPrice() + "\",").append(ACTION_REQ_STR + action + "\",")
					.append("\"offerId\":\"" + matchingEngineOrderId + "\",")
					.append("\"flag\":\"" + tradeDto.getFlagValue() + "\"}");

		} else {
			log.info("Update Order for price update");
			/** Update Order for price update */
			requestString = new StringBuilder("{").append(SYMBOL_REQ_STR + tradeDto.getOrdAssetSymbol() + "\",")
					.append(AMOUNT_REQ_STR + tradeDto.getRequestOrdQty() + "\",")
					.append(PRICE_REQ_STR + tradeDto.getOrdPrice() + "\",").append(ACTION_REQ_STR + action + "\",")
					.append("\"offerId\":\"" + matchingEngineOrderId + "\"}");
		}

		return requestString;
	}

	public ResponseEntity<Object> getAssetSymbols(TradeDto tradeDto, HttpServletRequest request) {
		List<Map<String, Object>> assetList;

		if (StringUtils.hasText(tradeDto.getCustomerId())
				&& (StringUtils.hasText(tradeDto.getSearchAsset()) || StringUtils.hasText(tradeDto.getAsset()))) {
			List<Map<String, Object>> searchWatchListDetails = tradeDao.getSearchWatchListDetails(
					tradeDto.getCustomerId(), tradeDto.getSearchAsset(), tradeDto.getAsset());
			assetList = searchWatchListDetails != null ? searchWatchListDetails : Collections.emptyList();
		} else if (!StringUtils.hasText(tradeDto.getCustomerId()) && StringUtils.hasText(tradeDto.getAsset())
				|| StringUtils.hasText(tradeDto.getSearchAsset())) {
			assetList = tradeDao.getActiveSymbols(tradeDto);
		} else if (StringUtils.hasText(tradeDto.getCustomerId())) {
			List<Map<String, Object>> watchListDetails = tradeDao.getWatchListDetails(tradeDto.getCustomerId());
			assetList = watchListDetails != null ? watchListDetails : Collections.emptyList();
		} else {
			assetList = tradeDao.getAllActiveSymbols();
		}

		if (!assetList.isEmpty()) {
			return ResponseEntity.ok(new CommonResponse<>(ResponseStatusEnum.SUCCESS, assetList));
		} else {
			return ResponseEntity.ok(new CommonResponse<>(400, "No records exist!", null));
		}
	}

	public ResponseEntity<Object> getBalance(String customerId, int assetSymbol, HttpServletRequest request) {
		CommonResponse<Object> commonRes = null;
		try {

			if (StringUtils.hasText(customerId) && isValidToken.isValidToken(request, customerId)) {

				if (StringUtils.hasText(customerId) && assetSymbol != 0) {

					Map<String, Object> fetchBalance = tradeDao.checkBalance(assetSymbol, customerId);
					if (!fetchBalance.isEmpty()) {
						commonRes = new CommonResponse<>(200, "Balance", fetchBalance);
					} else {
						commonRes = new CommonResponse<>(400, "No Records", null);
					}

				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
				}

			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}
		} catch (Exception e) {
			log.info("Error in getBalance:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> insertStopLimitOrder(TradeDto tradeDto, HttpServletRequest request) {
		CommonResponse<Object> commonRes = null;

		if (StringUtils.hasText(tradeDto.getCustomerId())
				&& isValidToken.isValidToken(request, tradeDto.getCustomerId())) {
			if (StringUtils.hasText(tradeDto.getCustomerId()) && (tradeDto.getOrdAssetSymbol() != 0)
					&& (StringUtils.hasText(tradeDto.getRequestOrdQty()))
					&& (StringUtils.hasText(tradeDto.getOrdSide())) && (StringUtils.hasText(tradeDto.getOrdType()))
					&& (StringUtils.hasText(tradeDto.getOrdLimitPrice()))
					&& (StringUtils.hasText(tradeDto.getOrdStopPrice()))) {

				boolean price = isValidNumberForOrder(tradeDto.getOrdLimitPrice(), tradeDto.getOrdStopPrice(),
						tradeDto.getRequestOrdQty());
				if (price) {

					Map<String, Object> assetPairDtls = tradeDao
							.fetchAssetPairDtlsUsingSymbol(tradeDto.getOrdAssetSymbol());
					log.info("assetPairDtls {}", assetPairDtls);
					String assetToSend = tradeDto.getOrdSide().equals("BUY")
							? String.valueOf(assetPairDtls.get(QUOTE_ASSET))
							: String.valueOf(assetPairDtls.get(BASE_ASSET));

					Map<String, Object> fetchBalance = tradeDao.fetchBalance(tradeDto.getCustomerId(), assetToSend);
					double requireAmount = tradeDto.getOrdSide().equals("BUY")
							? (Double.valueOf(tradeDto.getRequestOrdQty()) * Double.valueOf(tradeDto.getOrdStopPrice()))
							: (Double.valueOf(tradeDto.getRequestOrdQty()));

					log.info("requireAmount for stop limit order : {}", requireAmount);
					log.info("waltCurrentBalance for stop limit order : {}",
							Double.valueOf(String.valueOf(fetchBalance.get(WALLET_CURRENT_BALANCE))));

					if (fetchBalance.containsKey(WALLET_CURRENT_BALANCE) && requireAmount < Double
							.valueOf(String.valueOf(fetchBalance.get(WALLET_CURRENT_BALANCE)))) {

						tradeDto.setBaseAsset(assetPairDtls.get(BASE_ASSET).toString());
						tradeDto.setQuoteAsset(assetPairDtls.get(QUOTE_ASSET).toString());
						tradeDto.setOrdStatus("NEW");

						Map<String, Object> resMap = tradeDao.insertStopLimitOrder(tradeDto);
						if (!CollectionUtils.isEmpty(resMap)) {
							int returnId = (Integer) resMap.get(RETURNID);
							String message = (String) resMap.get(MESSAGE);

							if (returnId == 1) {

								Map<String, Object> response = new HashMap<>();
								response.put(ORDERID, resMap.get(ORDERID));
								commonRes = new CommonResponse<>(200, message, response);
							} else {
								commonRes = new CommonResponse<>(400, message, null);
							}
						}
					} else {
						commonRes = new CommonResponse<>(ResponseStatusEnum.INSUFFICIENTBALANCE, null);
					}
				} else {
					commonRes = new CommonResponse<>(400, "Invalid stop-price or limit-price", null);
				}

			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
			}

		} else {
			return ResponseEntity.badRequest().body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
		}

		return ResponseEntity.ok(commonRes);

	}

	public boolean isValidNumberForOrder(String... number) {
		String regex = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,8})?$";
		for (String price : number) {
			if (price == null || price.isEmpty() || !price.matches(regex)) {
				return false;
			}
		}
		return true;
	}

	public void notificationMethod(SocketNotifyDto socketNotifyDto) {
		log.info("alert message status {}", socketNotifyDto);
		/** insert alert message */
		if (StringUtils.hasText(socketNotifyDto.getCustomerId())
				&& StringUtils.hasText(socketNotifyDto.getAlertHeader())
				&& StringUtils.hasText(socketNotifyDto.getAlertMessage())) {
			log.info("alert message status if ");
			Map<String, Object> result = tradeDao.insertAlertDetails(socketNotifyDto);
			log.info("alert message status: {}", result);

			/** call socket api for notification */
			notificationService.socketApi(socketNotifyDto);

			/** call pushFcmNotification method for sending message */
			notificationService.pushFcmNotification(socketNotifyDto);
		}
	}

	public ResponseEntity<Object> insertWatchlistDetails(TradeDto tradeDto, HttpServletRequest request) {
		CommonResponse<Object> commonRes = null;

		if (StringUtils.hasText(tradeDto.getCustomerId())
				&& isValidToken.isValidToken(request, tradeDto.getCustomerId())) {

			if (StringUtils.hasText(tradeDto.getCustomerId()) && (tradeDto.getAssetPairId() != 0)
					&& (tradeDto.getAssetWatchStatus() == 1 || tradeDto.getAssetWatchStatus() == 0)) {
				Map<String, Object> resMap = tradeDao.insertWatchlistDetails(tradeDto);
				if (!CollectionUtils.isEmpty(resMap)) {
					int returnId = (Integer) resMap.get(RETURNID);
					String message = (String) resMap.get(MESSAGE);

					if (returnId == 1 || returnId == 2) {
						commonRes = new CommonResponse<>(200, message, null);
					} else {
						commonRes = new CommonResponse<>(400, message, null);
					}
				}

			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
			}

			return ResponseEntity.ok(commonRes);

		} else {
			return ResponseEntity.badRequest().body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
		}
	}

	public ResponseEntity<Object> addAssetSymbols(TradeDto tradeDto, HttpServletRequest request) {
		CommonResponse<Object> commonRes = null;

		if (StringUtils.hasText(tradeDto.getCustomerId())
				&& isValidToken.isValidToken(request, tradeDto.getCustomerId())) {
			if (StringUtils.hasText(tradeDto.getAssetCode()) && tradeDto.getSymbol() != 0) {
				String addAssetUrl = spotMatchingurl.concat("/addAsset");
				log.info("addAssetUrl of matching engine {} ", addAssetUrl);

				HttpHeaders headers = common();

				StringBuilder requestBodyBuilder = new StringBuilder();
				requestBodyBuilder.append("{").append("\"assetCode\":\"").append(tradeDto.getAssetCode()).append("\",")
						.append("\"symbol\":").append(tradeDto.getSymbol()).append("}");

				HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyBuilder.toString(), headers);

				ResponseEntity<String> addAssetApi = restTemplate.exchange(addAssetUrl, HttpMethod.POST, requestEntity,
						String.class);

				JSONObject assetResponse = new JSONObject(addAssetApi.getBody());
				log.info("assetResponse from matching engine {}", assetResponse);
				String message = assetResponse.getString("message");
				log.info("message from addasset api {}", message);
				if ("Duplicate asset can not be allowed.".equals(message)) {
					commonRes = new CommonResponse<>(400, message, null);
				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, message);
				}

			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
			}
		} else {
			return ResponseEntity.badRequest().body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
		}

		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> createBotOrder(TradeDto tradeDto) {
		CommonResponse<Object> commonRes = null;
		try {
//			ObjectMapper objectMapper = new ObjectMapper();
//			TradeDto tradeDto = objectMapper.readValue(payloadEncryptService.getDecryptValue(payload),
//					new TypeReference<TradeDto>() {
//					});
			log.info("Decrypted payload insertOrderDtls: {}", tradeDto);
			if (StringUtils.hasText(tradeDto.getCustomerId()) && (tradeDto.getOrdAssetSymbol() != 0)
					&& (StringUtils.hasText(tradeDto.getRequestOrdQty()))
					&& (StringUtils.hasText(tradeDto.getOrdSide())) && (StringUtils.hasText(tradeDto.getOrdType()))
					&& (StringUtils.hasText(tradeDto.getOrdPrice())) && (StringUtils.hasText(tradeDto.getOrdName()))) {
				/** To check balance in db before placing order */
				Map<String, Object> assetPairDtls = tradeDao
						.fetchAssetPairDtlsUsingSymbol(tradeDto.getOrdAssetSymbol());
				String assetToSend = null;
				double requireAmount = 0;
				if (tradeDto.getOrdSide().equals("BUY")) {
					assetToSend = String.valueOf(assetPairDtls.get("quoteAsset"));
					requireAmount = Double.valueOf(tradeDto.getRequestOrdQty())
							* Double.valueOf(tradeDto.getOrdPrice());
				} else {
					assetToSend = String.valueOf(assetPairDtls.get("baseAsset"));
					requireAmount = Double.valueOf(tradeDto.getRequestOrdQty());
				}
				log.info("assetToSend in createBotOrder : {}", assetToSend);
				Map<String, Object> fetchBalance = tradeDao.fetchBalance(tradeDto.getCustomerId(), assetToSend);
				log.info("fetchBalance in createBotOrder: {}", fetchBalance);
				log.info("requireAmount in createBotOrder: {}", requireAmount);
				if (fetchBalance.containsKey(WALLET_CURRENT_BALANCE)
						&& requireAmount < Double.valueOf(String.valueOf(fetchBalance.get(WALLET_CURRENT_BALANCE)))) {
					commonRes = processPlaceOrderApiResponse(assetPairDtls, tradeDto, assetToSend);
				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.INSUFFICIENTBALANCE, null);
				}
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
			}
		} catch (Exception e) {
			log.info("Error in insertOrderDtls:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

}
