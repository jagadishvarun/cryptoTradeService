package com.crypto.trade.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;
import com.crypto.trade.dto.SocketNotifyDto;
import com.crypto.trade.common.ConstantData;
import com.crypto.trade.dto.TradeDto;
import com.crypto.trade.util.CloseUtils;

@Component
public class TradeDao {

	private static final Logger log = LoggerFactory.getLogger(TradeDao.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static final String TRADEDTO = "tradeDto {}";
	public static final String CALLABLESTATEMENT = "callableStatement:  {}";
	public static final String RESULTSET = "resultSet:  {}";
	public static final String RETURNID = "returnId";
	public static final String RETURNID_LOG = "returnId {}";
	public static final String MESSAGE = "message";
	public static final String MESSAGE_LOG = "message {}";
	public static final String ORDERID = "orderId";
	public static final String BASEASSET = "baseAsset";
	public static final String BASE_ASSET_DB = "base_asset";
	public static final String QUOTEASSET = "quoteAsset";
	public static final String QUOTE_ASSET_DB = "quote_asset";
	public static final String ASSETPAIRNAME = "assetPairName";
	public static final String PRICE = "price";
	public static final String VOLUME = "volume";
	public static final String ASSETSYMBOL = "assetSymbol";

	public Map<String, Object> insertCustOrderDtls(TradeDto tradeDto) {
		log.info(TRADEDTO, tradeDto);

		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call insert_cust_order_dtls(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getCustomerId());
				callableStatement.setString(2, tradeDto.getOrderId());
				callableStatement.setString(3, tradeDto.getBaseAsset());
				callableStatement.setString(4, tradeDto.getQuoteAsset());
				callableStatement.setInt(5, tradeDto.getOrdAssetSymbol());
				callableStatement.setDouble(6, Double.parseDouble(tradeDto.getRequestOrdQty()));
				callableStatement.setString(7, tradeDto.getOrdSide());
				callableStatement.setString(8, tradeDto.getOrdType());
				callableStatement.setDouble(9, Double.parseDouble(tradeDto.getOrdPrice()));
				callableStatement.setString(10, tradeDto.getOrdTimeInforce());
				callableStatement.setString(11, tradeDto.getOrdStatus());
				callableStatement.setString(12, tradeDto.getOrdName());
				callableStatement.setInt(13, 0);
				callableStatement.setString(14, null);
				callableStatement.setString(15, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
					String orderId = resultSet.getString(3);
					resMap.put(ORDERID, orderId);
					log.info("orderId {}", orderId);
				}
			}

		} catch (Exception e) {
			log.error("Error in insertCustOrderDtls: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public Map<String, Object> updateOrDeleteOrderDtls(TradeDto tradeDto) {

		log.info(TRADEDTO, tradeDto);

		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call update_delete_cust_order_details(?,?,?,?,?,?,?,?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getCustomerId());
				callableStatement.setString(2, tradeDto.getAssetName());
				callableStatement.setString(3, tradeDto.getOrderId());
				callableStatement.setDouble(4, Double.parseDouble(tradeDto.getRequestOrdQty()));
				callableStatement.setDouble(5, Double.parseDouble(tradeDto.getOrdPrice()));
				callableStatement.setString(6, tradeDto.getOrdTimeInforce());
				callableStatement.setString(7, tradeDto.getOrdName());
				callableStatement.setString(8, tradeDto.getOrdSide());
				callableStatement.setString(9, tradeDto.getOrdStatus());
				callableStatement.setString(10, tradeDto.getFlagValue());
				callableStatement.setInt(11, 0);
				callableStatement.setString(12, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					log.info("updateOrDeleteOrderDtls: {} {} ", resultSet.getInt(1), resultSet.getString(2));
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
				}
			}

		} catch (Exception e) {
			log.error("Error in updateOrDeleteOrderDtls: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public Map<String, Object> fetchAssetPairDtlsUsingSymbol(int symbol) {
		try {
			String sql = "SELECT base_asset, quote_asset, asset_pair_name FROM asset_pairs_master WHERE asset_symbol = ?";
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put(BASEASSET, rs.getString(BASE_ASSET_DB));
				resMap.put(QUOTEASSET, rs.getString(QUOTE_ASSET_DB));
				resMap.put(ASSETPAIRNAME, rs.getString("asset_pair_name"));
				return resMap;
			}, symbol);
		} catch (Exception e) {
			log.error("Error in fetchAssetPairDtlsUsingSymbol: ", e);
			return Collections.emptyMap();
		}
	}

	public Map<String, Object> fetchPreviousOrderDtls(String orderId) {
		try {
			String sql = "select distinct request_ord_qty,requested_ord_price from customer_order_details where exc_order_id = ?";
			log.info("sql {}", sql);
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("orderQty", rs.getDouble("request_ord_qty"));
				resMap.put("orderPrice", rs.getDouble("requested_ord_price"));
				return resMap;
			}, orderId);
		} catch (Exception e) {
			log.error("Error in fetchPreviousOrderDtls: ", e);
			return Collections.emptyMap();
		}
	}

	public Map<String, Object> fetchAssetName(String orderId) {
		try {
			String sql = "select distinct base_asset, quote_asset from CUSTOMER_ORDER_DETAILS where exc_order_id = ?";
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put(BASEASSET, rs.getString(BASE_ASSET_DB));
				resMap.put(QUOTEASSET, rs.getString(QUOTE_ASSET_DB));
				return resMap;
			}, orderId);
		} catch (Exception e) {
			log.error("Error in fetchAssetName: ", e);
			return Collections.emptyMap();
		}
	}

	public Map<String, Object> orderFullfilledPartiallyFullfilled(TradeDto tradeDto) {
		log.info(TRADEDTO, tradeDto);

		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall(
						"call insert_cust_instant_order_match_sp(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getCustomerId());
				callableStatement.setString(2, tradeDto.getOrderId());
				callableStatement.setString(3, tradeDto.getTradeId());
				callableStatement.setString(4, tradeDto.getBaseAsset());
				callableStatement.setString(5, tradeDto.getQuoteAsset());
				callableStatement.setInt(6, tradeDto.getTrdAssetSymbol());
				callableStatement.setDouble(7, tradeDto.getTrdPrice());
				callableStatement.setDouble(8, tradeDto.getTrdQty());
				callableStatement.setDouble(9, tradeDto.getReqPrice());
				callableStatement.setDouble(10, tradeDto.getReqQty());
				callableStatement.setString(11, tradeDto.getOrdType());
				callableStatement.setString(12, tradeDto.getOrdSide());
				callableStatement.setString(13, tradeDto.getOrdName());
				callableStatement.setString(14, tradeDto.getOrdTimeInforce());
				callableStatement.setInt(15, tradeDto.getStatusCode());
				callableStatement.setDouble(16, tradeDto.getRemainingQty());
				callableStatement.setInt(17, 0);
				callableStatement.setString(18, null);
				callableStatement.setString(19, null);
				callableStatement.setString(20, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
					String orderId = resultSet.getString(3);
					resMap.put(ORDERID, orderId);
					log.info("orderId {}", orderId);
					String tradeId = resultSet.getString(4);
					resMap.put("tradeId", tradeId);
					log.info("tradeId {}", tradeId);
				}
			}

		} catch (Exception e) {
			log.error("Error in orderFullfilledPartiallyFullfilled: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public Map<String, Object> updateMatchedOrders(TradeDto tradeDto) {
		log.info(TRADEDTO, tradeDto);

		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call update_matched_orders_sp(?,?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getOrderId());
				callableStatement.setString(2, tradeDto.getTradeId());
				callableStatement.setDouble(3, tradeDto.getTradeQty());
				callableStatement.setDouble(4, tradeDto.getTradePrice());
				callableStatement.setInt(5, 0);
				callableStatement.setString(6, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
				}
			}

		} catch (Exception e) {
			log.error("Error in orderFullfilledPartiallyFullfilled: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public List<Map<String, Object>> getActiveSymbols(TradeDto tradeDto) {
		List<Map<String, Object>> resList = new ArrayList<>();
		try {
			SqlRowSet rs = jdbcTemplate.queryForRowSet(
					"select asset_pair_name, base_asset, quote_asset, base_precision, quote_precision, price, \"24hchange\", volume, is_cancel_replace, is_spot_trade, is_margin_trade, is_trailing_stop, is_trade, is_buy_allowed, is_sell_allowed, asset_symbol, min_trade_amount, min_amount_movement, min_price_movement, min_order_size, max_market_order_amt, max_num_open_limit_order from last_trade_changes WHERE UPPER(QUOTE_ASSET) = UPPER(?) OR UPPER(ASSET_PAIR_NAME) ILIKE '%' || UPPER(?) || '%'",
					new Object[] { tradeDto.getAsset(), tradeDto.getSearchAsset() });

			while (rs.next()) {
				Map<String, Object> res = new HashMap<>();
				res.put(ASSETPAIRNAME, rs.getString("ASSET_PAIR_NAME"));
				res.put(BASEASSET, rs.getString(BASE_ASSET_DB));
				res.put(QUOTEASSET, rs.getString(QUOTE_ASSET_DB));
				res.put("basePrecision", rs.getInt("base_precision"));
				res.put("quotePrecision", rs.getInt("quote_precision"));
				res.put(PRICE, rs.getDouble(PRICE));
				res.put("24hChange", rs.getDouble("24hchange"));
				res.put(VOLUME, rs.getDouble(VOLUME));
				res.put("isCancelReplace", rs.getBoolean("is_cancel_replace"));
				res.put("iSpotTrade", rs.getBoolean("is_spot_trade"));
				res.put("isMarginTrade", rs.getBoolean("is_margin_trade"));
				res.put("isTrailingStop", rs.getBoolean("is_trailing_stop"));
				res.put("isTrade", rs.getBoolean("is_trade"));
				res.put("isBuyAllowed", rs.getBoolean("is_buy_allowed"));
				res.put("isSellAllowed", rs.getBoolean("is_sell_allowed"));
				res.put(ASSETSYMBOL, rs.getInt("asset_symbol"));
				res.put("minTradeAmount", formatBigDecimal(rs.getBigDecimal("min_trade_amount")));
				res.put("minAmountMovement", formatBigDecimal(rs.getBigDecimal("min_amount_movement")));
				res.put("minPriceMovement", formatBigDecimal(rs.getBigDecimal("min_price_movement")));
				res.put("minOrderSize", formatBigDecimal(rs.getBigDecimal("min_order_size")));
				res.put("maxMarketOrderAmt", rs.getString("max_market_order_amt"));
				res.put("maxNumOpenLimitOrder", rs.getString("max_num_open_limit_order"));

				resList.add(res);
			}
		} catch (Exception e) {
			log.error("Error in getActiveSymbols -> ", e);
		}
		return resList;
	}

	public List<Map<String, Object>> getAllActiveSymbols() {
		List<Map<String, Object>> resList = new ArrayList<>();
		try {
			SqlRowSet rs = jdbcTemplate.queryForRowSet(
					"select asset_pair_name, base_asset, quote_asset, base_precision, quote_precision, price, \"24hchange\", volume, is_cancel_replace, is_spot_trade, is_margin_trade, is_trailing_stop, is_trade, is_buy_allowed, is_sell_allowed, asset_symbol, min_trade_amount, min_amount_movement, min_price_movement, min_order_size, max_market_order_amt, max_num_open_limit_order from last_trade_changes",
					new Object[] {}, new int[] {});

			while (rs.next()) {
				Map<String, Object> res = new HashMap<>();
				res.put(ASSETPAIRNAME, rs.getString("ASSET_PAIR_NAME"));
				res.put(BASEASSET, rs.getString(BASE_ASSET_DB));
				res.put(QUOTEASSET, rs.getString(QUOTE_ASSET_DB));
				res.put("basePrecision", rs.getInt("base_precision"));
				res.put("quotePrecision", rs.getInt("quote_precision"));
				res.put(PRICE, rs.getString(PRICE));
				res.put("24hChange", rs.getString("24hchange"));
				res.put(VOLUME, rs.getString(VOLUME));
				res.put("isCancelReplace", rs.getBoolean("is_cancel_replace"));
				res.put("iSpotTrade", rs.getBoolean("is_spot_trade"));
				res.put("isMarginTrade", rs.getBoolean("is_margin_trade"));
				res.put("isTrailingStop", rs.getBoolean("is_trailing_stop"));
				res.put("isTrade", rs.getBoolean("is_trade"));
				res.put("isBuyAllowed", rs.getBoolean("is_buy_allowed"));
				res.put("isSellAllowed", rs.getBoolean("is_sell_allowed"));
				res.put(ASSETSYMBOL, rs.getInt("asset_symbol"));
				res.put("minTradeAmount", formatBigDecimal(rs.getBigDecimal("min_trade_amount")));
				res.put("minAmountMovement", formatBigDecimal(rs.getBigDecimal("min_amount_movement")));
				res.put("minPriceMovement", formatBigDecimal(rs.getBigDecimal("min_price_movement")));
				res.put("minOrderSize", formatBigDecimal(rs.getBigDecimal("min_order_size")));
				res.put("maxMarketOrderAmt", rs.getString("max_market_order_amt"));
				res.put("maxNumOpenLimitOrder", rs.getString("max_num_open_limit_order"));
				resList.add(res);

			}
		} catch (Exception e) {
			log.error("Error in getActiveSymbols -> ", e);
		}
		return resList;
	}

	public static String formatBigDecimal(BigDecimal value) {
		DecimalFormat decimalFormat = new DecimalFormat("0.########");
		return decimalFormat.format(value);
	}

	public Map<String, Object> fetchBalance(String customerId, String assetSymbol) {
		try {
			String sql = "SELECT CS.WALL_CURR_BAL FROM CUST_SPOT_WALLET_DTLS CS "
					+ "LEFT JOIN ASSET_MASTER AN ON CS.WALL_ASSET_ID = AN.ASSET_ID "
					+ "WHERE CS.CUSTOMER_ID = ? AND AN.ASSET_SYMBOL = ?";

			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("waltCurrentBalance", rs.getDouble("WALL_CURR_BAL"));
				return resMap;
			}, customerId, assetSymbol);
		} catch (Exception e) {
			log.error("Error in fetchBalance: ", e);
			return Collections.emptyMap();
		}
	}

	public Map<String, Object> checkBalance(int assetSymbol, String customerId) {
		Map<String, Object> resMap = new HashMap<>();
		String sql = "WITH asset_info AS (SELECT base_asset,quote_asset FROM public.asset_pairs_master WHERE asset_symbol = ?),"
				+ "base_wallet_balance AS (SELECT COALESCE(ROUND(CPWD.WALL_CURR_BAL::numeric, 3), 0) AS BASE_WALT_BALANCE FROM asset_info AI LEFT JOIN PUBLIC.ASSET_MASTER AM_BASE ON "
				+ "AM_BASE.ASSET_SYMBOL = AI.base_asset LEFT JOIN PUBLIC.CUST_SPOT_WALLET_DTLS CPWD ON CPWD.WALL_ASSET_ID = AM_BASE.ASSET_ID AND CPWD.CUSTOMER_ID = ?), "
				+ "quote_wallet_balance AS (SELECT COALESCE(ROUND(CPWD.WALL_CURR_BAL::numeric, 3), 0) AS QUOTE_WALT_BALANCE FROM asset_info AI LEFT "
				+ "JOIN PUBLIC.ASSET_MASTER am_quote ON am_quote.asset_symbol = AI.quote_asset LEFT JOIN PUBLIC.CUST_SPOT_WALLET_DTLS CPWD ON "
				+ "CPWD.WALL_ASSET_ID = am_quote.ASSET_ID AND CPWD.CUSTOMER_ID = ?) SELECT BASE_WALT_BALANCE as base_asset_balance,QUOTE_WALT_BALANCE as quote_asset_balance FROM base_wallet_balance,quote_wallet_balance";

		try {
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, assetSymbol, customerId, customerId);

			if (rowSet.next()) {
				resMap = convertRowSetToHashMap(rowSet);
			}
		} catch (Exception e) {
			log.error("Error in checkBalance: ", e);
			return Collections.emptyMap();
		}

		return resMap;
	}

	public Map<String, Object> insertStopLimitOrder(TradeDto tradeDto) {
		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call insert_cust_stop_limit_ord_dtls(?,?,?,?,?,?,?,?,?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getCustomerId());
				callableStatement.setString(2, tradeDto.getBaseAsset());
				callableStatement.setString(3, tradeDto.getQuoteAsset());
				callableStatement.setInt(4, tradeDto.getOrdAssetSymbol());
				callableStatement.setDouble(5, Double.parseDouble(tradeDto.getRequestOrdQty()));
				callableStatement.setString(6, tradeDto.getOrdSide());
				callableStatement.setString(7, tradeDto.getOrdType());
				callableStatement.setDouble(8, Double.parseDouble(tradeDto.getOrdLimitPrice()));
				callableStatement.setDouble(9, Double.parseDouble(tradeDto.getOrdStopPrice()));
				callableStatement.setString(10, tradeDto.getOrdStatus());
				callableStatement.setInt(11, 0);
				callableStatement.setString(12, null);
				callableStatement.setString(13, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					String orderId = resultSet.getString(3);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
					resMap.put("orderId", orderId);
					log.info("orderId {}", orderId);
				}
			}

		} catch (Exception e) {
			log.error("Error in insertStopLimitOrder: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public List<Map<String, Object>> getAllStopLimitOrderDetails() {
		List<Map<String, Object>> resList = new ArrayList<>();
		try {
			SqlRowSet rs = jdbcTemplate.queryForRowSet(
					"SELECT * FROM CUST_STOP_LIMIT_ORDERS WHERE ORD_STATUS = 'ACTIVE'", new Object[] {}, new int[] {});

			while (rs.next()) {
				Map<String, Object> res = new HashMap<>();
				res.put("customerId", rs.getString("CUSTOMER_ID"));
				res.put(ASSETSYMBOL, rs.getInt("ORD_ASSET_SYMBOL"));
				res.put("orderQuantity", rs.getDouble("REQUEST_ORD_QTY"));
				res.put("orderSide", rs.getString("ORD_SIDE"));
				res.put("orderType", rs.getString("ORD_TYPE"));
				res.put("orderLimitPrice", rs.getDouble("ORD_LIMIT_PRICE"));
				res.put("orderStopPrice", rs.getDouble("ORD_STOP_PRICE"));
				res.put("orderStatus", rs.getString("ORD_STATUS"));
				res.put(ORDERID, rs.getString("ORDER_ID"));
				resList.add(res);

			}
		} catch (Exception e) {
			log.error("Error in getAllStopLimitOrderDetails -> ", e);
		}
		return resList;
	}

	public int updateOrderStatusToExecuted(String orderId) {
		String sql = "UPDATE CUST_STOP_LIMIT_ORDERS SET ORD_STATUS = 'EXECUTED' WHERE ORDER_ID = ?";
		try {
			return jdbcTemplate.update(sql, orderId);
		} catch (Exception e) {
			log.error("Error updating order status to executed: ", e);
			return -1;
		}
	}

	public Map<String, Object> checkDetails(String orderId) {
		try {
			String sql = "SELECT DISTINCT REQUEST_ORD_QTY, REQUESTED_ORD_PRICE FROM CUSTOMER_ORDER_DETAILS WHERE ORDER_ID = ?";
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("currentQty", rs.getDouble("REQUEST_ORD_QTY"));
				resMap.put("currentPrice", rs.getDouble("REQUESTED_ORD_PRICE"));
				return resMap;
			}, orderId);
		} catch (Exception e) {
			log.error("Error in checkDetails: ", e);
			return Collections.emptyMap();
		}
	}

	public static Map<String, Object> convertRowSetToHashMap(SqlRowSet rowSet) {
		Map<String, Object> resultMap = new HashMap<>();

		SqlRowSetMetaData metaData = rowSet.getMetaData();
		int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			String columnName = metaData.getColumnName(i);
			Object columnValue = rowSet.getObject(i);

			String camelCaseColumnName = convertToCamelCase(columnName);

			resultMap.put(camelCaseColumnName, columnValue);
		}

		return resultMap;
	}

	private static String convertToCamelCase(String columnName) {
		StringBuilder result = new StringBuilder();

		boolean capitalizeNext = false;

		for (char c : columnName.toCharArray()) {
			if (c == '_') {
				capitalizeNext = true;
			} else {
				if (capitalizeNext) {
					result.append(Character.toUpperCase(c));
					capitalizeNext = false;
				} else {
					result.append(Character.toLowerCase(c));
				}
			}
		}

		return result.toString();
	}

	public Map<String, Object> fetchPrice(int assetSymbol) {
		Map<String, Object> resMap = new HashMap<>();
		String sql = "SELECT MP.ASK_PRICE, MP.BID_PRICE FROM MARKET_PRICE_DETAILS MP "
				+ "INNER JOIN ASSET_PAIRS_MASTER AP ON MP.ASSET_PAIR = AP.ASSET_PAIRS WHERE AP.ASSET_SYMBOL =  ? "
				+ "ORDER BY MRKT_PRIC_DTL_ID DESC FETCH FIRST 1 ROW only";

		try {
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, assetSymbol);

			if (rowSet.next()) {
				resMap.put("askPrice", rowSet.getDouble("ASK_PRICE"));
				resMap.put("bidPrice", rowSet.getDouble("BID_PRICE"));

			}
		} catch (Exception e) {
			log.error("Error in fetchPrice: ", e);
			return Collections.emptyMap();
		}
		return resMap;
	}

	public Map<String, Object> fetchDetails() {
		Map<String, Object> resMap = new HashMap<>();

		String sql = "SELECT ASK_SPREAD, BID_SPREAD FROM SPREAD_MASTER WHERE ID = 2";

		try {
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);

			if (rowSet.next()) {
				resMap.put("askSpread", rowSet.getDouble("ASK_SPREAD"));
				resMap.put("bidSpread", rowSet.getDouble("BID_SPREAD"));
			}
		} catch (Exception e) {
			log.error("Error in fetchDetails: ", e);
			return Collections.emptyMap();
		}

		return resMap;
	}

	public Map<String, Object> insertWatchlistDetails(TradeDto tradeDto) {
		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call cust_watch_list_dtls_sp(?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getCustomerId());
				callableStatement.setInt(2, tradeDto.getAssetPairId());
				callableStatement.setInt(3, tradeDto.getAssetWatchStatus());
				callableStatement.setInt(4, 0);
				callableStatement.setString(5, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
				}
			}

		} catch (Exception e) {
			log.error("Error in insertWatchlistDetails: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public Map<String, Object> insertAlertDetails(SocketNotifyDto socketNotifyDto) {
		log.info(TRADEDTO, socketNotifyDto);
		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {
			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call insert_cust_alert_det_sp(?,?,?,?,?)");
				conn.setAutoCommit(false);
				callableStatement.setString(1, socketNotifyDto.getCustomerId());
				callableStatement.setString(2, socketNotifyDto.getAlertMessage());
				callableStatement.setString(3, socketNotifyDto.getAlertHeader());
				callableStatement.setInt(4, 0);
				callableStatement.setString(5, null);
				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
				}
			}
		} catch (Exception e) {
			log.error("Error in insertAlertDetails: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public String getFcmToken(String customerId) {
		String fcmToken = null;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(
				"SELECT ANDROID_FCM_TOKEN FROM customer_regi_info WHERE CUSTOMER_ID =? ", new Object[] { customerId },
				new int[] { Types.VARCHAR });
		if (rs.next()) {
			fcmToken = rs.getString("ANDROID_FCM_TOKEN");
		}
		return fcmToken;
	}

	public List<Map<String, Object>> getWatchListDetails(String customerId) {
		List<Map<String, Object>> watchListDetailsMap = new ArrayList<>();
		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				throw new IllegalStateException(ConstantData.DATA_IS_NULL);
			}

			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call GET_CUST_WATCHLIST_DTLS_SF(?) }")) {

				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.OTHER);
				callableStatement.setString(2, customerId);
				callableStatement.execute();

				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {
					if (!resultSet.isBeforeFirst()) {
						log.info("No watchlist found");
					} else {
						while (resultSet.next()) {
							Map<String, Object> watchListDetails = convertResultSetToHashMap(resultSet);
							watchListDetailsMap.add(watchListDetails);
						}
					}
				}
			}
		} catch (SQLException e) {
			log.error("SQL Error in getWatchListDetails: ", e);
		} catch (Exception e) {
			log.error("Error in getWatchListDetails: ", e);
		}
		return watchListDetailsMap;
	}

	public static Map<String, Object> convertResultSetToHashMap(ResultSet resultSet) throws SQLException {
		Map<String, Object> resultMap = new HashMap<>();

		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			String columnName = metaData.getColumnName(i);
			Object columnValue = resultSet.getObject(i);

			String camelCaseColumnName = convertToCamelCase(columnName);
			String stringValue = columnValue != null ? columnValue.toString() : null;

			if ("volume".equalsIgnoreCase(columnName) || "price".equalsIgnoreCase(columnName)) {
				resultMap.put(camelCaseColumnName, stringValue);
			} else if (columnValue instanceof BigDecimal) {
				int decimalPrecision = ((BigDecimal) columnValue).scale();
				double value = ((BigDecimal) columnValue).doubleValue();

				String formattedValue = String.format("%." + decimalPrecision + "f", value);
				formattedValue = formatDecimal(formattedValue);
			//	log.info("formattedValue: {} {}", columnName, formattedValue);
				resultMap.put(camelCaseColumnName, formattedValue);

			} else {
				resultMap.put(camelCaseColumnName, columnValue);
			}
		}

		return resultMap;

	}

	public static String formatDecimal(String value) {
		if (value == null) {
			return null;
		}

		// Trim trailing zeros after the decimal point
		int index = value.indexOf('.');
		if (index >= 0) {
			int length = value.length();
			for (int i = length - 1; i > index + 2; i--) {
				if (value.charAt(i) != '0') {
					break;
				}
				length--;
			}
			value = value.substring(0, length);
		}

		return value;
	}

	public List<Map<String, Object>> getSearchWatchListDetails(String customerId, String searchAsset, String asset) {
		List<Map<String, Object>> watchListDetails = new ArrayList<>();
		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				throw new IllegalStateException(ConstantData.DATA_IS_NULL);
			}

			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call GET_CUST_WATCHLIST_BY_SEARCH_SF(?, ?, ?) }")) {

				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.OTHER);

				// Set parameters based on conditions
				if (customerId != null && !customerId.isEmpty() && searchAsset != null && !searchAsset.isEmpty()) {
					callableStatement.setString(2, customerId);
					callableStatement.setString(3, searchAsset);
					callableStatement.setNull(4, Types.VARCHAR); // Set asset as null
				} else if (customerId != null && !customerId.isEmpty() && asset != null && !asset.isEmpty()) {
					callableStatement.setString(2, customerId);
					callableStatement.setNull(3, Types.VARCHAR); // Set searchAsset as null
					callableStatement.setString(4, asset);
				}

				callableStatement.execute();

				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {
					if (!resultSet.isBeforeFirst()) {
						log.info("No watchlist found");
					} else {
						while (resultSet.next()) {
							Map<String, Object> watchList = convertResultSetToHashMap(resultSet);
							watchListDetails.add(watchList);
						}
					}
				}
			}
		} catch (SQLException e) {
			log.error("SQL Error in getSearchWatchListDetails: ", e);
		} catch (Exception e) {
			log.error("Error in getSearchWatchListDetails: ", e);
		}
		return watchListDetails;
	}

	public int changeStatus(String customerId, String orderId) {
		return jdbcTemplate.update(
				"UPDATE CUST_STOP_LIMIT_ORDERS SET ORD_STATUS = 'INACTIVE' WHERE CUSTOMER_ID = ? AND ORDER_ID = ?",
				new Object[] { customerId, orderId }, new int[] { Types.VARCHAR, Types.VARCHAR });

	}

	public int updateStopOrder(String customerId, String orderId, String requestOrdQty, String ordLimitPrice,
			String ordStopPrice) {

		return jdbcTemplate.update(
				"UPDATE CUST_STOP_LIMIT_ORDERS SET REQUEST_ORD_QTY = COALESCE(?, REQUEST_ORD_QTY), ORD_LIMIT_PRICE = COALESCE(?, ORD_LIMIT_PRICE), ORD_STOP_PRICE = COALESCE(?, ORD_STOP_PRICE) WHERE CUSTOMER_ID = ? AND ORDER_ID = ?",
				new Object[] { Double.parseDouble(requestOrdQty), Double.parseDouble(ordLimitPrice),
						Double.parseDouble(ordStopPrice), customerId, orderId },
				new int[] { Types.DOUBLE, Types.DOUBLE, Types.DOUBLE, Types.VARCHAR, Types.VARCHAR });
	}

	public String getAssetNameByAssetId(int assetId) {
		String assetSymbol = "";
		SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT ASSET_SYMBOL FROM ASSET_MASTER  WHERE ASSET_ID  = ? ",
				new Object[] { assetId }, new int[] { Types.INTEGER });
		if (rs.next()) {
			assetSymbol = rs.getString("ASSET_SYMBOL");
		}
		return assetSymbol;
	}

	public int getMaxDesPrecisionValue(String asset) {
		int assetPrecision = 0;
		SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT ASSET_PERCISION FROM ASSET_MASTER WHERE ASSET_SYMBOL = ?",
				new Object[] { asset }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			assetPrecision = rs.getInt("ASSET_PERCISION");
		}
		return assetPrecision;
	}

	public String getBalance(int assetId, String customerId) {
		String balance = "";
		SqlRowSet rs = jdbcTemplate.queryForRowSet("WITH asset_info AS ("
				+ "    SELECT ASSET_SYMBOL, ASSET_ID FROM public.ASSET_MASTER WHERE ASSET_ID = ?" + "),"
				+ "base_wallet_balance AS ("
				+ "    SELECT COALESCE(ROUND((CPWD.WALL_CURR_BAL + CFWD.WALL_CURR_BAL) ::numeric, 3), 0) AS WALT_BALANCE "
				+ "    FROM ASSET_MASTER AM  "
				+ "    INNER JOIN PUBLIC.CUST_SPOT_WALLET_DTLS CPWD ON CPWD.WALL_ASSET_ID = AM.ASSET_ID AND CPWD.CUSTOMER_ID = ?"
				+ "    INNER JOIN PUBLIC.CUST_FUND_WALLET_DTLS CFWD ON CFWD.WALL_ASSET_ID = AM.ASSET_ID AND CFWD.CUSTOMER_ID = ?"
				+ "    WHERE AM.ASSET_ID = ?" + ")" + "SELECT WALT_BALANCE AS asset_balance FROM base_wallet_balance",
				new Object[] { assetId, customerId, customerId, assetId },
				new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.INTEGER });
		if (rs.next()) {
			balance = rs.getString("asset_balance");
		}
		return balance;
	}

	public Map<String, Object> getContactDetails(String customerId) {
		Map<String, Object> resMap = new HashMap<>();
		SqlRowSet rs = jdbcTemplate.queryForRowSet(
				"SELECT CPI.CUST_EMAIL, TRIM(COALESCE(CPI.FIRST_NAME,'') || ' ' || COALESCE(CPI.MIDDLE_NAME,'') || ' ' || COALESCE(CPI.LAST_NAME,'')) AS CUST_NAME, CRI.PHONE_CODE, CRI.PHONE_NUMBER, CRI.COUNTRY_CODE "
						+ "FROM CUSTOMER_PERSONAL_INFO CPI "
						+ "LEFT JOIN CUSTOMER_REGI_INFO CRI ON CPI.CUSTOMER_ID = CRI.CUSTOMER_ID "
						+ "WHERE CPI.CUSTOMER_ID = ?",
				new Object[] { customerId }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			resMap.put("custEmail", rs.getString("CUST_EMAIL"));
			resMap.put("custName", rs.getString("CUST_NAME"));
			resMap.put("countryPhone", rs.getString("PHONE_CODE"));
			resMap.put("mobileNo", rs.getString("PHONE_NUMBER"));
			resMap.put("countryCode", rs.getString("COUNTRY_CODE"));
		}
		return resMap;
	}

	public double spotBalance(String customerId, String assetSymbol) {
		double spotBalance = 0;

		SqlRowSet rs = jdbcTemplate.queryForRowSet(
				"SELECT WALL_CURR_BAL AS SPOT_BALANCE FROM CUST_SPOT_WALLET_DTLS WHERE CUSTOMER_ID = ? AND WALL_ASSET_ID = (SELECT ASSET_ID FROM asset_master WHERE ASSET_SYMBOL = ?)",
				customerId, assetSymbol);

		if (rs.next()) {
			spotBalance = rs.getDouble("SPOT_BALANCE");
		}
		return spotBalance;
	}

	public Map<String, Object> updateOrDeleteStopOrderDtls(TradeDto tradeDto) {
		log.info(TRADEDTO, tradeDto);

		Map<String, Object> resMap = new HashMap<>();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			DataSource ds = jdbcTemplate.getDataSource();
			if (ds != null) {
				conn = ds.getConnection();
				callableStatement = conn.prepareCall("call update_delete_stp_lmt_ord_dtls_sp(?,?,?,?,?,?,?,?)");

				conn.setAutoCommit(false);
				callableStatement.setString(1, tradeDto.getCustomerId());
				callableStatement.setString(2, tradeDto.getOrderId());
				callableStatement.setDouble(3, Double.parseDouble(tradeDto.getRequestOrdQty()));
				callableStatement.setDouble(4, Double.parseDouble(tradeDto.getOrdStopPrice()));
				callableStatement.setDouble(5, Double.parseDouble(tradeDto.getOrdLimitPrice()));
				callableStatement.setString(6, tradeDto.getOrdStatus());
				callableStatement.setInt(7, 0);
				callableStatement.setString(8, null);

				log.info(CALLABLESTATEMENT, callableStatement);
				ResultSet resultSet = callableStatement.executeQuery();
				log.info(RESULTSET, resultSet);
				if (resultSet.next()) {
					log.info("updateOrDeleteStopOrderDtls: {} {} ", resultSet.getInt(1), resultSet.getString(2));
					conn.commit();
					int returnId = resultSet.getInt(1);
					String message = resultSet.getString(2);
					resMap.put(RETURNID, returnId);
					log.info(RETURNID_LOG, returnId);
					resMap.put(MESSAGE, message);
					log.info(MESSAGE_LOG, message);
				}
			}

		} catch (Exception e) {
			log.error("Error in updateOrDeleteOrderDtls: ", e);
			return Collections.emptyMap();
		} finally {
			CloseUtils.callableClose(callableStatement);
			CloseUtils.connectionClose(conn);
		}
		return resMap;
	}

	public Map<String, Object> getOrderDetails(String orderId) {

		Map<String, Object> resMap = new HashMap<>();
		SqlRowSet rs = jdbcTemplate.queryForRowSet(
				"SELECT CRI.EMAIL_ID AS EMAIL_ID, TRIM(COALESCE(CPI.FIRST_NAME,'') || ' ' || COALESCE(CPI.MIDDLE_NAME,'') || ' ' || COALESCE(CPI.LAST_NAME,'')) AS CUST_NAME "
						+ "FROM CUSTOMER_REGI_INFO CRI INNER JOIN CUSTOMER_PERSONAL_INFO CPI ON CRI.CUSTOMER_ID = CPI.CUSTOMER_ID "
						+ "WHERE CRI.CUSTOMER_ID IN (SELECT CUSTOMER_ID FROM CUSTOMER_ORDER_DETAILS WHERE EXC_ORDER_ID = ?)",
				new Object[] { orderId }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			resMap.put("emailId", rs.getString("EMAIL_ID"));
			resMap.put("custName", rs.getString("CUST_NAME"));
		}
		return resMap;
	}

	public Map<String, Object> getCustomerId(String orderId) {

		Map<String, Object> resMap = new HashMap<>();
		SqlRowSet rs = jdbcTemplate.queryForRowSet(
				"SELECT CUSTOMER_ID, ORD_SIDE FROM CUSTOMER_ORDER_DETAILS WHERE EXC_ORDER_ID = ?",
				new Object[] { orderId }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			resMap.put("customerId", rs.getString("CUSTOMER_ID"));
			resMap.put("ordSide", rs.getString("ORD_SIDE"));
		}
		return resMap;
	}

	public double getRemQty(String customerId, String orderId) {
		SqlRowSet rs = jdbcTemplate.queryForRowSet(
				"SELECT ORD_REMAINING_QTY FROM CUSTOMER_ORDER_DETAILS WHERE CUSTOMER_ID = ? AND EXC_ORDER_ID = ?",
				new Object[] { customerId, orderId }, new int[] { Types.VARCHAR, Types.VARCHAR });
		if (rs.next()) {
			return rs.getDouble("ORD_REMAINING_QTY");
		}
		return -1;
	}

}
