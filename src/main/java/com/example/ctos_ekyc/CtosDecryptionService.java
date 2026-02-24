package com.example.ctos_ekyc;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CtosDecryptionService {

    private final CtosCryptoService cryptoService;
    private final ObjectMapper mapper = new ObjectMapper();

    public CtosDecryptionService(CtosCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

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
}