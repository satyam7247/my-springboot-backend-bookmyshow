package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Screen;
import com.BookMyShow.bookmyshow.Service.ScreenService;
import com.BookMyShow.bookmyshow.dto.ScreenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/screens", "/api/screen"})
@RequiredArgsConstructor

public class ScreenController {
    private final ScreenService screenService;

    @PostMapping
    public ResponseEntity<Screen> addScreen(@RequestBody ScreenRequest request){
        return ResponseEntity.ok(screenService.addScreen(request));
    }

    @GetMapping
    public ResponseEntity<List<Screen>> getAllScreen(){
        return ResponseEntity.ok(screenService.getAllScreen());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Screen> getScreenById(@PathVariable Long id){
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<Screen>> getScreenByTheaterId(@PathVariable Long theaterId){
        return ResponseEntity.ok(screenService.getScreenByTheater(theaterId));
    }
}
