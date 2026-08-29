package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Movie;
import com.BookMyShow.bookmyshow.Service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie){
        return ResponseEntity.ok(movieService.addmovie(movie));
    }

    @GetMapping
    public ResponseEntity<List<Movie>>  getAllMovies(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Movie>  getMovieById(@PathVariable Long id){
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovie(@RequestParam String title){
        return ResponseEntity.ok(movieService.searchByTitle(title));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Movie>> getByGenre(@PathVariable String genre){
        return ResponseEntity.ok(movieService.getByGenre(genre));
    }


    @GetMapping("/language/{language}")
    public ResponseEntity<List<Movie>> getByLanguage(@PathVariable String language){
        return ResponseEntity.ok(movieService.getByLanguage(language));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id){
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
