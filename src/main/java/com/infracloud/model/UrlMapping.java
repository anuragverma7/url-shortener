package com.infracloud.model;

public record UrlMapping(String originalUrl,
        String shortKey,
        String domain,
        long createdAt,
        int redirectCount) {
	public UrlMapping incrementRedirectCount() {
        return new UrlMapping(originalUrl, shortKey, domain, createdAt, redirectCount + 1);
    }
}
