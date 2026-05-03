package com.game.taquin.repository;

import com.game.taquin.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByPlayerIdAndCompletedFalse(Long playerId);
    List<Game> findAllByPlayerIdAndCompletedFalseAndSavedTrue(Long playerId);
}
