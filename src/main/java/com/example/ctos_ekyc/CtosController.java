package com.example.ctos_ekyc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	private final CtosAPIService apiService;

	@PostMapping("/create")
	public ResponseEntity<Object> create(@RequestBody CreateTransactionRequest request) throws Exception {

		return ResponseEntity.ok(service.createTransaction(request));
	}

	@GetMapping("/token")
	public ResponseEntity<Object> getToken() throws Exception {
		return ResponseEntity.ok(apiService.getAccessToken());
	}

	@GetMapping("/scanOCR")
	public ResponseEntity<Object> getOCRDetails() throws Exception {

// 2️⃣ Convert NRIC Image to Base64
		String imagePath = "D:/CTOS/NRIC_IMAGE/NRIC.png";
		byte[] fileContent = Files.readAllBytes(Path.of(imagePath));
		String base64Image = Base64.getEncoder().encodeToString(fileContent);

// 3️⃣ Call OCR (Card Type 1 = MyKad Front)
		Object ocrResponse = apiService.callFrontOcr("CTOS_SUNLIFE_001", "CTOS_SUNLIFE_002", // ref_id
				"1", // card_type (Front MyKad)
				base64Image);

		return ResponseEntity.ok(ocrResponse);

	}

	public String convertImageToBase64(String imagePath) throws Exception {
		byte[] fileContent = Files.readAllBytes(Path.of(imagePath));
		return Base64.getEncoder().encodeToString(fileContent);
	}
}