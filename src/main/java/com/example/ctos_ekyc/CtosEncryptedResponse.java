package com.example.ctos_ekyc;

import lombok.Data;

@Data
public class CtosEncryptedResponse {
    private boolean success;
    private Object data;
}