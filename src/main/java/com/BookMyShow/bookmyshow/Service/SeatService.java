package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Screen;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Repositry.SeatRepositry;
import com.BookMyShow.bookmyshow.dto.SeatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepositry seatRepositry;
    private final ScreenService screenService;

    public Seat addSeat(SeatRequest request){
        Screen screen = screenService.getScreenById(request.getScreenId());
        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber())
                .row(request.getRow())
                .col(request.getCol())
                .seatType(request.getSeatType())
                .screen(screen)
                .build();
        return seatRepositry.save(seat);
    }

    public List<Seat> getSeatsByScreen(Long screenId){
        return seatRepositry.findByScreenId(screenId);
    }

    public Seat getSeatById(Long id){
        return seatRepositry.findById(id)
                .orElseThrow(()->new RuntimeException("seat not found by id "+id));
    }


}
