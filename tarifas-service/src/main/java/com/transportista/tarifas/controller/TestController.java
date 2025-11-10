package com.transportista.tarifas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from Tarifas Service!");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Tarifas Service is running!");
    }
}

