package dev.vio.recommendations_service.controller;


import dev.vio.recommendations_service.service.RecommendationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationsController {

    private final RecommendationsService recommendationsService;

    @GetMapping
    public Map<String, List<String>> getRecommendations() {
        return recommendationsService.getRecommendations();
    }
}
