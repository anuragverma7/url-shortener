package com.infracloud.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.infracloud.dto.ShortenRequest;
import com.infracloud.dto.ShortenResponse;
import com.infracloud.dto.TopDomainResponse;
import com.infracloud.service.UrlShortenerService;

import jakarta.validation.Valid;

@RestController
public class UrlController {

	private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.shortenUrl(request.url());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        String originalUrl = service.getOriginalUrl(shortKey);
        if (originalUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/metrics/top-domains")
    public ResponseEntity<List<TopDomainResponse>> getTopDomains() {
        List<TopDomainResponse> top = service.getTopDomains(3);
        return ResponseEntity.ok(top);
    }
}
