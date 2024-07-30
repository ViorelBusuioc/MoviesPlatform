package dev.vio.rating_service.controller;

import dev.vio.rating_service.entity.Rating;
import dev.vio.rating_service.entity.RatingTotalDto;
import dev.vio.rating_service.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/ratings")
    public String saveRating(@RequestBody Rating rating) {

        return ratingService.saveRating(rating);

    }

    @GetMapping("/ratings/total")
    public List<RatingTotalDto> getMoviesSortedByTotalStars() {

        return ratingService.getMoviesSortedByTotalStars();
    }
}
