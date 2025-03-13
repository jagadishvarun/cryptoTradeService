package com.crypto.trade.configuration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.crypto.trade.dao.GetTokenDao;

@Service
public class ValidateTokenByUsername {
	
	private static final Logger log = LoggerFactory.getLogger(ValidateTokenByUsername.class);
	
	@Autowired
	GetTokenDao getTokenDao;

	public boolean isValidToken(HttpServletRequest request, String username) {
		if (StringUtils.hasText(username)) {
			String auth = request.getHeader("Authorization");
			if (StringUtils.hasText(auth)) {
				String requestToken = String.valueOf(request.getHeader("Authorization").split(" ")[1]);
				String userName = getTokenDao.getUsernameByToken(requestToken);
				if ( userName != null) {	
					return username.equalsIgnoreCase(userName);
				} else {
					log.info("Token could not be validated!");
					return false;
				}
			} else {
				log.info("Authorization header can not be null!");
				return false;
			}
		} else {
			log.info("username name must be filled!");
			return false;
		}

	}
}
