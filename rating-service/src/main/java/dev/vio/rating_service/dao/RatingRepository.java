package dev.vio.rating_service.dao;

import dev.vio.rating_service.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("SELECT r.movieName, SUM(r.stars) as totalStars FROM Rating r GROUP BY r.movieName ORDER BY totalStars DESC")
    List<Object[]> findMoviesByTotalStars();
}
