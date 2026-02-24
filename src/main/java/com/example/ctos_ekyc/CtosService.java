package com.example.ctos_ekyc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class CtosService {

    private final RestTemplate restTemplate;
    private final CtosCryptoService cryptoService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ctos.url}")
    private String baseUrl;

    @Value("${ctos.api-version}")
    private String apiVersion;

    @Value("${ctos.api-key}")
    private String apiKey;

    @Value("${ctos.package-name}")
    private String packageName;

    public Object createTransaction(CreateTransactionRequest req) throws Exception {

        String requestTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String signature =
                cryptoService.generateSignature(req.getRefId(), requestTime);

        Map<String, Object> body = new HashMap<>();
        body.put("ref_id", req.getRefId());
        body.put("document_name", req.getDocumentName());
        body.put("document_number", req.getDocumentNumber());
        body.put("document_type", req.getDocumentType());
        body.put("platform", "Web");
        body.put("response_url", "https://your-response-url.com");
        body.put("backend_url", "https://your-backend-url.com");
        body.put("callback_mode", "2");
        body.put("request_time", requestTime);
        body.put("signature", signature);
        body.put("api_key", apiKey);
        body.put("package_name", packageName);

        String json = mapper.writeValueAsString(body);

        String encryptedBody = cryptoService.encrypt(json);
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("data", encryptedBody);
        requestBody.put("api_key", apiKey);
        
        String finalJson = mapper.writeValueAsString(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(finalJson, headers);

        String url = baseUrl + "/" + apiVersion + "/gateway/create-transaction";

        ResponseEntity<CtosEncryptedResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        CtosEncryptedResponse.class
                );

        if (!response.getBody().isSuccess()) {
            throw new RuntimeException("CTOS request failed");
        }

        // Decrypt response
        Map<String, Object> decryptedData = new CtosDecryptionService(cryptoService)
                .decryptResponse(response.getBody());

        // Now you can return the decrypted data instead of raw response
        return decryptedData;

       
    }
}