package com.game.taquin.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.io.File;

@Controller
public class ImageController {

    @GetMapping(value = "/static/images/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getPuzzleImage(@PathVariable String name) throws IOException {
        // Tenter de lire l'image depuis le dossier physique
        // On cherche dans src/main/webapp/static/images/
        File file = new File("c:/Users/Admin/Desktop/younes/J2EEE/game/src/main/webapp/static/images/" + name);
        
        if (file.exists() && file.isFile()) {
            return Files.readAllBytes(file.toPath());
        }

        // Si le fichier n'existe pas, on génère l'image par défaut
        int size = 500;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        Color color1, color2;
        String text = "PUZZLE";

        switch (name) {
            case "puzzle2.jpg":
                color1 = new Color(231, 76, 60); // Rouge
                color2 = new Color(192, 57, 43);
                text = "FIRE";
                break;
            case "puzzle3.jpg":
                color1 = new Color(46, 204, 113); // Vert
                color2 = new Color(39, 174, 96);
                text = "NATURE";
                break;
            case "puzzle4.jpg":
                color1 = new Color(241, 196, 15); // Jaune
                color2 = new Color(243, 156, 18);
                text = "SUN";
                break;
            default:
                color1 = new Color(26, 115, 232); // Bleu (par défaut)
                color2 = new Color(128, 0, 128);
                text = "OCEAN";
                break;
        }

        // Fond dégradé
        GradientPaint gp = new GradientPaint(0, 0, color1, size, size, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, size, size);

        // Dessiner des formes variées selon l'image
        g2d.setColor(new Color(255, 255, 255, 40));
        for (int i = 0; i < 15; i++) {
            if (name.equals("puzzle2.jpg")) {
                g2d.fillPolygon(new int[]{i*30, i*30+20, i*30+10}, new int[]{i*40, i*40, i*40-20}, 3);
            } else if (name.equals("puzzle3.jpg")) {
                g2d.fillOval(i * 40, i * 20, 60, 60);
            } else {
                g2d.fillRect(i * 35, i * 25, 50, 50);
            }
        }

        // Ajouter le texte
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2;
        g2d.drawString(text, x, y);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
