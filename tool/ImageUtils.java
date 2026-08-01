import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: Please provide at least the image file path.");
            System.out.println("Usage 1 (Default 1,1,510,510): java ImageUtils xicon.png");
            System.out.println("Usage 2 (Custom coordinates): java ImageUtils xicon.png [x] [y] [width] [height]");
            System.out.println("Example 2: java ImageUtils xicon.png 1 1 510 510");
            return;
        }

        String inputPath = args[0];
        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            System.out.println("Error: The file '" + inputPath + "' could not be found.");
            return;
        }

        // Varsayılan koordinat değerleri (Kullanıcı değer girmezse bunlar geçerli olur)
        double x = 1;
        double y = 1;
        double width = 510;
        double height = 510;

        // Eğer kullanıcı 5 argüman birden girdiyse, koordinatları parametrelerden oku
        if (args.length == 5) {
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                width = Double.parseDouble(args[3]);
                height = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Coordinates and sizes must be numeric values.");
                return;
            }
        } else if (args.length > 1 && args.length < 5) {
            System.out.println("Error: Invalid number of arguments. Provide either just the file path, or the path followed by exactly 4 coordinates (x, y, width, height).");
            return;
        }

        try {
            BufferedImage sourceImage = ImageIO.read(inputFile);
            if (sourceImage == null) {
                System.out.println("Error: The file is not a valid image format.");
                return;
            }

            // Seçilen veya varsayılan koordinatlarla oval kırpma işlemini yap
            BufferedImage editedImage = cropOvalRegion(sourceImage, x, y, width, height);

            String outputFileName = createOutputFileName();
            File outputFile = new File(outputFileName);

            ImageIO.write(editedImage, "png", outputFile);
            System.out.println("Success! Image saved to: " + createOutputFileName());
            System.out.println(String.format(java.util.Locale.US, "Cropped area parameters -> X: %.1f, Y: %.1f, Width: %.1f, Height: %.1f", x, y, width, height));

        } catch (IOException e) {
            System.out.println("An error occurred while processing the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Copies a user-defined oval region from the source image onto a new transparent destination image.
     */
    public static BufferedImage cropOvalRegion(BufferedImage sourceImage, double x, double y, double width, double height) {
        int imgWidth = sourceImage.getWidth();
        int imgHeight = sourceImage.getHeight();

        BufferedImage destImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = destImage.createGraphics();

        try {
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, imgWidth, imgHeight);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.SrcOver);

            // Kullanıcının belirlediği dinamik koordinatlarla oval maske oluşturuluyor
            Ellipse2D.Double ovalMask = new Ellipse2D.Double(x, y, width, height);
            g2d.setClip(ovalMask);

            g2d.drawImage(sourceImage, 0, 0, null);

        } finally {
            g2d.dispose();
        }

        return destImage;
    }

    private static String createOutputFileName() {
        return "icon.png";
    }
    
}
