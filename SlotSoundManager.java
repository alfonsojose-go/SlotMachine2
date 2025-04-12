import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SlotSoundManager {
    private final HashMap<String, Clip> soundClips = new HashMap<>();
    private float currentVolume = 1.0f; // Default volume (100%)

    // Load and store sound clip
    public void load(String name, String filePath) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundClips.put(name, clip);
            applyVolume(clip); // Apply current volume
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + name);
            e.printStackTrace();
        }
    }

    // Play sound once
    public void play(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    // Loop sound
    public void loop(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Stop sound
    public void stop(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // Increase volume by 10%
    public void increaseVolume() {
        setGlobalVolume(currentVolume + 0.1f);
    }

    // Decrease volume by 10%
    public void decreaseVolume() {
        setGlobalVolume(currentVolume - 0.1f);
    }

    // Set volume globally (clamped between 0 and 1)
    public void setGlobalVolume(float volume) {
        this.currentVolume = Math.max(0f, Math.min(volume, 1f));
        for (Clip clip : soundClips.values()) {
            applyVolume(clip);
        }
    }

    // Get current volume (0.0 to 1.0)
    public float getCurrentVolume() {
        return currentVolume;
    }

    // Apply current volume to a clip
    private void applyVolume(Clip clip) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(Math.max(currentVolume, 0.0001)) * 20); // Convert to decibels
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            dB = Math.max(min, Math.min(dB, max)); // Clamp dB to valid range
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            System.err.println("Volume control not supported for this clip.");
        }
    }

    // Release resources
    public void cleanup() {
        for (Clip clip : soundClips.values()) {
            clip.close();
        }
        soundClips.clear();
    }
}
