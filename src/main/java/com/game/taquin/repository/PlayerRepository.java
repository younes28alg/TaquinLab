package com.game.taquin.repository;

import com.game.taquin.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUsername(String username);
    Optional<Player> findByUsernameAndPassword(String username, String password);
    List<Player> findTop10ByOrderByHighScoreAsc(); // Meilleurs scores (moins de coups = mieux)
}
