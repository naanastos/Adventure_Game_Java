import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;


// This handles looping background music.
// music.wav plays once on startup, then music2.wav loops continuously.

public class MusicPlayer {

    private static final String AUDIO_DIR   = "assets/audio/";
    private static final String INTRO       = "music.wav";   // plays once on startup
    private static final String DEFAULT     = "music2.wav";  // loops after intro ends
    private static final long   INTERVAL_US = 10_000_000L;   // 10 seconds intervals


    // Any location not listed here plays the default track.
    private static final Map<LocationId, String> LOCATION_TRACKS = new HashMap<>();
    static {
        LOCATION_TRACKS.put(LocationId.FITNESS_CENTER, "fitness.wav");
        LOCATION_TRACKS.put(LocationId.RADIO_STATION,  "lofi.wav");
    }

    // Saved resume positions (in microseconds) keyed by filename.
    private static final Map<String, Long> savedPositions = new HashMap<>();

    private static Clip             currentClip   = null;
    private static volatile String  currentTrack  = "";
    private static volatile boolean introFinished = false;

    // Call this every time the player enters a new location.
    public static void play(LocationId loc) {
        String track = LOCATION_TRACKS.getOrDefault(loc, currentDefault());
        if (track.equals(currentTrack)) return;
        switchTo(track);
    }

    //for non location screens like the title
    public static void playDefault() {
        String track = currentDefault();
        if (track.equals(currentTrack)) return;
        switchTo(track);
    }

    // intro if not yet finished otherwise the looping track
    private static String currentDefault() {
        return introFinished ? DEFAULT : INTRO;
    }

    private static void switchTo(String filename) {
        savePosition();
        currentTrack = filename; // set BEFORE stop() so the intro listener ignores this stop event
        stop();

        File file = new File(AUDIO_DIR + filename);
        if (!file.exists()) return; // missing file - run silently

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            currentClip = AudioSystem.getClip();
            currentClip.open(stream);

            // Resume from the last saved 10-second boundary, if any.
            long resume = savedPositions.getOrDefault(filename, 0L);
            long clipLength = currentClip.getMicrosecondLength();
            if (resume > 0 && resume < clipLength) {
                currentClip.setMicrosecondPosition(resume);
            }

            if (filename.equals(INTRO)) {
                // Intro plays once. When it ends naturally, transition to the loop track.
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP && INTRO.equals(currentTrack)) {
                        introFinished = true;
                        switchTo(DEFAULT);
                    }
                });
                currentClip.start();
            } else {
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception ignored) {}
    }

    // Saves the current position rounded down to the nearest 10 sec interval 
    private static void savePosition() {
        if (currentClip == null || currentTrack.isEmpty()) return;
        long pos = currentClip.getMicrosecondPosition();
        savedPositions.put(currentTrack, (pos / INTERVAL_US) * INTERVAL_US);
    }

    private static void stop() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
        }
        currentClip = null;
    }
}
