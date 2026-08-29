package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Screen;
import com.BookMyShow.bookmyshow.Entity.Theater;
import com.BookMyShow.bookmyshow.Repositry.ScreenRepositry;
import com.BookMyShow.bookmyshow.dto.ScreenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepositry screenRepositry;
    private final TheaterService theaterService;

    public Screen addScreen(ScreenRequest request){
        Theater theater = theaterService.getTheaterById(request.getTheaterId());
        Screen screen = Screen.builder()
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .theater(theater)
                .build();
        return screenRepositry.save(screen);
    }

    public List<Screen> getAllScreen(){
        return screenRepositry.findAll();
    }

    public Screen getScreenById(Long id){
        return screenRepositry.findById(id)
                .orElseThrow(()->new RuntimeException("Screen Not found"+id));
    }

    public List<Screen> getScreenByTheater(Long theaterId){
        return screenRepositry.findByTheaterId(theaterId);
    }
    
}
