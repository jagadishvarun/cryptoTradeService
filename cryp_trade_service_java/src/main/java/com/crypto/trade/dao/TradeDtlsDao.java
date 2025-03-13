package com.crypto.trade.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import com.crypto.trade.common.ConstantData;
import com.crypto.trade.dto.TradeOrderDtlsDto;
import com.crypto.trade.dto.TransLedgerDtlsDto;

@Component
public class TradeDtlsDao {
	private static final Logger log = LoggerFactory.getLogger(TradeDtlsDao.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public static final String ROWCOUNT = "rowCount";

	public static Map<String, Object> convertResultSetToHashMap(ResultSet resultSet) throws SQLException {
		Map<String, Object> resultMap = new HashMap<>();

		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			String columnName = metaData.getColumnName(i);
			Object columnValue = resultSet.getObject(i);

			String camelCaseColumnName = convertToCamelCase(columnName);

			if (columnValue instanceof BigDecimal) {
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

	public Map<String, Object> getCustomerAllOrders(TradeOrderDtlsDto tradeOrderDtlsDto) {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				log.error(ConstantData.DATA_IS_NULL);
				resultMap.put(ConstantData.ERROR, ConstantData.DATA_IS_NULL);
				return resultMap;
			}

			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call get_cust_all_orders(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")) {

				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.INTEGER);
				callableStatement.registerOutParameter(2, Types.OTHER);
				callableStatement.setString(3, tradeOrderDtlsDto.getCustomerId());
				callableStatement.setString(4, tradeOrderDtlsDto.getOrdStatus());
				callableStatement.setString(5, tradeOrderDtlsDto.getOrdSide());
				callableStatement.setString(6, tradeOrderDtlsDto.getFromDate());
				callableStatement.setString(7, tradeOrderDtlsDto.getToDate());
				callableStatement.setString(8, tradeOrderDtlsDto.getBaseAsset());
				callableStatement.setString(9, tradeOrderDtlsDto.getQuoteAsset());
				callableStatement.setInt(10, tradeOrderDtlsDto.getPageNo());
				callableStatement.setInt(11, tradeOrderDtlsDto.getPageSize());

				callableStatement.execute();

				int rowCount = callableStatement.getInt(1);
				resultMap.put(ROWCOUNT, rowCount);
				ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
				List<Map<String, Object>> customerList = new ArrayList<>();
				while (resultSet.next()) {
					Map<String, Object> customerAllList = convertResultSetToHashMap(resultSet);
					customerList.add(customerAllList);
				}

				if (customerList.isEmpty()) {
					resultMap.put(ConstantData.MESSAGE, "No customerList found ");
				} else {
					resultMap.put("customerList", customerList);
				}
			}
		} catch (SQLException e) {
			log.error("Error in getCustomerAllOrders: ", e);
			resultMap.put(ConstantData.ERROR, "Error in getCustomerAllOrders: " + e.getMessage());
		}

		return resultMap;
	}

	public Map<String, Object> getCustomerAllTrades(TradeOrderDtlsDto tradeOrderDtlsDto) {
		Map<String, Object> resultMap = new HashMap<>();

		Connection connection = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;

		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				log.error(ConstantData.DATA_IS_NULL);
				resultMap.put(ConstantData.ERROR, ConstantData.DATA_IS_NULL);
				return resultMap;
			}

			connection = dataSource.getConnection();
			callableStatement = connection
					.prepareCall("{ ? = call get_cust_all_trade_dtls(?, ?, ?, ?, ?, ?, ?, ?, ?) }");
			connection.setAutoCommit(false);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.registerOutParameter(2, Types.OTHER);
			callableStatement.setString(3, tradeOrderDtlsDto.getCustomerId());
			callableStatement.setString(4, tradeOrderDtlsDto.getTradeSide());
	//		callableStatement.setString(5, tradeOrderDtlsDto.getTradeStatus());
			callableStatement.setInt(5, tradeOrderDtlsDto.getPageNo());
			callableStatement.setInt(6, tradeOrderDtlsDto.getPageSize());
			callableStatement.setString(7, tradeOrderDtlsDto.getFromDate());
			callableStatement.setString(8, tradeOrderDtlsDto.getToDate());
			callableStatement.setString(9, tradeOrderDtlsDto.getBaseAsset());
			callableStatement.setString(10, tradeOrderDtlsDto.getQuoteAsset());

			callableStatement.execute();

			int rowCount = callableStatement.getInt(1);
			resultMap.put(ROWCOUNT, rowCount);
 			resultSet = (ResultSet) callableStatement.getObject(2);
			List<Map<String, Object>> customerList = new ArrayList<>();
			while (resultSet.next()) {
				Map<String, Object> customerAllList = convertResultSetToHashMap(resultSet);
				customerList.add(customerAllList);
			}

			if (customerList.isEmpty()) {
				resultMap.put(ConstantData.MESSAGE, "No customerList found ");
			} else {
				resultMap.put("customerList", customerList);
			}
		} catch (SQLException e) {
			log.error("Error in getCustomerAllTrades: ", e);
			resultMap.put(ConstantData.ERROR, "Error in getCustomerAllTrades: " + e.getMessage());
		} finally {
			// Close resources
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					log.error("Error closing ResultSet: ", e);
				}
			}
			if (callableStatement != null) {
				try {
					callableStatement.close();
				} catch (SQLException e) {
					log.error("Error closing CallableStatement: ", e);
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					log.error("Error closing Connection: ", e);
				}
			}
		}

		return resultMap;
	}

	public List<Map<String, Object>> getWalletDetails(String customerId, int assetId) {
		List<Map<String, Object>> walletDetailsList = new ArrayList<>();
		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				throw new IllegalStateException(ConstantData.DATA_IS_NULL);
			}

			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call get_cust_wallet_dtls_sp(?, ?) }")) {
				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.OTHER);
				callableStatement.setString(2, customerId);
				callableStatement.setInt(3, assetId);

				callableStatement.execute();

				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {
					while (resultSet.next()) {
						Map<String, Object> walletDetails = convertResultSetToHashMap(resultSet);
						walletDetailsList.add(walletDetails);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error in getWalletDetails: ", e);
		}

		return walletDetailsList;
	}

	public Map<String, Object> getCustomerTransLedger(TradeOrderDtlsDto tradeOrderDtlsDto) {

		try {
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("GET_TRAN_LEDGER_SP")
					.returningResultSet("OUT_TRANS_DATA", BeanPropertyRowMapper.newInstance(TransLedgerDtlsDto.class));
			SqlParameterSource in = new MapSqlParameterSource()
					.addValue("P_CUSTOMER_ID", tradeOrderDtlsDto.getCustomerId())
					.addValue("P_ASSET_TYPE", tradeOrderDtlsDto.getAssetType())
					.addValue("P_TRANSACTION_ID", tradeOrderDtlsDto.getTransactionId())
					.addValue("P_TYPE", tradeOrderDtlsDto.getType())
					.addValue("P_DAYS_OR_YEAR", tradeOrderDtlsDto.getDaysOrYear())
					.addValue("P_ASSET", tradeOrderDtlsDto.getAsset())
					.addValue("P_STATUS", tradeOrderDtlsDto.getStatus())
					.addValue("p_PAGE_NO", tradeOrderDtlsDto.getPageNo())
					.addValue("p_PAGE_SIZE", tradeOrderDtlsDto.getPageSize());
			return simpleJdbcCall.execute(in);

		} catch (Exception e) {
			log.error("error in getCustomerTransLedger ->", e);
		}
		return Collections.emptyMap();

	}

	public Map<String, Object> getOpenOrder(TradeOrderDtlsDto tradeOrderDtlsDto) {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				log.error(ConstantData.DATA_IS_NULL);
				resultMap.put(ConstantData.ERROR, ConstantData.DATA_IS_NULL);
				return resultMap;
			}

			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call public.get_cust_all_open_orders_sp(?, ?, ?, ?, ?, ?, ?) }")) {

				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.INTEGER);
				callableStatement.registerOutParameter(2, Types.OTHER);
				callableStatement.setString(3, tradeOrderDtlsDto.getCustomerId());
				callableStatement.setInt(4, tradeOrderDtlsDto.getPageNo());
				callableStatement.setInt(5, tradeOrderDtlsDto.getPageSize());
				callableStatement.setString(6, tradeOrderDtlsDto.getOrdType());
				callableStatement.setString(7, tradeOrderDtlsDto.getOrdSide());
				callableStatement.setString(8, tradeOrderDtlsDto.getAssetPair());

				callableStatement.execute();

				int rowCount = callableStatement.getInt(1);
				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(2)) {
					List<Map<String, Object>> orderList = new ArrayList<>();

					while (resultSet.next()) {
						Map<String, Object> orderAllList = convertResultSetToHashMap(resultSet);
						orderList.add(orderAllList);
					}

					if (orderList.isEmpty()) {
						resultMap.put(ConstantData.MESSAGE, "No orderList found ");
					} else {
						resultMap.put("orderList", orderList);
						resultMap.put(ROWCOUNT, rowCount);
					}
				}
			}
		} catch (SQLException e) {
			log.error("Error in getOpenOrder: ", e);
			resultMap.put(ConstantData.ERROR, "Error in getOpenOrder: " + e.getMessage());
		}

		return resultMap;
	}


	public List<Map<String, Object>> getSpotWalletDetails(String customerId, Integer assetId) {
		List<Map<String, Object>> spotWalletDetailsList = new ArrayList<>();
		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				throw new IllegalStateException(ConstantData.DATA_IS_NULL);
			}
			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call GET_SPOT_WALLET_DETAILS_SF(?, ?) }")) {
				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.OTHER);
				callableStatement.setString(2, customerId);
				if (assetId != null) {
					callableStatement.setInt(3, assetId);
				} else {
					callableStatement.setNull(3, Types.INTEGER);
				}

				callableStatement.execute();

				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {

					while (resultSet.next()) {
						Map<String, Object> spotWalletDetails = convertResultSetToHashMap(resultSet);
						spotWalletDetailsList.add(spotWalletDetails);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error in getSpotWalletDetails: ", e);
		}
		return spotWalletDetailsList;
	}

	
	public Map<String, Object> getCustomerOrderDetails(String customerId, String orderId) {
	    Map<String, Object> resultMap = new HashMap<>();
	    List<Map<String, Object>> orderDetailsList = new ArrayList<>();
	    List<Map<String, Object>> tradeDetailsList = new ArrayList<>();
	    List<Map<String, Object>> orderHistoryList = new ArrayList<>(); 
	    
	    try {
	        DataSource dataSource = jdbcTemplate.getDataSource();
	        if (dataSource == null) {
	            throw new IllegalStateException(ConstantData.DATA_IS_NULL);
	        }

	        try (Connection connection = dataSource.getConnection();
	             CallableStatement callableStatement = connection.prepareCall("{ ? = call GET_CUST_ORDER_DTLS_SP(?, ?, ?, ?) }")) {

	            connection.setAutoCommit(false);
	            callableStatement.registerOutParameter(1, Types.OTHER);
	            callableStatement.registerOutParameter(2, Types.OTHER);
	            callableStatement.registerOutParameter(3, Types.OTHER);
	            callableStatement.setString(4, customerId);
	            callableStatement.setString(5, orderId);

	            callableStatement.execute();

	            // Process both result sets
	            try (ResultSet resultSet1 = (ResultSet) callableStatement.getObject(1);
	                 ResultSet resultSet2 = (ResultSet) callableStatement.getObject(2);
	                 ResultSet resultSet3 = (ResultSet) callableStatement.getObject(3)) {

	                // Process the first result set (order details)
	                if (resultSet1.next()) {
	                    Map<String, Object> orderDetails = convertResultSetToHashMap(resultSet1);
	                    orderDetailsList.add(orderDetails);
	                }

	                // Process the second result set (trade details)
	                while (resultSet2.next()) {
	                    Map<String, Object> tradeDetails = convertResultSetToHashMap(resultSet2);
	                    tradeDetailsList.add(tradeDetails);
	                }
	                
	                // Process the third result set (order history)
	                while (resultSet3.next()) {
	                    Map<String, Object> orderHistory = convertResultSetToHashMap(resultSet3);
	                    orderHistoryList.add(orderHistory);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        log.error("SQL Error in getCustomerOrderDetails: ", e);
	    } catch (Exception e) {
	        log.error("Error in getCustomerOrderDetails: ", e);
	    }

	    // Populate the resultMap
	    resultMap.put("orderDetails", orderDetailsList);
	    resultMap.put("tradeDetails", tradeDetailsList);
	    resultMap.put("orderHistory", orderHistoryList);
	    
	    return resultMap;
	}

	public List<Map<String, Object>> getAssetBySearch(String asset) {
		List<Map<String, Object>> assetList = new ArrayList<>();
		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				throw new IllegalStateException(ConstantData.DATA_IS_NULL);
			}
			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call GET_ASSETS_BY_SEARCH_SF(?) }")) {
				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.OTHER);
				if (asset != null) {
					callableStatement.setString(2, asset);
				} else {
					callableStatement.setNull(2, Types.VARCHAR);
				}

				callableStatement.execute();

				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {

					while (resultSet.next()) {
						Map<String, Object> assetDetails = convertResultSetToHashMap(resultSet);
						assetList.add(assetDetails);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error in getAssetBySearch: ", e);
		}
		return assetList;
	}

	public List<Map<String, Object>> getAssetPairsSearch(String searchString) {
		List<Map<String, Object>> assetPairsList = new ArrayList<>();
		try {
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				throw new IllegalStateException(ConstantData.DATA_IS_NULL);
			}
			try (Connection connection = dataSource.getConnection();
					CallableStatement callableStatement = connection
							.prepareCall("{ ? = call GET_ASSET_PAIRS_BY_SEARCH_SF(?) }")) {
				connection.setAutoCommit(false);
				callableStatement.registerOutParameter(1, Types.OTHER);
				if (searchString != null) {
					callableStatement.setString(2, searchString);
				} else {
					callableStatement.setNull(2, Types.VARCHAR);
				}

				callableStatement.execute();

				try (ResultSet resultSet = (ResultSet) callableStatement.getObject(1)) {

					while (resultSet.next()) {
						Map<String, Object> assetDetails = convertResultSetToHashMap(resultSet);
						assetPairsList.add(assetDetails);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error in getAssetPairsSearch: ", e);
		}
		return assetPairsList;
	}

}
