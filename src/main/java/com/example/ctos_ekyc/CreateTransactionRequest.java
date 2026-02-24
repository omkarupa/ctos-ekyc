package com.example.ctos_ekyc;

import lombok.Data;

@Data
public class CreateTransactionRequest {

    private String refId;
    private String documentName;
    private String documentNumber;
    private String documentType;
}