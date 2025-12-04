package com.marv.arionwallet.core.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("app", "ArionWallet");
        payload.put("status", "UP");
        payload.put("time", Instant.now().toString());

        return ApiResponse.ok("Service is healthy", payload);
    }

}
