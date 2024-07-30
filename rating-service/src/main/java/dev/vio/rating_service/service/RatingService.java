package dev.vio.rating_service.service;

import dev.vio.rating_service.dao.RatingRepository;
import dev.vio.rating_service.entity.Rating;
import dev.vio.rating_service.entity.RatingTotalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public List<Rating> getAllRatings() {

        return ratingRepository.findAll();
    }

    public Rating getRating(Long id) {

        return ratingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("No rating with id " + id));
    }

    public String saveRating(Rating rating) {

        if (isMovieAvailable(rating.getMovieName())) {
            ratingRepository.save(rating);
            log.info("Rating added. Movie -> {}", rating.getMovieName());
            kafkaTemplate.send("ratings-topic", rating.getMovieName(), String.valueOf(rating.getStars()));
            return "Rating for the movie " + rating.getMovieName() + "was saved to the database!";
        } else {
            return "Rating not saved. The movie with the name " + rating.getMovieName() + " does not exists in the database";
        }
    }

    public Boolean isMovieAvailable(String name) {

        WebClient webClient = webClientBuilder.build();
        String url = "http://movies-service/api/movies/search?name=" + name;
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .block();
        } catch (Exception e) {
            return false;
        }
    }

    public List<RatingTotalDto> getMoviesSortedByTotalStars() {
        List<Object[]> results = ratingRepository.findMoviesByTotalStars();
        return results.stream()
                .map(result -> new RatingTotalDto((String) result[0], (Long) result[1]))
                .collect(Collectors.toList());
    }

}
