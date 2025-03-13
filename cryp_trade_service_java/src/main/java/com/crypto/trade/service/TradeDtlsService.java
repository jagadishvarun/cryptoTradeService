package com.crypto.trade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.crypto.trade.aes.PayloadEncryptService;
import com.crypto.trade.common.CommonResponse;
import com.crypto.trade.common.ResponseStatusEnum;
import com.crypto.trade.configuration.ValidateTokenByUsername;
import com.crypto.trade.dao.TradeDtlsDao;
import com.crypto.trade.dto.TradeOrderDtlsDto;
import com.crypto.trade.dto.TransLedgerDtlsDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TradeDtlsService {

	@Autowired
	TradeDtlsDao tradeDtlsDao;

	@Autowired
	PayloadEncryptService payloadEncryptService;

	@Autowired
	ValidateTokenByUsername isValidToken;

	public static final String CUSTOMERLIST = "customerList";
	public static final String ROWCOUNT = "rowCount";

	public ResponseEntity<Object> getCustomerAllOrders(TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {

		CommonResponse<Object> commonRes = null;
		try {

//			ObjectMapper objectMapper = new ObjectMapper();
//			TradeOrderDtlsDto tradeDto = objectMapper.readValue(payloadEncryptService.getDecryptValue(payload),
//					new TypeReference<TradeOrderDtlsDto>() {
//					});
			// log.info("Decrypted payload getCustomerAllOrders: {}", tradeOrderDtlsDto);

			if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
					&& isValidToken.isValidToken(request, tradeOrderDtlsDto.getCustomerId())) {

				if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
						&& StringUtils.hasText(tradeOrderDtlsDto.getOrdStatus())
						&& StringUtils.hasText(tradeOrderDtlsDto.getOrdSide())
						&& StringUtils.hasText(tradeOrderDtlsDto.getFromDate())
						&& StringUtils.hasText(tradeOrderDtlsDto.getToDate())
						&& StringUtils.hasText(tradeOrderDtlsDto.getBaseAsset())
						&& StringUtils.hasText(tradeOrderDtlsDto.getQuoteAsset()) && tradeOrderDtlsDto.getPageNo() != 0
						&& tradeOrderDtlsDto.getPageSize() != 0) {

					Map<String, Object> customerList = tradeDtlsDao.getCustomerAllOrders(tradeOrderDtlsDto);

					if (customerList.isEmpty() || !customerList.containsKey(CUSTOMERLIST)) {
						commonRes = new CommonResponse<>(400, "No customerList found ", null);
					} else {

						Map<String, Object> responseMap = new HashMap<>();
						responseMap.put(ROWCOUNT, customerList.get(ROWCOUNT));
						responseMap.put(CUSTOMERLIST, customerList.get(CUSTOMERLIST));

						commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, responseMap);
					}

				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
				}

			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}

		} catch (Exception e) {
			log.info("Error in getCustomerAllOrders:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> getCustomerAllTrades(TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {

		CommonResponse<Object> commonRes = null;
		try {

//			ObjectMapper objectMapper = new ObjectMapper();
//			TradeOrderDtlsDto tradeDto = objectMapper.readValue(payloadEncryptService.getDecryptValue(payload),
//					new TypeReference<TradeOrderDtlsDto>() {
//					});
			log.info("Decrypted payload getCustomerAllTrades: {}", tradeOrderDtlsDto);

			if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
					&& isValidToken.isValidToken(request, tradeOrderDtlsDto.getCustomerId())) {

				if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
						&& StringUtils.hasText(tradeOrderDtlsDto.getTradeSide())
						&& tradeOrderDtlsDto.getPageNo() != 0 && tradeOrderDtlsDto.getPageSize() != 0
						&& StringUtils.hasText(tradeOrderDtlsDto.getFromDate())
						&& StringUtils.hasText(tradeOrderDtlsDto.getToDate())
						&& StringUtils.hasText(tradeOrderDtlsDto.getBaseAsset())
						&& StringUtils.hasText(tradeOrderDtlsDto.getQuoteAsset())) {

					Map<String, Object> tradeData = tradeDtlsDao.getCustomerAllTrades(tradeOrderDtlsDto);

					if (tradeData.isEmpty() || !tradeData.containsKey(CUSTOMERLIST)) {
						commonRes = new CommonResponse<>(400, "No customerList found ", null);
					} else {

						Map<String, Object> responseMap = new HashMap<>();
						responseMap.put(ROWCOUNT, tradeData.get(ROWCOUNT));
						responseMap.put(CUSTOMERLIST, tradeData.get(CUSTOMERLIST));
						commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, responseMap);
					}

				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
				}

			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}

		} catch (Exception e) {
			log.info("Error in getCustomerAllTrades:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> getWalletDetails(String customerId, int assetId) {
		CommonResponse<Object> commonRes;

		try {
			List<Map<String, Object>> walletDetailsList = tradeDtlsDao.getWalletDetails(customerId, assetId);

			if (!CollectionUtils.isEmpty(walletDetailsList)) {
				commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, walletDetailsList);
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.NODATA, null);
			}
		} catch (Exception e) {
			log.error("Error in getWalletDetails -> ", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}

		return ResponseEntity.ok(commonRes);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<Object> getCustomerTransLedger(TradeOrderDtlsDto tradeOrderDtlsDto,
			HttpServletRequest request) {

		CommonResponse<Object> commonRes = null;
		try {

//			ObjectMapper objectMapper = new ObjectMapper();
//			TradeOrderDtlsDto tradeDto = objectMapper.readValue(payloadEncryptService.getDecryptValue(payload),
//					new TypeReference<TradeOrderDtlsDto>() {
//					});
			log.info("Decrypted payload getCustomerTransLedger: {}", tradeOrderDtlsDto);
			if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
					&& isValidToken.isValidToken(request, tradeOrderDtlsDto.getCustomerId())) {
				if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
						&& StringUtils.hasText(tradeOrderDtlsDto.getAssetType())
						&& StringUtils.hasText(tradeOrderDtlsDto.getTransactionId())
						&& StringUtils.hasText(tradeOrderDtlsDto.getType())
						&& StringUtils.hasText(tradeOrderDtlsDto.getDaysOrYear())
						&& StringUtils.hasText(tradeOrderDtlsDto.getAsset())
						&& StringUtils.hasText(tradeOrderDtlsDto.getStatus()) && tradeOrderDtlsDto.getPageNo() != 0
						&& tradeOrderDtlsDto.getPageSize() != 0 && tradeOrderDtlsDto.getRowCount() != 0) {

					Map<String, Object> transDtls = tradeDtlsDao.getCustomerTransLedger(tradeOrderDtlsDto);

					if (!CollectionUtils.isEmpty(transDtls)) {
						List<TransLedgerDtlsDto> transLedgerDtls = (List) transDtls.get("OUT_TRANS_DATA");
						Map<String, Object> resMap = new HashMap<>();
						resMap.put(ROWCOUNT, transDtls.get("P_ROW_COUNT"));
						resMap.put("transLedgerDtls", transLedgerDtls);
						commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, resMap);

						commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, resMap);

					} else {
						commonRes = new CommonResponse<>(400, "Record(s) not exists !", null);
					}
				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);
				}

			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}

		} catch (Exception e) {
			log.info("Error in getCustomerTransLedger:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}
		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> getOpenOrders(TradeOrderDtlsDto tradeOrderDtlsDto, HttpServletRequest request) {
		CommonResponse<Object> commonRes = null;
		try {

			if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId())
					&& isValidToken.isValidToken(request, tradeOrderDtlsDto.getCustomerId())) {

				if (StringUtils.hasText(tradeOrderDtlsDto.getCustomerId()) && tradeOrderDtlsDto.getPageNo() != 0
						&& StringUtils.hasText(tradeOrderDtlsDto.getOrdType())
						&& StringUtils.hasText(tradeOrderDtlsDto.getAssetPair())
						&& StringUtils.hasText(tradeOrderDtlsDto.getOrdSide())
						&& tradeOrderDtlsDto.getPageSize() != 0) {

					Map<String, Object> orderList = tradeDtlsDao.getOpenOrder(tradeOrderDtlsDto);

					if (orderList.isEmpty() || !orderList.containsKey("orderList")) {
						commonRes = new CommonResponse<>(400, "No orderList found ", null);
					} else {
						commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, orderList);
					}
				} else {
					commonRes = new CommonResponse<>(ResponseStatusEnum.VALIDATIONERROR, null);

				}
			} else {
				return ResponseEntity.badRequest()
						.body(new CommonResponse<>(ResponseStatusEnum.INVALID_JWT_TOKEN, null));
			}

		} catch (Exception e) {
			log.info("Error in getOpenOrder:", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}

		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> getSpotWalletDetails(String customerId, Integer assetId) {
		CommonResponse<Object> commonRes = null;

		try {
			List<Map<String, Object>> spotWalletDetailsList = tradeDtlsDao.getSpotWalletDetails(customerId, assetId);

			if (!CollectionUtils.isEmpty(spotWalletDetailsList)) {
				commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, spotWalletDetailsList);
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.NODATA, null);
			}
		} catch (Exception e) {
			log.error("Error in getSpotWalletDetails -> ", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}

		return ResponseEntity.ok(commonRes);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getCustomerOrderDetails(String customerId, String orderId) {
		CommonResponse<Object> commonRes = null;
		try {
			String matchingEngineOrderId = orderId.replaceAll("\\D", ""); // Remove non-digits
			matchingEngineOrderId = matchingEngineOrderId.replaceFirst("^0+", ""); // Remove leading zeros
			log.info("matchingEngineOrderId {}", matchingEngineOrderId);

			// Retrieve both order and trade details
			Map<String, Object> orderAndTradeDetails = tradeDtlsDao.getCustomerOrderDetails(customerId,
					matchingEngineOrderId);
			log.info("orderAndTradeDetails {} ", orderAndTradeDetails);
			
			

			if (orderAndTradeDetails != null && !orderAndTradeDetails.isEmpty()) {
				// Construct the response map
				Object orderDetailsObject = null;
				List<Map<String, Object>> orderDetailsList = (List<Map<String, Object>>) orderAndTradeDetails
						.get("orderDetails");
				
				List<Map<String, Object>> tradeDetailsList = (List<Map<String, Object>>) orderAndTradeDetails
						.get("tradeDetails");
				log.info("tradeDetailsList {} ", tradeDetailsList);
			

				if (!orderDetailsList.isEmpty()) {
					// Assuming you want to pick the first item from the list as the orderDetails
					// object
					orderDetailsObject = (orderDetailsList.get(0));
					log.info("orderDetailsObject {} ", orderDetailsObject);
					
					 formatOrdRemainingQty(orderDetailsObject);
				}

				// Format tradeFee in tradeDetailsList
				formatTradeFeeInTradeDetails(tradeDetailsList);

				Map<String, Object> resMap = new HashMap<>();
				resMap.put("orderDetails", orderDetailsObject);
				resMap.put("tradeDetails", orderAndTradeDetails.get("tradeDetails"));
				resMap.put("orderHistory", orderAndTradeDetails.get("orderHistory"));


				commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, resMap);
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.NODATA, null);
			}

		} catch (Exception e) {
			log.error("Error in getCustomerOrderDetails -> ", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}

		return ResponseEntity.ok(commonRes);
	}

	@SuppressWarnings("unchecked")
	private void formatOrdRemainingQty(Object orderDetailsObject) {
	    if (orderDetailsObject instanceof Map) {
	        Map<String, Object> orderDetailsMap = (Map<String, Object>) orderDetailsObject;
	        Object ordRemainingQtyObj = orderDetailsMap.get("ordRemainingQty");
	        if (ordRemainingQtyObj instanceof BigDecimal) {
	            BigDecimal ordRemainingQty = (BigDecimal) ordRemainingQtyObj;
	            DecimalFormat decimalFormat = new DecimalFormat("0.########");
	            String formattedOrdRemainingQty = decimalFormat.format(ordRemainingQty);
	            orderDetailsMap.put("ordRemainingQty", formattedOrdRemainingQty);
	        }
	    }
	}

	private void formatTradeFeeInTradeDetails(List<Map<String, Object>> tradeDetailsList) {
		for (Map<String, Object> tradeDetails : tradeDetailsList) {
			Object tradeFeeObj = tradeDetails.get("tradeFee");
			if (tradeFeeObj instanceof Number) {
				// Convert tradeFee to BigDecimal to preserve precision
				BigDecimal tradeFee = new BigDecimal(tradeFeeObj.toString());
				// Set desired precision and formatting for tradeFee
				DecimalFormat decimalFormat = new DecimalFormat("0.########");
				decimalFormat.setRoundingMode(RoundingMode.DOWN); // Specify rounding mode as needed
				String formattedTradeFee = decimalFormat.format(tradeFee);
				// Update tradeFee in the map
				tradeDetails.put("tradeFee", formattedTradeFee);
			}
		}
	}

	public ResponseEntity<Object> getAssetBySearch(String asset) {
		CommonResponse<Object> commonRes = null;

		try {
			List<Map<String, Object>> assetList = tradeDtlsDao.getAssetBySearch(asset);
			if (!CollectionUtils.isEmpty(assetList)) {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("assetList", assetList);
				commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, resMap);
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.NODATA, null);
			}

		} catch (Exception e) {
			log.error("Error in getAssetBySearch -> ", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}

		return ResponseEntity.ok(commonRes);
	}

	public ResponseEntity<Object> getAssetPairsSearch(String searchString) {
		CommonResponse<Object> commonRes = null;

		try {
			List<Map<String, Object>> assetPairsList = tradeDtlsDao.getAssetPairsSearch(searchString);
			if (!CollectionUtils.isEmpty(assetPairsList)) {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("assetPairsList", assetPairsList);
				commonRes = new CommonResponse<>(ResponseStatusEnum.SUCCESS, resMap);
			} else {
				commonRes = new CommonResponse<>(ResponseStatusEnum.NODATA, null);
			}

		} catch (Exception e) {
			log.error("Error in getAssetPairsSearch -> ", e);
			commonRes = new CommonResponse<>(ResponseStatusEnum.EXCEPTIONERROR, null);
		}

		return ResponseEntity.ok(commonRes);
	}

}
