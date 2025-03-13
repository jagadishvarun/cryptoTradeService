package com.crypto.trade.dao;

import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class GetTokenDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public String getJwtToken(String userName) {
		String token = null;
		SqlRowSet rs = jdbcTemplate.queryForRowSet("select jat.TOKEN from JWT_AUTH_TOKEN jat where USER_NAME = ?",
				new Object[] { userName }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			token = rs.getString("TOKEN");
		}
		return token;
	}
	
	public String getUsernameByToken(String token) {
		String username = null;
		SqlRowSet rs = jdbcTemplate.queryForRowSet("select user_name from JWT_AUTH_TOKEN jat where token = ?",
				new Object[] { token }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			username = rs.getString("user_name");
		}
		return username;
	}

	public String getMobileNoByCustId(String customerId) {
		String phoneNo = null;
		SqlRowSet rs = jdbcTemplate.queryForRowSet("select MOBILE_NUMBER from CUSTOMER_BASIC_INFO where CUSTOMER_ID = ?",
				new Object[] { customerId }, new int[] { Types.VARCHAR });
		if (rs.next()) {
			phoneNo = rs.getString("MOBILE_NUMBER");
		}
		return phoneNo;
	}

}
