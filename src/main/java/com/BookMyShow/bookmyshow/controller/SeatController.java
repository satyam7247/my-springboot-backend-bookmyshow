package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Service.SeatService;
import com.BookMyShow.bookmyshow.dto.SeatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private  final SeatService seatService;

    @PostMapping
    public ResponseEntity<Seat> addSeat(@RequestBody SeatRequest request){
        return ResponseEntity.ok(seatService.addSeat(request));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<Seat>> getSeatByScreen(@PathVariable Long screenId){
        return ResponseEntity.ok(seatService.getSeatsByScreen(screenId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long id){
        return ResponseEntity.ok(seatService.getSeatById(id));
    }



}
