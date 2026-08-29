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


    //update movie apn ko bana na he.
    //delete movie apn ko bana na he.
}
