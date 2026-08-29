package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Movie;
import com.BookMyShow.bookmyshow.Entity.Screen;
import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Repositry.ShowRepositry;
import com.BookMyShow.bookmyshow.dto.ShowRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepositry showRepositry;
    private final MovieService movieService;
    private  final ScreenService screenService;

    public Show addshow(ShowRequest request){
        Movie movie= movieService.getMovieById(request.getMovieId());
        Screen screen = screenService.getScreenById(request.getScreenId());
        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .ticketPrice(request.getTicketPrice())
                .build();

        return showRepositry.save(show);
    }

    public List<Show> getAllShow(){
        return showRepositry.findAll();
    }

    public Show getShowById(Long id){
        return showRepositry.findById(id)
                .orElseThrow(()-> new RuntimeException("Show not found id "+id));
    }

    public void deleteShow(Long id){
        Show show = getShowById(id);
        showRepositry.delete(show);
    }

    public List<Show> getShowByMovie(Long movieId){
        return showRepositry.findBymovieId(movieId);
    }

    public List<Show> getShowByMovieAndDate(Long movieId, LocalDate date){
        return showRepositry.findByMovieIdAndShowDate(movieId , date);
    }


}

