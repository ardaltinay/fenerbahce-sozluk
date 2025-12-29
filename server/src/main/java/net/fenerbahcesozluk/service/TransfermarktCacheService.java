package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfermarktCacheService {

    private static final String BASE_URL = "https://transfermarkt-api.fly.dev";
    private final RestTemplate restTemplate;

    @Cacheable(value = "playerProfiles", key = "#playerId")
    public String getPlayerProfile(String playerId) {
        try {
            String url = BASE_URL + "/players/" + playerId + "/profile";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("Fetched player profile from API: {}", playerId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching player profile: {}", e.getMessage());
            return null;
        }
    }

    @Cacheable(value = "clubProfiles", key = "#clubId")
    public String getClubProfile(String clubId) {
        try {
            String url = BASE_URL + "/clubs/" + clubId + "/profile";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("Fetched club profile from API: {}", clubId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching club profile: {}", e.getMessage());
            return null;
        }
    }
}
