package com.example.ctos_ekyc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ctos")
@RequiredArgsConstructor
public class CtosController {

    private final CtosService service;

    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody CreateTransactionRequest request) throws Exception {

        return ResponseEntity.ok(service.createTransaction(request));
    }
}