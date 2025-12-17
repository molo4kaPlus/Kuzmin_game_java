// SoundManager.java
package com.example.game;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;

public class SoundManager {
    private static SoundManager instance;
    private Context context;
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private HashMap<String, Integer> soundMap;
    private boolean soundsLoaded = false;
    private float volume = 1.0f;

    // Идентификаторы звуков
    public static final String SOUND_TIMER_BEEP1 = "beep1";
    public static final String SOUND_TIMER_BEEP2 = "beep2";

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initializeSoundPool();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public static synchronized SoundManager getInstance() {
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
            instance.loadSoundsFromAssets();
        }
    }

    private void initializeSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        soundMap = new HashMap<>();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                Log.d("SoundManager", "Sound loaded successfully, sampleId: " + sampleId);
            } else {
                Log.e("SoundManager", "Failed to load sound, sampleId: " + sampleId);
            }
        });
    }

    public void loadSoundsFromAssets() {
        try {
            loadSoundFromAssets(SOUND_TIMER_BEEP1, "sound/beep2.mp3");

            soundsLoaded = true;
            Log.d("SoundManager", "All sounds loaded successfully from assets");

        } catch (Exception e) {
            Log.e("SoundManager", "Error loading sounds from assets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSoundFromAssets(String soundName, String assetPath) {
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(assetPath);
            int soundId = soundPool.load(afd, 1);
            soundMap.put(soundName, soundId);
            afd.close();
            Log.d("SoundManager", "Loaded sound from assets: " + soundName + " path: " + assetPath + " ID: " + soundId);
        } catch (IOException e) {
            Log.e("SoundManager", "Failed to load sound from assets: " + soundName + " path: " + assetPath, e);
        } catch (Exception e) {
            Log.e("SoundManager", "Unexpected error loading sound: " + soundName, e);
        }
    }

    public void playSound(String soundName) {
        playSound(soundName, volume);
    }

    public void playSound(String soundName, float volume) {
        if (!soundsLoaded) {
            Log.w("SoundManager", "Sounds not loaded yet");
            return;
        }

        Integer soundId = soundMap.get(soundName);
        if (soundId != null && soundId != 0) {
            float adjustedVolume = Math.max(0.0f, Math.min(1.0f, volume));
            soundPool.play(soundId, adjustedVolume, adjustedVolume, 1, 0, 1.5f);
            Log.d("SoundManager", "Playing sound: " + soundName + " at volume: " + adjustedVolume);
        } else {
            Log.e("SoundManager", "Sound not found or not loaded: " + soundName);
        }
    }

    public void playSoundLooped(String soundName, float volume) {
        if (!soundsLoaded) return;

        Integer soundId = soundMap.get(soundName);
        if (soundId != null && soundId != 0) {
            float adjustedVolume = Math.max(0.0f, Math.min(1.0f, volume));
            soundPool.play(soundId, adjustedVolume, adjustedVolume, 1, -1, 1.0f);
        }
    }

    public void stopSound(String soundName) {
        if (!soundsLoaded) return;

        Integer soundId = soundMap.get(soundName);
        if (soundId != null && soundId != 0) {
            soundPool.stop(soundId);
        }
    }

    public void stopAllSounds() {
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }

    public void resumeAllSounds() {
        if (soundPool != null) {
            soundPool.autoResume();
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public float getVolume() {
        return volume;
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundMap != null) {
            soundMap.clear();
        }
        instance = null;
        Log.d("SoundManager", "SoundManager released");
    }

    public boolean isSoundsLoaded() {
        return soundsLoaded;
    }

    public void playBackgroundMusic(String assetPath, boolean loop) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context.getAssets().openFd(assetPath);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.setLooping(loop);
            mediaPlayer.setVolume(0.3f, 0.3f);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.d("SoundManager", "Playing background music: " + assetPath);

        } catch (IOException e) {
            Log.e("SoundManager", "Error playing background music from assets", e);
        }
    }

    public void pauseBackgroundMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeBackgroundMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean hasSound(String soundName) {
        return soundMap.containsKey(soundName) && soundMap.get(soundName) != 0;
    }
}