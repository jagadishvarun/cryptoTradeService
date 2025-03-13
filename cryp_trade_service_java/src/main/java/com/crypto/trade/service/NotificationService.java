package com.crypto.trade.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.json.JSONObject;
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

import com.crypto.trade.dao.TradeDao;
import com.crypto.trade.dto.SocketNotifyDto;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	@Autowired
	RestTemplate restTemplate;

	@Value("${pcl.socket.data.url}")
	private String pclSocketDataUrl;

	@Value("${google.fcm.auth.key}")
	private String fcmAuthKey;

	@Autowired
	TradeDao tradeDao;

	public HttpHeaders common() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.put("Content-Type", Arrays.asList("application/json"));
		return headers;
	}

	/** Method to call notify status socket API */
	public void socketApi(SocketNotifyDto socketNotifyDto) {
		try {
			String url = pclSocketDataUrl.concat("/notifyStatus");
			log.info("socket url : {}", url);
			HttpHeaders headers = common();
			HttpEntity<SocketNotifyDto> entity = new HttpEntity<>(socketNotifyDto, headers);
			log.info("Socket api request {}", socketNotifyDto);
			ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			if (res.getStatusCodeValue() == 200 || res.getStatusCodeValue() == 201 || res.getStatusCodeValue() == 202) {
				log.info("socket api response body: {} ", res.getBody());
			}
		} catch (Exception ex) {
			log.error("Error calling socket api: ", ex);
		}
	}

	/** Method to send Notifications from server to client end android */
	public String pushFcmNotification(SocketNotifyDto socketNotifyDto) {
		String message = null;
		String fcmToken = tradeDao.getFcmToken(socketNotifyDto.getCustomerId());
		log.info("fcmToken {}", fcmToken);
		if (fcmToken != null) {
			try {

				URL url = new URL("https://fcm.googleapis.com/fcm/send");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				conn.setRequestMethod("POST");
				conn.setRequestProperty("Authorization", "key=" + fcmAuthKey);
				conn.setRequestProperty("Content-Type", "application/json");

				JSONObject data = new JSONObject();
				data.put("to", fcmToken.trim());
				JSONObject info = new JSONObject();
				info.put("title", socketNotifyDto.getAlertTitle()); // Notification title
				info.put("body", socketNotifyDto.getAlertMessage()); // Notification body
				info.put("content_available", true);
				data.put("notification", info);

				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data.toString());
				wr.flush();
				wr.close();

				int responseCode = conn.getResponseCode();
				log.info("Response Code : {}", responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				message = "Notification sent";
			} catch (Exception e) {
				log.error("Error calling pushFcmNotification api: ", e);
			}
		} else {
			message = "User doesn't has android_fcm_token";
		}
		return message;
	}

}
