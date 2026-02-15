package com.infracloud;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import com.infracloud.dto.ShortenResponse;
import com.infracloud.dto.TopDomainResponse;
import com.infracloud.service.UrlShortenerService;
import com.infracloud.service.impl.UrlShortenerServiceImpl;

@SpringBootTest
class UrlShortenerApplicationTests {
	private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerServiceImpl();
    }

    @Test
    void shouldReturnSameShortUrlForSameInput() {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

        ShortenResponse first = service.shortenUrl(url);
        ShortenResponse second = service.shortenUrl(url);

        assertThat(first.shortUrl()).isEqualTo(second.shortUrl());
        assertThat(first.originalUrl()).isEqualTo(second.originalUrl());
    }

    @Test
    void shouldReturnDifferentShortUrlsForDifferentInputs() {
        String url1 = "https://www.google.com";
        String url2 = "https://www.wikipedia.org";

        ShortenResponse r1 = service.shortenUrl(url1);
        ShortenResponse r2 = service.shortenUrl(url2);

        assertThat(r1.shortUrl()).isNotEqualTo(r2.shortUrl());
    }

    @Test
    void shouldTrackTopDomainsCorrectly() {
        service.shortenUrl("https://www.youtube.com/watch?v=1");
        service.shortenUrl("https://www.youtube.com/watch?v=2");
        service.shortenUrl("https://www.youtube.com/watch?v=3");
        service.shortenUrl("https://udemy.com/course/java");
        service.shortenUrl("https://udemy.com/course/docker");
        service.shortenUrl("https://en.wikipedia.org/wiki/Java");

        List<TopDomainResponse> top = service.getTopDomains(3);

        assertThat(top).hasSize(3);
        assertThat(top.get(0).domain()).isEqualTo("youtube.com");
        assertThat(top.get(0).count()).isEqualTo(3);
        assertThat(top.get(1).domain()).isEqualTo("udemy.com");
        assertThat(top.get(1).count()).isEqualTo(2);
        assertThat(top.get(2).domain()).isEqualTo("wikipedia.org");
        assertThat(top.get(2).count()).isEqualTo(1);
    }

}
