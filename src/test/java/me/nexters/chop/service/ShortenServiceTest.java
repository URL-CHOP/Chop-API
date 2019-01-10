package me.nexters.chop.service;

import me.nexters.chop.domain.url.Url;
import me.nexters.chop.dto.url.UrlRequestDto;
import me.nexters.chop.repository.ShortenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author junho.park
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
public class ShortenServiceTest {

    @Autowired
    private ShortenRepository shortenRepository;

    @Value("${string.base62matchPattern}")
    private String base62matchPattern;

    @Value("${string.originUrl}")
    private String originUrl;

    @Value("${string.shortUrl}")
    private String shortUrl;

    @Test
    public void base62Encode_SatisfyRegex() {
        assertEquals(true, shortUrl.matches(base62matchPattern));
    }

    @Test
    public void save_SaveSuccess() {
        UrlRequestDto requestDto = UrlRequestDto.builder()
                .originUrl(originUrl)
                .shortUrl(shortUrl)
                .build();

        Url url = shortenRepository.save(requestDto.toEntity());

        assertEquals(url.getOriginUrl(), originUrl);
    }

    @Test
    public void save_EmptyOriginUrl_DataIntegrityViolationException() {
        UrlRequestDto requestDto = UrlRequestDto.builder()
                .build();

        assertThrows(DataIntegrityViolationException.class, () ->
            shortenRepository.save(requestDto.toEntity())
        );
    }
}
