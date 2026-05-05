import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

//Loads PNG images from assets/ and caches them.
//Returns null if a file is missing
public class AssetManager {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    public static BufferedImage get(String key) {
        if (cache.containsKey(key)) return cache.get(key);

        File file = new File("assets/" + key + ".png");
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) { cache.put(key, img); return img; }
            } catch (Exception ignored) {}
        }

        cache.put(key, null); // cache the miss so it doesnt retry on every frame
        return null;
    }
}