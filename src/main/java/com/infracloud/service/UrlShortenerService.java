package com.infracloud.service;

import java.util.List;

import com.infracloud.dto.ShortenResponse;
import com.infracloud.dto.TopDomainResponse;

public interface UrlShortenerService {
	ShortenResponse shortenUrl(String originalUrl);

    String getOriginalUrl(String shortKey);

    List<TopDomainResponse> getTopDomains(int limit);
}
