import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import net.murat.gui.*;

/**
 * GenerateImageFromScene - Configuration-driven scene builder for Murat Ray Tracer
 * 
 * This class parses scene.txt configuration files and builds complete
 * 3D scenes with lights, shapes, and materials. All color operations use
 * packed ARGB integers for zero-allocation performance.
 * 
 * @author Murat Ray Tracer Team
 * @version 2.0 (GC-free int color system)
 */
public final class GenerateImageFromScene {
    
    @Override
    public String toString() {
        return "GenerateImageFromScene";
    }
    
    private static final void usage(final String[] args) {
		if (args.length < 2) {
			System.err.println("Example:\n\tjava -cp .:guijtracer.jar GenerateImageFromScene xicon.txt xicon.png\n");
			System.exit(-1);
		}
	}
	
    public static void main(String[] args) {
       usage(args);
                    
       File sceneFile = new File(args[0]);
       File outputFile = new File(args[1]);

       SceneParser parser = new SceneParser();
       
       boolean errored = false;

       try {   
          BufferedImage bimg = parser.renderScene(sceneFile);
          ImageIO.write(bimg, "png", outputFile);  
        } catch (Exception e) {
          System.err.println("[Fatal] Parsing cracked: " + e.getMessage());
          e.printStackTrace();
          errored = true;
        }
        
        if (!errored) {
	      System.out.println("\nGenerated: " + outputFile.getName());
		}
    }
    
}

// javac -cp guijtracer.jar GenerateImageFromScene.java
// java -cp .:guijtracer.jar GenerateImageFromScene xicon.txt xicon.png
