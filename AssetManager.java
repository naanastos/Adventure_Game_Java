import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

//Loads PNG images from assets/ and caches them.
//Returns null if a file is missing
public class AssetManager {

    private static final Map<String, BufferedImage>       cache       = new HashMap<>();
    private static final Map<String, List<BufferedImage>> framesCache = new HashMap<>();

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

    // Animated NPCs. Cylces through PNG in a list
    public static List<BufferedImage> getFrames(String key) {
        if (framesCache.containsKey(key)) return framesCache.get(key);

        List<BufferedImage> frames = new ArrayList<>();
        int i = 0;
        while (true) {
            File f = new File("assets/" + key + "_" + i + ".png");
            if (!f.exists()) break;
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null) frames.add(img);
            } catch (Exception ignored) {}
            i++;
        }

        // If frames aren't numbered, all back to static png
        if (frames.isEmpty()) {
            BufferedImage single = get(key);
            if (single != null) frames.add(single);
        }

        framesCache.put(key, frames);
        return frames;
    }
}
