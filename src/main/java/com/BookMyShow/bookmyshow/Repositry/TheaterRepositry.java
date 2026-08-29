package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TheaterRepositry extends JpaRepository<Theater,Long> {


    List<Theater> findByCityId(Long cityId);
}
