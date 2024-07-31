package dev.vio.movies_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vio.movies_service.dao.MovieRepository;
import dev.vio.movies_service.entity.Movie;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public List<Movie> findAllMovies() {

        return movieRepository.findAll();
    }

    public Movie getById(Long id) {

        Movie movie = null;

        Optional<Movie> result = movieRepository.findById(id);

        if(result.isPresent()) {
           return movie = result.get();
        } else {
            throw new RuntimeException("No movie with the id: "+ id);
        }
    }

    public String saveMovie(Movie movie) {

        movieRepository.save(movie);

        kafkaTemplate.send("movies-topic", movie.getName(), movie.getGenre());

        log.info("Movie {} has been added to the database!", movie.getName());

        return "Movie " + movie.getName() + " has been added to the database!";

    }

    public String deleteMovieById(Long id) {

        movieRepository.deleteById(id);

        return "Movie with the id + " + id + " has been deleted!";
    }

    public void addMoviesFromJSON(MultipartFile file) {

        try {
            // Parse JSON file into a list of movies
            List<Movie> movies = objectMapper.readValue(file.getInputStream(), new TypeReference<List<Movie>>(){});
            // Save the movies to the database
            for (Movie movie : movies) {
                saveMovie(movie);
            }
            System.out.println("Movies have been loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load movies from file.");
        }

    }

    public Movie getByName(String name) {

        return movieRepository.getByName(name).orElseThrow(
                () -> new RuntimeException("No movie with the name -> " + name));
    }

}
