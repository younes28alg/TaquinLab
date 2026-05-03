package com.game.taquin.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entité représentant un joueur dans le système.
 * Stocke les informations d'authentification et le record personnel.
 */
@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; // Nom d'utilisateur unique

    private String password; // Mot de passe (en clair pour ce projet simplifié)

    private int highScore; // Meilleur score (nombre de coups le plus bas)

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Game> games; // Liste des parties associées à ce joueur

    public Player() {}

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }
    public List<Game> getGames() { return games; }
    public void setGames(List<Game> games) { this.games = games; }
}
