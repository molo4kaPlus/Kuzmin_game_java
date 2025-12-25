package com.example.game;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    private Level currentLevel;
    private Context context;
    private Handler gameLoopHandler;
    private Runnable gameLoopRunnable;
    private boolean isGameRunning = false;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 16; // ~60 FPS

    ModuleTimer timer;

    // Game state
    private int lives = 3;
    private boolean gameOver = false;
    private boolean gamePaused = false;
    private GameEventListener gameEventListener;
    public interface GameEventListener {
        void onGameOver();
        void onScoreChanged(int newScore);
        void onLivesChanged(int newLives);
        void onErrorAdded(int totalErrors);
        void onTimerFinished(String moduleName);
        void onModuleActivated(String moduleName, boolean activated);
        void onObjectClicked(String objectType, int row, int col);
    }
    private GameManager(Context context) {
        this.context = context.getApplicationContext();
        this.gameLoopHandler = new Handler(Looper.getMainLooper());
        initializeGameLoop();
    }
    public static synchronized GameManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameManager(context);
        }
        return instance;
    }
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameManager must be initialized with context first");
        }
        return instance;
    }
    public static void init(Context context) {
        if (instance == null) {
            instance = new GameManager(context);
        }
    }
    private void initializeGameLoop() {
        gameLoopRunnable = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning && !gamePaused) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                        update();
                        lastUpdateTime = currentTime;
                    }
                }
                gameLoopHandler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };
    }
    public void startGame(Level level) {
        this.currentLevel = level;
        this.lives = 3;
        this.gameOver = false;
        this.gamePaused = false;

        if (gameEventListener != null) {
            gameEventListener.onLivesChanged(lives);
        }

        isGameRunning = true;
        gameLoopHandler.post(gameLoopRunnable);
        Log.d("GameManager", "Game started");
    }
    public void stopGame() {
        isGameRunning = false;
        gameLoopHandler.removeCallbacks(gameLoopRunnable);
        Log.d("GameManager", "Game stopped");
    }
    public void pauseGame() {
        gamePaused = true;
        pauseAllTimers();
        SoundManager.getInstance().stopAllSounds();
        Log.d("GameManager", "Game paused");
    }
    public void resumeGame() {
        gamePaused = false;
        resumeAllTimers();
        SoundManager.getInstance().resumeAllSounds();
        Log.d("GameManager", "Game resumed");
    }
    private void update() {
        if (currentLevel == null || gameOver || gamePaused) {
            return;
        }

        List<Module> modules = currentLevel.getModules();
        for (Module module : modules) {
            updateModule(module);
            updateObjectsInModule(module);
        }
        checkGameConditions();
    }
    private void updateModule(Module module) {
        if (module instanceof ModuleTimer) {
            timer = (ModuleTimer) module;
            if (!timer.isRunning() && timer.getTimeLeftInMillis() <= 0) {
                handleTimerFinished(timer);
            }
        }
        module.updateObjects();
    }
    private void handleTimerFinished(ModuleTimer timer) {
        Log.d("myLog", "Timer finished: " + timer.getName() + timer.getErrorCount());
        timer.addError();
        timer.addError();
        timer.addError();
    }
    private void updateObjectsInModule(Module module) {
        List<Obj> objects = module.getObjects();

        for (Obj obj : objects) {
            if (obj instanceof ObjWire) {
                ObjWire wire = (ObjWire) obj;
                handleWire(wire, module);
            }

            if (obj instanceof ObjBattery) {
                handleBattery((ObjBattery) obj, module);
            }
            obj.update();
        }
    }
    private void handleWire(ObjWire wire, Module module) {
        boolean shouldBeCut = false;
        int color = wire.getColor();
        if(color == Color.WHITE){
            shouldBeCut = true;
        }

        if (!shouldBeCut && wire.isCut() && !wire.checked){
            timer.addError();
            wire.checked = true;
        }
    }
    private void handleBattery(ObjBattery battery, Module module) {
        Log.d("GameManager", "Battery at module: " + module.getName() +
                " type: " + battery.getBatteryType());
        if (gameEventListener != null) {
            gameEventListener.onObjectClicked("BATTERY",
                    battery.getGridRow() + module.getRow() * 3,
                    battery.getGridCol() + module.getCol() * 5
            );
        }
    }
    private void checkGameConditions() {
        if (timer != null && timer.getErrorCount() >= 3) {
            gameOver = true;
            if (gameEventListener != null) {
                gameEventListener.onGameOver();
            }
            Log.d("GameManager", "Game Over - 3 strikes!");
            timer.pauseTimer();
            return;
        }
        boolean allModulesSolved = checkAllModulesSolved();
        if (allModulesSolved) {
            gameOver = true;
            Log.d("GameManager", "Level completed!");
        }
    }
    public int getBatteryCount() {
        if (currentLevel == null) return 0;

        int batteryCount = 0;
        List<Module> modules = currentLevel.getModules();

        for (Module module : modules) {
            List<Obj> objects = module.getObjects();
            for (Obj obj : objects) {
                if (obj instanceof ObjBattery) {
                    batteryCount++;
                }
            }
        }

        return batteryCount;
    }
    private boolean checkAllModulesSolved() {
        if (currentLevel == null) return false;

        List<Module> modules = currentLevel.getModules();
        for (Module module : modules) {

            if (module instanceof ModuleTimer) {
                ModuleTimer timer = (ModuleTimer) module;
                if (timer.isRunning() || timer.getErrorCount() > 0) {
                    return false;
                }
            }

        }

        return true;
    }
    public void addErrorToGame() {

    }
    private void pauseAllTimers() {
        if (currentLevel == null) return;

        for (Module module : currentLevel.getModules()) {
            if (module instanceof ModuleTimer) {
                ((ModuleTimer) module).pauseTimerForBackground();
            }
        }
    }
    private void resumeAllTimers() {
        if (currentLevel == null) return;

        for (Module module : currentLevel.getModules()) {
            if (module instanceof ModuleTimer) {
                ((ModuleTimer) module).resumeTimerFromBackground();
            }
        }
    }
    public boolean handleTouch(float x, float y) {
        if (currentLevel == null || gameOver || gamePaused) {
            return false;
        }

        return currentLevel.handleTouch(x, y);
    }
    public Level getCurrentLevel() {
        return currentLevel;
    }
    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
    }
    public int getLives() {
        return lives;
    }
    public void setLives(int lives) {
        this.lives = lives;
        if (gameEventListener != null) {
            gameEventListener.onLivesChanged(lives);
        }
    }
    public boolean isGameOver() {
        return gameOver;
    }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    public boolean isGamePaused() {
        return gamePaused;
    }
    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }
    public void removeGameEventListener() {
        this.gameEventListener = null;
    }
    public void cleanup() {
        stopGame();
        currentLevel = null;
        gameEventListener = null;
    }
    public void resetGame() {
        lives = 3;
        gameOver = false;
        gamePaused = false;
        if (currentLevel != null) {
            // Reset all modules
            for (Module module : currentLevel.getModules()) {
                if (module instanceof ModuleTimer) {
                    ((ModuleTimer) module).resetTimer();
                    ((ModuleTimer) module).resetErrors();
                    ((ModuleTimer) module).addRandomObjects();
                }

                for (Obj obj : module.getObjects()) {
                    if (obj instanceof ObjWire) {
                    }
                }
            }
        }

        if (gameEventListener != null) {
            gameEventListener.onLivesChanged(lives);
        }
    }
}