package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.City;
import com.BookMyShow.bookmyshow.Entity.Theater;
import com.BookMyShow.bookmyshow.Repositry.TheaterRepositry;
import com.BookMyShow.bookmyshow.dto.TheaterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepositry theaterRepositry;
    private final CityService cityService;

    public Theater addTheater(TheaterRequest request){
        City city = cityService.getCityById(request.getCityId());
        Theater theater = Theater.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(city)
                .build();
        return theaterRepositry.save(theater);
    }

    public List<Theater> getAllTheater(){
        return theaterRepositry.findAll();
    }

    public Theater getTheaterById(Long id){
        return theaterRepositry.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found " + id));
    }

    public List<Theater> getTheaterByCity(Long cityId){
        return theaterRepositry.findByCityId(cityId);
    }
}
