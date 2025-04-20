# cryptoTradeService
Crypto Trade Service – Microservice Description
Overview: The Crypto Trade Service is a core microservice responsible for managing cryptocurrency trades, including placing buy/sell orders, matching trades, recording transactions, and ensuring data consistency across the trading platform.

Key Responsibilities:

Accepts and processes buy and sell trade requests from users for various cryptocurrencies (e.g., BTC, ETH).

Validates trade input, checks user wallet balances, and locks funds before order placement.

Integrates with market data services for real-time price feeds and order matching engines to execute trades.

Records all trade transactions securely using JDBC (PostgreSQL) and publishes events to Kafka for downstream services (e.g., portfolio, notification).

Implements rate limiting, authentication, and authorization using Spring Security and JWT.

Exposes RESTful APIs for order creation, trade history, and trade status tracking.
