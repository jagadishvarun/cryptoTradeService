package com.crypto.trade.configuration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class CustomCorsFilter implements Filter {

	static final String ORIGIN = "Origin";

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		response.setHeader("Access-Control-Allow-Origin", request.getHeader(ORIGIN));
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS");
		response.addHeader("Access-Control-Allow-Headers",
				"Authorization, X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Max-Age", "3600");
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) {
		// To initialize
	}

	@Override
	public void destroy() {
		// To destroy
	}

}
