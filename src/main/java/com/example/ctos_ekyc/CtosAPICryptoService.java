package com.example.ctos_ekyc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CtosAPICryptoService {

    @Value("${ctos.v3.api-key}")
    private String apiKey;

    @Value("${ctos.v3.package-name}")
    private String packageName;

    @Value("${ctos.iv}")
    private String iv;

    @Value("${ctos.md5}")
    private String md5;

    // ================= SIGNATURE =================
    public String generateSignature(String refId, String requestTime) throws Exception {

        String signatureBody =
                apiKey + md5 + packageName + refId + md5 + requestTime;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(signatureBody.getBytes(StandardCharsets.UTF_8));

        String hex = bytesToHex(hash);

        return Base64.getEncoder()
                .encodeToString(hex.getBytes(StandardCharsets.UTF_8));
    }

    // ================= ENCRYPT =================
    public String encrypt(String json) throws Exception {

        String keyString = (iv + apiKey).substring(0, 32);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKeySpec keySpec =
                new SecretKeySpec(keyString.getBytes(), "AES");

        IvParameterSpec ivSpec =
                new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted =
                cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    // ================= DECRYPT =================
    public String decrypt(String encrypted) throws Exception {

        String keyString = (iv + apiKey).substring(0, 32);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKeySpec keySpec =
                new SecretKeySpec(keyString.getBytes(), "AES");

        IvParameterSpec ivSpec =
                new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decoded = Base64.getDecoder().decode(encrypted);

        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}