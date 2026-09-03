package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Movie;
import com.BookMyShow.bookmyshow.Repositry.MovieRepositry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepositry movieRepositry;

    public Movie addmovie(Movie movie){
        // Frontend likeCount nahi bhejta - null aaye to 0 se start karo (not-null constraint fail na ho)
        if (movie.getLikeCount() == null) movie.setLikeCount(0);
        return movieRepositry.save(movie);
    }

    public void deleteMovie(Long id){
        Movie movie = getMovieById(id);
        movieRepositry.delete(movie);
    }

    public List<Movie> getAllMovies(){
        return movieRepositry.findAll();
    }

    public Movie getMovieById(Long id){
        return movieRepositry.findById(id)
                .orElseThrow(()-> new RuntimeException("Movie not found"+id));
    }

    public List<Movie> searchByTitle(String title){
        return movieRepositry.findByTitleContainingIgnoreCase(title);
    }


    public List<Movie> getByGenre(String genre){
        return movieRepositry.findByGenre(genre);
    }
    public List<Movie> getByLanguage(String language){
        return movieRepositry.findByLanguage(language);
    }

    public Movie updateMovie(Long id, Movie updatedMovie){
        Movie movie = getMovieById(id); // agar id nahi mili to already RuntimeException throw hoga

        movie.setTitle(updatedMovie.getTitle());
        movie.setDescription(updatedMovie.getDescription());
        movie.setGenre(updatedMovie.getGenre());
        movie.setLanguage(updatedMovie.getLanguage());
        movie.setDurationMinutes(updatedMovie.getDurationMinutes());
        movie.setRating(updatedMovie.getRating());
        movie.setReleaseDate(updatedMovie.getReleaseDate());
        movie.setPosterUrl(updatedMovie.getPosterUrl());

        return movieRepositry.save(movie);
    }

    /* Like count +1 - null safe (purane records me likeCount null ho sakta hai) */
    public Movie incrementLike(Long id){
        Movie movie = getMovieById(id);
        movie.setLikeCount((movie.getLikeCount() == null ? 0 : movie.getLikeCount()) + 1);
        return movieRepositry.save(movie);
    }

    /* Like count -1 - 0 se niche kabhi nahi jayega */
    public Movie decrementLike(Long id){
        Movie movie = getMovieById(id);
        int current = (movie.getLikeCount() == null ? 0 : movie.getLikeCount());
        movie.setLikeCount(Math.max(current - 1, 0));
        return movieRepositry.save(movie);
    }

}
