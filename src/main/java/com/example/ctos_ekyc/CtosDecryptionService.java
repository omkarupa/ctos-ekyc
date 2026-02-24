package com.example.ctos_ekyc;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CtosDecryptionService {

    private final CtosCryptoService cryptoService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CtosAPICryptoService apiCryptoService;

   
    /**
     * Decrypts the encrypted CTOS response
     * 
     * @param encryptedResponse The response containing "data" field
     * @return Map<String, Object> decrypted data
     * @throws Exception
     */
    public Map<String, Object> decryptResponse(CtosEncryptedResponse encryptedResponse) throws Exception {
        if (encryptedResponse == null || encryptedResponse.getData() == null) {
            throw new IllegalArgumentException("Encrypted response or data is null");
        }

        // Decrypt the "data" field
        String decryptedJson = cryptoService.decrypt(encryptedResponse.getData().toString());

        // Convert JSON string to Map
        Map<String, Object> decryptedMap = mapper.readValue(decryptedJson, Map.class);

        return decryptedMap;
    }
    
    public Map<String, Object> decryptAPIResponse(CtosEncryptedResponse encryptedResponse) throws Exception {
        if (encryptedResponse == null || encryptedResponse.getData() == null) {
            throw new IllegalArgumentException("Encrypted response or data is null");
        }

        // Decrypt the "data" field
        String decryptedJson = apiCryptoService.decrypt(encryptedResponse.getData().toString());

        // Convert JSON string to Map
        Map<String, Object> decryptedMap = mapper.readValue(decryptedJson, Map.class);

        return decryptedMap;
    }
}