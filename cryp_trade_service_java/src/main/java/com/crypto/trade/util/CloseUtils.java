package com.crypto.trade.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CloseUtils {
	
	private CloseUtils() {
		// to resolve sonarlint issue
		// not called
	}
	private static final Logger log = LoggerFactory.getLogger(CloseUtils.class);
	public static void callableClose(CallableStatement callableStatement) {
		if (callableStatement != null) {
			try {
				callableStatement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	public static void connectionClose(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("error in conn close -> ", e);
			}
		}
	}


}
