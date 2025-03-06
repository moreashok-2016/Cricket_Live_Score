package com.livescoring.Controller;

import com.livescoring.Service.MatchService;
import com.livescoring.Entity.Match;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController 
@RequestMapping("/match")
//@CrossOrigin("*")
@CrossOrigin(origins = "http://localhost:4200") 
public class MatchController {

    private final MatchService matchService; 

    // Constructor Injection
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    // Get live matches
    @GetMapping("/live")
    public ResponseEntity<List<Match>> getLiveMatches() {
        List<Match> liveMatches = matchService.getLiveMatches();
        if (!liveMatches.isEmpty()) {
            return new ResponseEntity<>(liveMatches, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // 204 status if no live matches
        }
    }
    
    //get all matches
   // @GetMapping("/all") 
    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches()
    {
    	return new ResponseEntity<>(this.matchService.getAllMatches(),HttpStatus.OK);
    }
    
    //get point table;
    @GetMapping("/point-table")
    public ResponseEntity<?> getPointTable() 
    {
    	return new ResponseEntity<>(this.matchService.getPointTable(),HttpStatus.OK); 
    }
}
