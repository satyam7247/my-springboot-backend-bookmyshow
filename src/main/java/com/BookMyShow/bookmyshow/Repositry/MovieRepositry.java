package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.Movie;
import com.BookMyShow.bookmyshow.Entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MovieRepositry extends JpaRepository<Movie,Long> {

    List<Movie> findByGenre(String genre);
    List<Movie> findByLanguage(String language);
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
