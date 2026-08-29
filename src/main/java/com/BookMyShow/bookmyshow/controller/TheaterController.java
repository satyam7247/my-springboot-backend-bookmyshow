package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Theater;
import com.BookMyShow.bookmyshow.Service.TheaterService;
import com.BookMyShow.bookmyshow.dto.TheaterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @PostMapping
    public ResponseEntity<Theater> addTheater(@RequestBody TheaterRequest request){
        return ResponseEntity.ok(theaterService.addTheater(request));
    }

    @GetMapping
    public ResponseEntity<List<Theater>> getAllTheaters(){
        return ResponseEntity.ok(theaterService.getAllTheater());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable Long id){
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Theater>> getTheaterByCity(@PathVariable Long cityId){
        return ResponseEntity.ok(theaterService.getTheaterByCity(cityId));
    }
}
