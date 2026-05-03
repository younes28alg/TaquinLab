package com.game.taquin.controller;

import com.game.taquin.model.Game;
import com.game.taquin.model.Player;
import com.game.taquin.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur principal gérant les requêtes HTTP de l'application.
 * Contient les routes pour les vues HTML et l'API AJAX.
 */
@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * API : Récupère l'état actuel de la partie en format JSON.
     */
    @GetMapping("/api/game-state")
    @ResponseBody
    public ResponseEntity<?> getGameState(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return ResponseEntity.status(401).build();

        Long gameId = (Long) session.getAttribute("currentGameId");
        if (gameId == null) return ResponseEntity.notFound().build();

        Game game = gameService.getGameById(gameId).orElse(null);
        if (game == null) return ResponseEntity.notFound().build();

        Map<String, Object> response = new HashMap<>();
        response.put("board", game.getBoard());
        response.put("moves", game.getMoves());
        response.put("totalScore", game.getTotalScore());
        response.put("completed", game.isSolved());
        response.put("level", game.getLevel());
        return ResponseEntity.ok(response);
    }

    /**
     * API : Traite un mouvement de tuile via AJAX sans recharger la page.
     */
    @PostMapping("/api/move/{index}")
    @ResponseBody
    public ResponseEntity<?> moveAjax(@PathVariable int index, HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return ResponseEntity.status(401).build();

        Long gameId = (Long) session.getAttribute("currentGameId");
        if (gameId == null) return ResponseEntity.notFound().build();

        boolean moved = gameService.makeMove(gameId, index);
        
        Game game = gameService.getGameById(gameId).orElseThrow();
        Map<String, Object> response = new HashMap<>();
        response.put("moved", moved);
        response.put("board", game.getBoard());
        response.put("moves", game.getMoves());
        response.put("totalScore", game.getTotalScore());
        response.put("solved", game.isSolved());

        // Si le puzzle est résolu, on passe au niveau suivant ou on finit le jeu
        if (game.isSolved()) {
            gameService.finalizeLevel(game.getId());
            if (game.getLevel() < 4) {
                int nextLevel = game.getLevel() + 1;
                String nextImage = "puzzle" + nextLevel + ".jpg";
                Game nextGame = gameService.createNewGame(player.getId(), nextLevel, nextImage, game.getTotalScore());
                session.setAttribute("currentGameId", nextGame.getId());
                response.put("nextLevelUrl", "/taquin/game/" + nextGame.getId());
            } else {
                session.removeAttribute("currentGameId");
                response.put("finished", true);
            }
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Page d'accueil : Affiche le menu, les sauvegardes et le tableau des scores.
     */
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player != null) {
            model.addAttribute("player", player);
            model.addAttribute("savedGames", gameService.getSavedGames(player.getId()));
        }
        model.addAttribute("topScorers", gameService.getTopScorers());
        return "index";
    }

    /**
     * Connexion : Vérifie les identifiants et ouvre une session.
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<Player> player = gameService.authenticate(username, password);
        if (player.isPresent()) {
            session.setAttribute("player", player.get());
            return "redirect:/";
        }
        model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        model.addAttribute("topScorers", gameService.getTopScorers());
        return "index";
    }

    /**
     * Inscription : Crée un compte et connecte automatiquement l'utilisateur.
     */
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            Player player = gameService.register(username, password);
            session.setAttribute("player", player);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé");
            model.addAttribute("topScorers", gameService.getTopScorers());
            return "index";
        }
    }

    /**
     * Démarre une nouvelle partie (Niveau 1).
     */
    @GetMapping("/new-game")
    public String startNewGame(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return "redirect:/";
        
        Game game = gameService.createNewGame(player.getId(), 1, "puzzle1.jpg", 0);
        return "redirect:/game/" + game.getId();
    }

    /**
     * Charge une partie spécifique par son ID.
     */
    @GetMapping("/game/{id}")
    public String game(@PathVariable Long id, HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return "redirect:/";

        Game game = gameService.getGameById(id).orElse(null);
        if (game == null || !game.getPlayer().getId().equals(player.getId())) return "redirect:/";
        
        session.setAttribute("currentGameId", id);
        model.addAttribute("game", game);
        return "game";
    }

    /**
     * Affiche la vue du jeu actuel.
     */
    @GetMapping("/game")
    public String game(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return "redirect:/";

        Long gameId = (Long) session.getAttribute("currentGameId");
        if (gameId == null) {
            Optional<Game> activeGame = gameService.getActiveGame(player.getId());
            if (activeGame.isPresent()) {
                gameId = activeGame.get().getId();
            } else {
                return "redirect:/";
            }
        }

        Game game = gameService.getGameById(gameId).orElse(null);
        if (game == null) return "redirect:/";
        
        model.addAttribute("game", game);
        return "game";
    }

    /**
     * Déconnexion de l'utilisateur.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * Marque la partie comme sauvegardée et quitte vers l'accueil.
     */
    @GetMapping("/save-game")
    public String saveAndExit(HttpSession session) {
        Player player = (Player) session.getAttribute("player");
        Long gameId = (Long) session.getAttribute("currentGameId");
        
        if (player != null && gameId != null) {
            gameService.saveGame(gameId);
            session.removeAttribute("currentGameId");
        }
        return "redirect:/";
    }
}
