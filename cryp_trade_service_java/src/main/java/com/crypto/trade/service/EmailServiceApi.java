package com.crypto.trade.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.crypto.trade.dto.EmailTemplateDto;

@Service
public class EmailServiceApi {

	private static final Logger log = LoggerFactory.getLogger(EmailServiceApi.class);

	@Value("${pcl.message.url}")
	private String pclMessageUrl;

	@Autowired
	RestTemplate restTemplate;

	/** common headers */
	public HttpHeaders commonHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.put("Content-Type", Arrays.asList("application/json"));
		return headers;
	}

	public String commonEmail(EmailTemplateDto emailTemplateDto) {
		String url = pclMessageUrl + "/email-template";
		log.info("url: {}", url);
		HttpEntity<EmailTemplateDto> entity = new HttpEntity<>(emailTemplateDto, commonHeaders());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		return response.getBody();
	}
}
