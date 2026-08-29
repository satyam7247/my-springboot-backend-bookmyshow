package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.City;
import com.BookMyShow.bookmyshow.Service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private  final CityService cityService;

    @PostMapping
    public ResponseEntity<City> addCity(@RequestBody City city){
        return ResponseEntity.ok(cityService.addcity(city));
    }

    @GetMapping
    public ResponseEntity<List<City>> getAllCity(){
        return ResponseEntity.ok(cityService.getAllcityes());
    }

    @GetMapping("{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id){
        return ResponseEntity.ok(cityService.getCityById(id));
    }
}
