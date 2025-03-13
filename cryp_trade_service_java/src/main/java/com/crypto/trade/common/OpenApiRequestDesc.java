package com.crypto.trade.common;

public class OpenApiRequestDesc {

	public static final String INSERT_CUST_ORDER_DTLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"requestOrdQty\": {{Integer}}\",\r\n" + "\t" + "    \"ordSide\": \"{{String}}\",\r\n" + "\t"
			+ "    \"ordType\": \"{{String}}\",\r\n" + "\t" + "    \"ordPrice\": {{Integer}}\",\r\n" + "\t"
			+ "    \"ordName\": \"{{String}}\",\r\n" + "\t" + "    \"ordAssetSymbol\":\"{{String}}\"\r\n" + "\t" + "}";

	public static final String UPDATE_DELETE_ORDER_DTLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"requestOrdQty\": {{Integer}}\",\r\n" + "\t" + "    \"ordSide\": \"{{String}}\",\r\n" + "\t"
			+ "    \"ordType\": \"{{String}}\",\r\n" + "\t" + "    \"ordPrice\": {{Integer}}\",\r\n" + "\t"
			+ "    \"ordName\": \"{{String}}\",\r\n" + "\t" + "    \"ordAssetSymbol\":\"{{String}}\",\r\n" + "\t"
			+ "    \"orderId\":\"{{String}}\",\r\n" + "\t" + "    \"flagValue\":\"{{String}}\"\r\n" + "\t" + "}";

	public static final String GET_ASSET_SYMBOLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t\r\n" + "\t\r\n" + "\t{}" + "\t\r\n" + "\t\r\n" + "\t{\r\n" + "\t\t\"customerId\": \"{{String}}\",\r\n"
			+ "\t}" + "\t\r\n" + "\t\r\n" + "\t{\r\n" + "\t\t\"customerId\": \"{{String}}\",\r\n" + "\t" + "\r\n"
			+ "\t\t\"asset\": \"{{String}}\",\r\n" + "\t" + "\t\tOR\r\n" + "\t\t\"searchAsset\": \"{{String}}\",\r\n"
			+ "\t}";

	public static final String INSERT_STOP_LIMIT_ORDER_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"requestOrdQty\": {{Integer}}\",\r\n" + "\t" + "    \"ordSide\": \"{{String}}\",\r\n" + "\t"
			+ "    \"ordType\": \"{{String}}\",\r\n" + "\t" + "    \"ordLimitPrice\": {{Integer}}\",\r\n" + "\t"
			+ "    \"ordStopPrice\": {{Integer}}\",\r\n" + "\t" + "    \"ordAssetSymbol\":\"{{String}}\"\r\n" + "\t"
			+ "}";

	public static final String INSERT_WATCHLIST_DTLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"assetpairId\": {{Integer}}\",\r\n" + "\t" + "    \"assetWatchStatus\":{{Integer}}\"\r\n" + "\t"
			+ "}";

	public static final String GET_CUSTOMER_ALL_ORDERS_DTLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"ordStatus\": \"{{String}}\",\r\n" + "\t" + "    \"ordSide\": \"{{String}}\",\r\n" + "\t"
			+ "    \"fromDate\": \"{{String}}\",\r\n" + "\t" + "    \"toDate\": \"{{String}}\",\r\n" + "\t"
			+ "    \"baseAsset\": \"{{String}}\",\r\n" + "\t" + "    \"quoteAsset\":\"{{String}}\",\r\n" + "\t"
			+ "    \"pageNo\":{{Integer}}\",\r\n" + "\t" + "    \"pageSize\":{{Integer}}\"\r\n" + "\t" + "}";

	public static final String GET_CUSTOMER_ALL_TRADE_DTLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"tradeSide\": \"{{String}}\",\r\n" + "\t" + "    \"tradeStatus\": \"{{String}}\",\r\n" + "\t"
			+ "    \"ordSide\": \"{{String}}\",\r\n" + "\t" + "    \"fromDate\": \"{{String}}\",\r\n" + "\t"
			+ "    \"toDate\": \"{{String}}\",\r\n" + "\t" + "    \"baseAsset\": \"{{String}}\",\r\n" + "\t"
			+ "    \"quoteAsset\":\"{{String}}\",\r\n" + "\t" + "    \"pageNo\":{{Integer}}\",\r\n" + "\t"
			+ "    \"pageSize\":{{Integer}}\"\r\n" + "\t" + "}";

	public static final String GET_CUSTOMER_TRANS_LEDGER_DTLS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"assetType\": \"{{String}}\",\r\n" + "\t" + "    \"transactionId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"type\": \"{{String}}\",\r\n" + "\t" + "    \"daysOrYear\": \"{{String}}\",\r\n" + "\t"
			+ "    \"asset\": \"{{String}}\",\r\n" + "\t" + "    \"status\": \"{{String}}\",\r\n" + "\t"
			+ "    \"pageNo\":{{Integer}}\",\r\n" + "\t" + "    \"pageSize\":{{Integer}}\",\r\n" + "\t"
			+ "    \"rowCount\":{{Integer}}\"\r\n" + "\t" + "}";

	public static final String GET_OPEN_ORDERS_DESC = "The request body for this API is as mentioned in the JSON below:"
			+ "\t" + "\r\n" + "\r\n" + "\t" + "{\r\n" + "\t" + "    \"customerId\": \"{{String}}\",\r\n" + "\t"
			+ "    \"pageNo\": \"{{String}}\",\r\n" + "\t" + "    \"ordType\": \"{{String}}\",\r\n" + "\t"
			+ "    \"assetPair\": \"{{String}}\",\r\n" + "\t" + "    \"ordSide\": \"{{String}}\",\r\n" + "\t"
			+ "    \"pageSize\":{{Integer}}\",\r\n" + "\t" + "}";
}
