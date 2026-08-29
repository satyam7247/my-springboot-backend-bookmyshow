package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.City;
import com.BookMyShow.bookmyshow.Repositry.CityRepositry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepositry cityRepositry;

    public City addcity(City city){
        return cityRepositry.save(city);
    }

    public List<City> getAllcityes(){
        return cityRepositry.findAll();
    }

    public City getCityById(Long id){
        return cityRepositry.findById(id)
                .orElseThrow(()-> new RuntimeException("City not found"+id));
    }
}
