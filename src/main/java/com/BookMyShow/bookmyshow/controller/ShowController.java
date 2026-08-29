package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Service.ShowService;
import com.BookMyShow.bookmyshow.dto.ShowRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Service.BookingService;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Show> addShow(@RequestBody ShowRequest request){
        return ResponseEntity.ok(showService.addshow(request));
    }

    @GetMapping
    public ResponseEntity<List<Show>> getAllAhows(){
        return ResponseEntity.ok(showService.getAllShow());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Show> getShowById(@PathVariable Long id){
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id){
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getShowByMovie(@PathVariable Long movieId){
        return ResponseEntity.ok(showService.getShowByMovie(movieId));
    }

    @GetMapping("/movie/{movieId}/date")
    public ResponseEntity<List<Show>> getShowByMovieAndDate(@PathVariable Long movieId,  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        return ResponseEntity.ok(showService.getShowByMovieAndDate(movieId , date));
    }

    @GetMapping("/{showId}/available-seats")
    public ResponseEntity<List<Seat>> getAvailableSeatsByShowId(@PathVariable Long showId) {
        return ResponseEntity.ok(bookingService.getAvaliableSeat(showId));
    }

    @GetMapping("/available-seats/{showId}")
    public ResponseEntity<List<Seat>> getAvailableSeatsByShowIdLegacy(@PathVariable Long showId) {
        return ResponseEntity.ok(bookingService.getAvaliableSeat(showId));
    }
}
