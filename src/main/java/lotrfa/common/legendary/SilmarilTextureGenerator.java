package lotrfa.common.legendary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class SilmarilTextureGenerator {
    public static void main(String[] args) throws Exception {
        generateSilmaril("silmaril_varda.png", new Color(255, 255, 255), new Color(200, 220, 255));

        generateSilmaril("silmaril_earendil.png", new Color(220, 240, 255),new Color(150, 180, 220));

        generateSilmaril("silmaril_maedhros.png", new Color(255, 250, 220), new Color(220, 180, 100));
    }

    static void generateSilmaril(String filename, Color center, Color outer) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                double dx = x - 8;
                double dy = y - 8;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 6) {
                    float ratio = (float) (distance / 6.0);
                    int r = (int) (center.getRed() * (1 - ratio) + outer.getRed() * ratio);
                    int g = (int) (center.getGreen() * (1 - ratio) + outer.getGreen() * ratio);
                    int b = (int) (center.getBlue() * (1 - ratio) + outer.getBlue() * ratio);
                    int a = 255;

                    img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }

        ImageIO.write(img, "PNG", new File(filename));
        System.out.println("Generated: " + filename);
    }
}