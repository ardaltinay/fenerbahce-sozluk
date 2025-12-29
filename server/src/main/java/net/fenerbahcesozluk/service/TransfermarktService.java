package net.fenerbahcesozluk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfermarktService {

    private static final String BASE_URL = "https://transfermarkt-api.fly.dev";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TransfermarktCacheService cacheService;

    public JsonNode searchPlayers(String name) {
        try {
            String url = BASE_URL + "/players/search/" + name;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Error searching players: {}", e.getMessage());
            return null;
        }
    }

    public JsonNode searchClubs(String name) {
        try {
            String url = BASE_URL + "/clubs/search/" + name;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Error searching clubs: {}", e.getMessage());
            return null;
        }
    }

    public JsonNode getPlayerProfile(String playerId) {
        String cached = cacheService.getPlayerProfile(playerId);
        if (cached == null)
            return null;
        try {
            return objectMapper.readTree(cached);
        } catch (Exception e) {
            log.error("Error parsing player profile: {}", e.getMessage());
            return null;
        }
    }

    public JsonNode getClubProfile(String clubId) {
        String cached = cacheService.getClubProfile(clubId);
        if (cached == null)
            return null;
        try {
            return objectMapper.readTree(cached);
        } catch (Exception e) {
            log.error("Error parsing club profile: {}", e.getMessage());
            return null;
        }
    }
}
