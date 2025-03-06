package com.livescoring.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.livescoring.Entity.Match; 


@Repository 
public interface MatchRepository extends JpaRepository<Match,Integer>
{
	//Match Fetch Karna Chahate Hoon
	//Provide kar de -->teamHeading.
   Optional<Match> findByTeamHeading(String teamHeading);
}
