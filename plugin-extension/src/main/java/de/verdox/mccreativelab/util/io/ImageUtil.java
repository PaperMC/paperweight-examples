package de.verdox.mccreativelab.util.io;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ImageUtil {
    public static InputStream bufferedImageToInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    public static BufferedImage cropTransparentPixels(InputStream inputStream) {
        try {
            return cropTransparentPixels(ImageIO.read(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getPixelHeight(Supplier<InputStream> bitMapFile) throws IOException {
        return ImageIO.read(bitMapFile.get()).getHeight();
    }

    public static int getPixelWidth(Supplier<InputStream> bitMapFile) throws IOException {
        return ImageIO.read(bitMapFile.get()).getWidth();
    }

    public static BufferedImage cropTransparentPixels(BufferedImage image) {
        // Get the bounds of the visible content
        Rectangle bounds = getVisibleBounds(image);
        if(bounds.getWidth() <= 0 || bounds.getHeight() <= 0)
            return createTransparentImage(1,1);

        // Create a new image with the dimensions of the visible content
        BufferedImage croppedImage = new BufferedImage(
                bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);

        // Draw the visible content onto the new image
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(image, 0, 0, bounds.width, bounds.height,
                bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, null);
        g.dispose();

        return croppedImage;
    }

    private static BufferedImage createTransparentImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.black);
        //g.clearRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private static Rectangle getVisibleBounds(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int minX = width;
        int minY = height;
        int maxX = 0;
        int maxY = 0;

        // Iterate over each pixel and update the bounds of the visible content
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                if ((pixel >> 24) != 0x00) {  // Check if the pixel is not transparent
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        // Calculate the width and height of the visible content
        int visibleWidth = maxX - minX + 1;
        int visibleHeight = maxY - minY + 1;

        // Adjust the bounds relative to the top-left corner of the image
        int adjustedX = Math.max(0, minX);
        int adjustedY = Math.max(0, minY);
        int adjustedWidth = Math.min(visibleWidth, width - adjustedX);
        int adjustedHeight = Math.min(visibleHeight, height - adjustedY);

        return new Rectangle(adjustedX, adjustedY, adjustedWidth, adjustedHeight);
    }

    public static void brightenImage(File file, int brightness) {
        brightenImage(file, file, brightness);
    }

    public static void brightenImage2(File input, File output, int brightness) {
        try {
            // Lade das Bild von der Festplatte im ARGB-Format
            BufferedImage image = ImageIO.read(input);

            // Überprüfe, ob das Bild erfolgreich geladen wurde
            if (image == null) {
                System.out.println("Fehler beim Laden des Bildes.");
                return;
            }

            // Überprüfe, ob das Bild einen nativen Transparenzkanal hat
            if (image.getTransparency() != BufferedImage.TRANSLUCENT) {
                System.out.println("Das Bild hat keinen nativen Transparenzkanal.");
                return;
            }

            // Iteriere über jeden Pixel im Bild
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // Erhalte den ARGB-Farbwert des aktuellen Pixels
                    int argb = image.getRGB(x, y);

                    // Erhalte den Alpha-Wert des Pixels
                    int alpha = (argb >> 24) & 0xFF;

                    // Überprüfe, ob der Pixel transparent ist
                    if (alpha == 0) {
                        continue; // Überspringe transparente Pixel
                    }

                    // Erhöhe den Helligkeitswert des Pixels
                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;

                    red += brightness;
                    green += brightness;
                    blue += brightness;

                    // Stelle sicher, dass die Helligkeitswerte im gültigen Bereich von 0 bis 255 bleiben
                    red = Math.min(red, 255);
                    green = Math.min(green, 255);
                    blue = Math.min(blue, 255);

                    // Setze den neuen Farbwert für den Pixel
                    int newArgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, newArgb);
                }
            }

            // Speichere das modifizierte Bild auf der Festplatte
            ImageIO.write(image, "png", output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void brightenImage(File file, File output, int brightness) {
        try {
            // Lade das Bild von der Festplatte
            BufferedImage image = ImageIO.read(file);

            // Konvertiere das Bild in einen unterstützten Farbraum
            image = convertToARGB(image);

            // Iteriere über jeden Pixel im Bild
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // Erhalte die Farbe des aktuellen Pixels
                    Color pixelColor = new Color(image.getRGB(x, y));

                    // Überprüfe, ob der Pixel transparent ist
                    if (pixelColor.getAlpha() == 0 || (pixelColor.getRed() == 0 && pixelColor.getGreen() == 0 && pixelColor.getBlue() == 0))
                        continue; // Überspringe transparente Pixel

                    // Erhöhe den Helligkeitswert des Pixels
                    int red = pixelColor.getRed() + brightness;
                    int green = pixelColor.getGreen() + brightness;
                    int blue = pixelColor.getBlue() + brightness;

                    // Stelle sicher, dass die Helligkeitswerte im gültigen Bereich von 0 bis 255 bleiben
                    red = Math.min(red, 255);
                    green = Math.min(green, 255);
                    blue = Math.min(blue, 255);

                    // Setze die neue Farbe für den Pixel
                    Color newColor = new Color(red, green, blue);
                    image.setRGB(x, y, newColor.getRGB());
                }
            }

            // Speichere das modifizierte Bild auf der Festplatte
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage convertToARGB(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = convertedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return convertedImage;
    }

    public static List<BufferedImage> splitImage(InputStream inputStream, int parts) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int[] originalPixels = originalImage.getRGB(0, 0, width, height, null, 0, width);
        var imageList = new LinkedList<BufferedImage>();

        int[] outputPixels = new int[originalPixels.length];
        Arrays.fill(outputPixels, 0, outputPixels.length, 0x00000000);
        for (int i = 1; i <= parts; i++) {
            int startX = (i - 1) * width / parts;
            int endX = i * width / parts;
            for (int y = 0; y < height; y++) {
                for (int x = startX; x < endX; x++) {
                    int pixelIndex = y * width + x;
                    outputPixels[pixelIndex] = originalPixels[pixelIndex];
                }
            }
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            outputImage.setRGB(0, 0, width, height, outputPixels, 0, width);
            imageList.add(outputImage);
/*            File outputfile = new File("output" + i + ".png");
            ImageIO.write(outputImage, "png", outputfile);*/
        }
        return imageList;
    }


    public enum Direction {
        RIGHT,
        LEFT,
        UP,
        DOWN
    }

}
