package com.game.taquin.service;

import com.game.taquin.model.Game;
import com.game.taquin.model.Player;
import com.game.taquin.repository.GameRepository;
import com.game.taquin.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

/**
 * Service gérant la logique métier de l'application.
 * Fait le lien entre les contrôleurs et les dépôts de données (repositories).
 */
@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Vérifie les identifiants d'un joueur pour la connexion.
     */
    public Optional<Player> authenticate(String username, String password) {
        return playerRepository.findByUsernameAndPassword(username, password);
    }

    /**
     * Crée un nouveau compte joueur si le pseudo n'existe pas déjà.
     */
    public Player register(String username, String password) {
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        Player p = new Player();
        p.setUsername(username);
        p.setPassword(password);
        return playerRepository.save(p);
    }

    /**
     * Récupère un joueur ou le crée s'il n'existe pas (ancienne méthode).
     */
    public Player getOrCreatePlayer(String username) {
        return playerRepository.findByUsername(username)
                .orElseGet(() -> {
                    Player p = new Player();
                    p.setUsername(username);
                    return playerRepository.save(p);
                });
    }

    /**
     * Initialise une nouvelle partie pour un joueur donné.
     */
    @Transactional
    public Game createNewGame(Long playerId, int level, String imageName, int accumulatedMoves) {
        int size = 2 + level; // Niveau 1 -> 3x3, Niveau 2 -> 4x4, etc.
        Player player = playerRepository.findById(playerId).orElseThrow();
        
        Game game = new Game(size, level, imageName, accumulatedMoves);
        game.setPlayer(player);
        return gameRepository.save(game);
    }

    /**
     * Récupère toutes les parties sauvegardées d'un joueur.
     */
    public List<Game> getSavedGames(Long playerId) {
        return gameRepository.findAllByPlayerIdAndCompletedFalseAndSavedTrue(playerId);
    }

    /**
     * Marque une partie comme étant sauvegardée pour qu'elle apparaisse dans la liste.
     */
    @Transactional
    public void saveGame(Long gameId) {
        gameRepository.findById(gameId).ifPresent(game -> {
            game.setSaved(true);
            gameRepository.save(game);
        });
    }

    /**
     * Récupère la dernière partie active (non terminée) d'un joueur.
     */
    public Optional<Game> getActiveGame(Long playerId) {
        return gameRepository.findByPlayerIdAndCompletedFalse(playerId);
    }

    /**
     * Récupère une partie spécifique par son ID.
     */
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    /**
     * Traite un mouvement de tuile et enregistre l'état si le mouvement est valide.
     */
    @Transactional
    public boolean makeMove(Long gameId, int tileIndex) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        boolean moved = game.move(tileIndex);
        if (moved) {
            gameRepository.save(game);
        }
        return moved;
    }

    /**
     * Termine un niveau, enregistre le score et met à jour le record du joueur.
     */
    @Transactional
    public void finalizeLevel(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        game.setCompleted(true);
        gameRepository.save(game);
        
        Player player = game.getPlayer();
        int currentTotalScore = game.getTotalScore();
        
        // Mise à jour du High Score si nécessaire
        if (player.getHighScore() == 0 || currentTotalScore < player.getHighScore()) {
            player.setHighScore(currentTotalScore);
            playerRepository.save(player);
        }
    }

    /**
     * Récupère le top 10 des meilleurs joueurs.
     */
    public List<Player> getTopScorers() {
        return playerRepository.findTop10ByOrderByHighScoreAsc();
    }
}
