package com.livescoring.Service.Implementation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescoring.Entity.Match;
import com.livescoring.Entity.MatchStatus;
import com.livescoring.Repository.MatchRepository;
import com.livescoring.Service.MatchService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);

    @Autowired
    private MatchRepository matchRepo;

    @Override
    public List<Match> getAllMatches() {
        return matchRepo.findAll();
    } 

    @Override
    public List<Match> getLiveMatches() {
        List<Match> matches = new ArrayList<>();
        try {
            String url = "https://www.cricbuzz.com/cricket-match/live-scores";
            Document document = Jsoup.connect(url).get();
            Elements liveScoreElements = document.select("div.cb-mtch-lst.cb-tms-itm");

            for (Element match : liveScoreElements) {
                Match match1 = new Match();

                match1.setTeamHeading(match.select("h3.cb-lv-scr-mtch-hdr a").text());
                match1.setMatchNumberVenue(match.select("span").text());

                Elements matchBatTeamInfo = match.select("div.cb-hmscg-bat-txt");
                match1.setBattingTeam(matchBatTeamInfo.select("div.cb-hmscg-tm-nm").text());
                match1.setBattingTeamScore(matchBatTeamInfo.select("div.cb-hmscg-tm-nm+div").text());

                Elements bowlTeamInfo = match.select("div.cb-hmscg-bwl-txt");
                match1.setBowlTeam(bowlTeamInfo.select("div.cb-hmscg-tm-nm").text());
                match1.setBowlTeamScore(bowlTeamInfo.select("div.cb-hmscg-tm-nm+div").text());

                match1.setLiveText(match.select("div.cb-text-live").text());
                match1.setMatchLink(match.select("a.cb-lv-scrs-well.cb-lv-scrs-well-live").attr("href"));
                match1.setTextComplete(match.select("div.cb-text-complete").text());

                // Set match status dynamically using setStatus()
                if (!match1.getLiveText().isEmpty()) {
                    match1.setStatus(MatchStatus.LIVE); // Corrected line
                } else {
                    match1.setStatus(MatchStatus.COMPLETED); // Corrected line
                }

                // Save or update match in database
                updateMatch(match1);

                matches.add(match1);
                
                //Update the match in database
                updateMatchInDb(match1);
            }
        } catch (IOException e) {
            logger.error("Error fetching live matches", e);
        }
        return matches;
    }


    private void updateMatchInDb(Match match1) 
    {
		Match match=this.matchRepo.findByTeamHeading(match1.getTeamHeading()).orElse(null);
		
		//database main match hai hi nahi 
		if(match==null)
		{
			matchRepo.save(match1);  
		}else// Agar match hai to update ho jayegi
		{
			match1.setId(match.getId());
			
		}
		
	}

    @Override
    public List<List<String>> getPointTable() {
        List<List<String>> pointTable = new ArrayList<>();
        String tableURL = "https://www.cricbuzz.com/cricket-series/6732/icc-cricket-world-cup-2023/points-table";
        try {
            Document document = Jsoup.connect(tableURL).get();
            Elements table = document.select("table.cb-srs-pnts");
            Elements tableHeads = table.select("thead>tr>*");
            List<String> headers = new ArrayList<>();
            tableHeads.forEach(element -> {
                headers.add(element.text());
            });
            pointTable.add(headers);
            Elements bodyTrs = table.select("tbody>*");
            bodyTrs.forEach(tr -> {
                List<String> points = new ArrayList<>();
                if (tr.hasAttr("class")) {
                    Elements tds = tr.select("td");
                    String team = tds.get(0).select("div.cb-col-84").text();
                    points.add(team);
                    tds.forEach(td -> {
                        if (!td.hasClass("cb-srs-pnts-name")) {
                            points.add(td.text());
                        }
                    });
//                    System.out.println(points);
                    pointTable.add(points);
                }


            });

            System.out.println(pointTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pointTable;
    }

    
    
    // Update existing match or insert new one
    private void updateMatch(Match match) {
        Optional<Match> existingMatch = matchRepo.findByTeamHeading(match.getTeamHeading());
        if (existingMatch.isPresent()) {
            Match existing = existingMatch.get();

            // Update existing match with new values
            existing.setBattingTeam(match.getBattingTeam());
            existing.setBattingTeamScore(match.getBattingTeamScore());
            existing.setBowlTeam(match.getBowlTeam());
            existing.setBowlTeamScore(match.getBowlTeamScore());
            existing.setLiveText(match.getLiveText());
            existing.setMatchLink(match.getMatchLink());
            existing.setTextComplete(match.getTextComplete());
            existing.setStatus(match.getStatus());

            logger.debug("Saving updated match: " + existing);  // Log entity state

            // Save or flush the updated match entity
            matchRepo.saveAndFlush(existing);  // Flush changes to DB
        } else {
            matchRepo.save(match);  // Save the new match if not found
        }
    }
    
    

}
