package dev.vio.rating_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingTotalDto {

    private String movieName;

    private Long totalStars;

}
