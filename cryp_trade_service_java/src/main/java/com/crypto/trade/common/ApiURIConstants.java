package com.crypto.trade.common;

public class ApiURIConstants {

	private ApiURIConstants() {

	}

	public static final String INSERT_CUST_ORDER_DTLS = "/v1/create-order";

	public static final String UPDATE_DELETE_ORDER_DTLS = "/v1/update-delete-order";

	public static final String GET_CUSTOMER_ALL_ORDERS_DTLS = "/v1/get-customer-all-orders";

	public static final String GET_CUSTOMER_ALL_TRADE_DTLS = "/v1/get-customer-all-trades";

	public static final String GET_WALLET_DETAILS = "/v1/get-wallet-details/{customerId}/{assetId}";

	public static final String GET_CUSTOMER_TRANS_LEDGER_DTLS = "/v1/get-trans-ledger-dtls";

	public static final String GET_ASSET_SYMBOLS = "/v1/get-asset-symbols";

	public static final String GET_OPEN_ORDERS = "/v1/get-open-orders";

	public static final String GET_BALANCE = "/v1/get-balance/{customerId}/{assetSymbol}";

	public static final String GET_SPOT_WALLET_DETAILS_BY_TYPE = "/v1/get-spot-wallet-details-by-type/{customerId}/{assetId}";
	
	public static final String GET_SPOT_WALLET_DETAILS = "/v1/get-spot-wallet-details/{customerId}";
	
	public static final String INSERT_STOP_LIMIT_ORDER = "/v1/create-stop-limit-order";
	
	public static final String INSERT_WATCHLIST_DTLS = "/v1/create-watchlist";
	
	public static final String GET_CUSTOMER_ORDER_DTLS = "/v1/get-customer-orders/{customerId}/{orderId}";
	
	public static final String GET_ASSET_BY_SEARCH = "/v1/get-asset-by-search/{asset}";
	
	public static final String GET_ASSET_BY_SEARCH_DTLS = "/v1/get-asset-by-search";
	
	public static final String GET_ASSET_PAIRS_SEARCH = "/v1/get-asset-pairs-search/{searchString}";
	
	public static final String GET_ASSET_PAIRS_SEARCH_DTLS = "/v1/get-asset-pairs-search";
	
	public static final String ADD_ASSET_SYMBOLS = "/v1/add-asset-symbols";
	
	public static final String CREATE_BOT_ORDER = "/v1/create-bot-order"; //for blockchain
		
}
