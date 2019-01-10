package me.nexters.chop.api;

import me.nexters.chop.domain.url.Url;
import me.nexters.chop.service.ShortenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author junho.park
 */
@RestController
public class ShortenController {

    private final ShortenService shortenService;

    public ShortenController(ShortenService shortenService) {
        this.shortenService = shortenService;
    }

    @PostMapping("/chop/v1/shorten")
    public Url shortenUrl(@RequestBody Map<String,String> requestBody) {
        String originUrl = requestBody.get("originUrl");
        return shortenService.save(originUrl);
    }
}
