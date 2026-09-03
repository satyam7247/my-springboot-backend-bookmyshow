package com.BookMyShow.bookmyshow.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String genre;
    private String language;
    private Integer durationMinutes;
    private Double rating;
    private LocalDate releaseDate;
    private String posterUrl;

    /* Total likes - sab users ke likes ka count (frontend me movie.likeCount se padha jata hai) */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer likeCount = 0;

}
