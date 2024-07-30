package dev.vio.recommendations_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationsService {

    private final Map<String, List<String>> recommendations = new HashMap<>();

    @KafkaListener(topics = "movies-topic")
    public void listenMoviesTopic(ConsumerRecord<String, String> record) throws InterruptedException {

        String title = record.key();
        String genre = record.value();
        processMovieGenres(title, genre);
        log.info("Recommendations Updated!");
    }

    public void processMovieGenres(String title, String genres) {
        String[] genreArray = genres.split("\\|");

        for (String genre : genreArray) {
            recommendations.computeIfAbsent(genre.trim(), k -> new ArrayList<>()).add(title);
        }
    }

    public Map<String, List<String>> getRecommendations() {
        return recommendations;
    }
}
