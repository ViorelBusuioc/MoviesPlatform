package dev.vio.movies_service.controller;

import dev.vio.movies_service.entity.Movie;
import dev.vio.movies_service.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;


    @GetMapping("/movies")
    public List<Movie> getAllMovie() {

        return movieService.findAllMovies();
    }

    @GetMapping("/movies/{id}")
    public Movie getMovieById(@PathVariable("id") Long id) {

        return movieService.getById(id);
    }

    @GetMapping("/movies/search")
    public Movie getMovieById(@RequestParam("name") String name) {

        return movieService.getByName(name);
    }

    @PostMapping("/movies")
    public String saveMovie(@RequestBody Movie movie) {

        return movieService.saveMovie(movie);
    }

    @PostMapping("/movies/load")
    public void loadData() {

        String filePath = "/Users/viorelbusuioc/Downloads/Test Projects/MoviesApp/movies-service/src/main/java/dev/vio/movies_service/service/MOCK_DATA.json"; // Update with your actual file path
        movieService.addMoviesFromJSON(filePath);
    }

}
