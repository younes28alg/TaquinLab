package com.game.taquin.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Entité représentant une partie de Taquin.
 * Gère la logique de la grille, le mélange et la validation des mouvements.
 */
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player player; // Le joueur propriétaire de cette partie

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> board; // Représentation plate de la grille (0 = case vide)

    private int size; // Taille de la grille (ex: 3 pour 3x3)
    private int moves; // Nombre de mouvements dans le niveau actuel
    private int totalAccumulatedMoves; // Score cumulé des niveaux précédents
    private long startTime; // Timestamp de début
    private boolean completed; // Vrai si le niveau est fini
    private boolean saved; // Vrai si le joueur a cliqué sur "Sauvegarder"
    private int level; // Niveau actuel (1, 2, 3 ou 4)
    private String imageName; // Nom de l'image de fond

    public Game() {}

    /**
     * Initialise une nouvelle partie.
     */
    public Game(int size, int level, String imageName, int totalAccumulatedMoves) {
        this.size = size;
        this.level = level;
        this.imageName = imageName;
        this.totalAccumulatedMoves = totalAccumulatedMoves;
        this.moves = 0;
        this.completed = false;
        this.startTime = System.currentTimeMillis();
        initializeBoard();
    }

    /**
     * Crée la grille ordonnée puis la mélange.
     */
    private void initializeBoard() {
        board = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            board.add(i); 
        }
        shuffleBoard();
    }

    /**
     * Mélange la grille jusqu'à ce qu'elle soit résoluble mais pas déjà gagnée.
     */
    private void shuffleBoard() {
        do {
            Collections.shuffle(board);
        } while (!isSolvable() || isSolved());
    }

    /**
     * Algorithme de vérification de la résolubilité d'un puzzle Taquin.
     */
    private boolean isSolvable() {
        int inversions = 0;
        for (int i = 0; i < board.size(); i++) {
            for (int j = i + 1; j < board.size(); j++) {
                if (board.get(i) != 0 && board.get(j) != 0 && board.get(i) > board.get(j)) {
                    inversions++;
                }
            }
        }
        if (size % 2 != 0) {
            return inversions % 2 == 0;
        } else {
            int emptyRow = board.indexOf(0) / size;
            return (inversions + emptyRow) % 2 != 0;
        }
    }

    /**
     * Vérifie si toutes les tuiles sont à leur place.
     */
    public boolean isSolved() {
        for (int i = 0; i < board.size() - 1; i++) {
            if (board.get(i) != i + 1) return false;
        }
        return board.get(board.size() - 1) == 0;
    }

    /**
     * Effectue un mouvement si la tuile est adjacente à la case vide.
     */
    public boolean move(int tileIndex) {
        if (completed) return false;
        
        int emptyIndex = board.indexOf(0);
        if (isAdjacent(tileIndex, emptyIndex)) {
            Collections.swap(board, tileIndex, emptyIndex);
            moves++;
            if (isSolved()) {
                completed = true;
            }
            return true;
        }
        return false;
    }

    /**
     * Vérifie si deux index sont voisins (Haut, Bas, Gauche, Droite).
     */
    private boolean isAdjacent(int idx1, int idx2) {
        int r1 = idx1 / size, c1 = idx1 % size;
        int r2 = idx2 / size, c2 = idx2 % size;
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public List<Integer> getBoard() { return board; }
    public void setBoard(List<Integer> board) { this.board = board; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getMoves() { return moves; }
    public void setMoves(int moves) { this.moves = moves; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public int getTotalAccumulatedMoves() { return totalAccumulatedMoves; }
    public void setTotalAccumulatedMoves(int totalAccumulatedMoves) { this.totalAccumulatedMoves = totalAccumulatedMoves; }
    public int getTotalScore() { return totalAccumulatedMoves + moves; }
}
