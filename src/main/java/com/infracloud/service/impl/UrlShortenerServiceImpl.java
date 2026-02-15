package com.infracloud.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.infracloud.dto.ShortenResponse;
import com.infracloud.dto.TopDomainResponse;
import com.infracloud.model.UrlMapping;
import com.infracloud.service.UrlShortenerService;
@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

	private final Map<String, UrlMapping> urlStore = new ConcurrentHashMap<>();

    // In-memory storage: originalUrl → shortKey  (for idempotency)
    private final Map<String, String> urlToShortKey = new ConcurrentHashMap<>();

    // Domain → count of unique shortened URLs from that domain
    private final Map<String, Integer> domainCount = new ConcurrentHashMap<>();

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int KEY_LENGTH = 7;

    @Override
    public ShortenResponse shortenUrl(String originalUrl) {
        // Normalize URL a bit
        String normalizedUrl = normalizeUrl(originalUrl);

        // Check if already shortened
        String existingKey = urlToShortKey.get(normalizedUrl);
       
        if (existingKey != null) {
            UrlMapping mapping = urlStore.get(existingKey);
            return new ShortenResponse(
                    buildShortUrl(existingKey),
                    mapping.originalUrl()
            );
        }

        // Generate short key
        String shortKey = generateShortKey(normalizedUrl);

        // Extract domain
        String domain = extractDomain(normalizedUrl);

        // Save mapping
        UrlMapping mapping = new UrlMapping(
                normalizedUrl,
                shortKey,
                domain,
                System.currentTimeMillis(),
                0
        );
        if(existingKey ==null) {
       	 urlStore.put(shortKey, mapping);
            urlToShortKey.put(normalizedUrl, shortKey);
       }
        urlStore.put(shortKey, mapping);
        urlToShortKey.put(normalizedUrl, shortKey);

        // Update domain stats (count unique URLs per domain)
        domainCount.merge(domain, 1, Integer::sum);

        return new ShortenResponse(buildShortUrl(shortKey), normalizedUrl);
    }

    @Override
    public String getOriginalUrl(String shortKey) {
        UrlMapping mapping = urlStore.get(shortKey);
        if (mapping == null) {
            return null;
        }

        // Update redirect count (for potential future use)
        UrlMapping updated = mapping.incrementRedirectCount();
        urlStore.put(shortKey, updated);

        return mapping.originalUrl();
    }

    @Override
    public List<TopDomainResponse> getTopDomains(int limit) {
        return domainCount.entrySet().stream()
                .map(e -> new TopDomainResponse(e.getKey(), e.getValue()))
                .sorted()
                .limit(limit)
                .toList();
    }

    // Very simple base62 shortening (not collision-proof in production)
    private String generateShortKey(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes());
            long value = ((long) (digest[0] & 0xFF) << 56) |
                         ((long) (digest[1] & 0xFF) << 48) |
                         ((long) (digest[2] & 0xFF) << 40) |
                         ((long) (digest[3] & 0xFF) << 32) |
                         ((long) (digest[4] & 0xFF) << 24) |
                         ((long) (digest[5] & 0xFF) << 16) |
                         ((long) (digest[6] & 0xFF) << 8)  |
                         (digest[7] & 0xFF);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < KEY_LENGTH; i++) {
                sb.append(BASE62_CHARS.charAt((int) (Long.remainderUnsigned(value, 62))));
                value /= 62;
            }
            return sb.reverse().toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    private String buildShortUrl(String key) {
        return "http://localhost:8080/" + key;
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return "unknown";

            // Remove www. prefix and take the main domain
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // Take only the domain part (remove subdomains)
            int lastDot = host.lastIndexOf('.');
            if (lastDot > 0) {
                int secondLastDot = host.lastIndexOf('.', lastDot - 1);
                if (secondLastDot > 0) {
                    return host.substring(secondLastDot + 1);
                }
            }
            return host;
        } catch (URISyntaxException e) {
            return "invalid";
        }
    }

}
