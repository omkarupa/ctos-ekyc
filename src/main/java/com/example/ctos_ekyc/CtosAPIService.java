package com.example.ctos_ekyc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CtosAPIService {

	private final RestTemplate restTemplate;

	private final CtosAPICryptoService apiCryptoService;
	private final ObjectMapper mapper = new ObjectMapper();
	private final CtosDecryptionService decryptionService;

	@Value("${ctos.api.url}")
	private String baseUrl;

	@Value("${ctos.api-version}")
	private String apiVersion;

	@Value("${ctos.v3.api-key}")
	private String apiKey;

	@Value("${ctos.v3.package-name}")
	private String packageName;

	@Value("${ctos.v3.encodeKey}")
	private String encodeKey;

	public Object getAccessToken() throws Exception {

		Map<String, Object> body = new HashMap<>();
		body.put("device_model", "Xiaomi11tpro");
		body.put("device_brand", "Xiaomi");
		body.put("device_imei", "341231235");
		body.put("device_mac", "08:62:66:66:18:b9");
		body.put("api_key", apiKey);
		body.put("package_name", packageName);

		String json = mapper.writeValueAsString(body);

		String encrypted = apiCryptoService.encrypt(json);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("data", encrypted);
		requestBody.put("api_key", apiKey);

		String finalJson = mapper.writeValueAsString(requestBody);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(finalJson, headers);

		String url = baseUrl + "/v2/auth/get-token";

		ResponseEntity<CtosEncryptedResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
				CtosEncryptedResponse.class);

		if (!response.getBody().isSuccess()) {
			throw new RuntimeException("CTOS request failed");
		}

		// Decrypt response
		Map<String, Object> decryptedData = decryptionService.decryptAPIResponse(response.getBody());

		// Now you can return the decrypted data instead of raw response
		return decryptedData;

	}

	public Object callFrontOcr(String onboardingId, String refId, String cardType, String base64Image)
			throws Exception {

		String date = LocalDate.now().toString();

		String requestTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // match your test
// OR
// LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		String extra = generateExtra(apiKey, refId, encodeKey, date, packageName);

		Map<String, Object> ocr = new HashMap<>();
		ocr.put("extra", extra);
		ocr.put("onboarding_id", onboardingId);
		ocr.put("ref_id", refId);
		ocr.put("card_type", cardType);
		ocr.put("request_time", requestTime);
		ocr.put("material_check", true);
		ocr.put("label_check", true);
		ocr.put("id_image", base64Image);

		Map<String, Object> wrapper = new HashMap<>();
		wrapper.put("ocr", ocr);

		String encrypted = apiCryptoService.encrypt(mapper.writeValueAsString(wrapper));

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("data", encrypted);
		requestBody.put("api_key", apiKey);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, Object> tokenObj = (Map<String, Object>) getAccessToken();
		String accessToken = tokenObj.containsKey("access_token") ?  tokenObj.get("access_token").toString() : null;
		headers.set("Authorization", "access_token " + accessToken);

		HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);

		String url = baseUrl + "/v3/webservices/ocr-scanner";

		ResponseEntity<CtosEncryptedResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
				CtosEncryptedResponse.class);

		if (!response.getBody().isSuccess()) {
			return response;
		}

		// Decrypt response
		Map<String, Object> decryptedData = decryptionService.decryptAPIResponse(response.getBody());

		// Now you can return the decrypted data instead of raw response
		return decryptedData;
	}

	private String generateExtra(String apiKey, String refId, String encodeKey, String date, String packageName)
			throws Exception {

// Step 1: Create raw string
		String raw = apiKey + refId + encodeKey + date + packageName;

// Step 2: SHA256 -> HEX string (like CryptoJS.toString())
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			hexString.append(String.format("%02x", b));
		}

		String hex = hexString.toString();

// Step 3: Convert HEX string to UTF-8 bytes
		byte[] utf8Bytes = hex.getBytes(StandardCharsets.UTF_8);

// Step 4: Base64 encode those bytes
		return Base64.getEncoder().encodeToString(utf8Bytes);
	}

}
