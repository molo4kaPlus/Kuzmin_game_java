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
        void onGameWon();
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
        if (module instanceof  ModuleWords){
            initWords();
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

            if (obj instanceof ObjButton) {
                handleButton((ObjButton) obj);
            }
            obj.update();
        }
    }
    private void handleWire(ObjWire wire, Module module) {
        boolean shouldBeCut = false;
        int color = wire.getColor();
        if(color == Color.WHITE){
            shouldBeCut = true;
        } else if (color == Color.GREEN) {
            if (getBatteryCount() % 2 == 0){
                shouldBeCut = true;
            }
        }

        if (!shouldBeCut && wire.isCut() && !wire.checked){
            timer.addError();
            wire.checked = true;
            wire.setSolved(true);
        } else if (shouldBeCut && wire.isCut()) {
            wire.setSolved(true);
        } else if (shouldBeCut && !wire.isCut()) {
            wire.setSolved(false);
        }
    }
    private void handleButton(ObjButton button){
        boolean shouldBePressed = false;

        if (button.getColor() == Color.GREEN){
            shouldBePressed = true;
        }

        if (button.isPressed() && shouldBePressed){
            button.setSolved(true);
        } else if (button.isPressed() && !shouldBePressed) {
            button.setSolved(false);
        } else if (!button.isPressed() && !shouldBePressed) {
            button.setSolved(true);
        } else if (!button.isPressed() && shouldBePressed) {
            button.setSolved(false);
        }
    }
    private void handleBattery(ObjBattery battery, Module module) {
        battery.setSolved(true);
    }
    private void handleWords(ModuleWords words){

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
            if (gameEventListener != null) {
                gameEventListener.onGameWon();
            }
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
            if (!module.isSolved()) {
                return false;
            }
        }
        return true;
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
    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
        initWords();
    }
    private void initWords(){
        int ID;
        if(currentLevel.hasRJ45()){
            ID = (currentLevel.getSerial() % 10) - 1;
            ((ModuleWords)  currentLevel.getModuleAt(0,1)).setCorrectID(ID);
        } else if (currentLevel.hasPShalf()) {
            ID = (currentLevel.getSerial() % 10);
            ((ModuleWords)  currentLevel.getModuleAt(0,1)).setCorrectID(ID);
        }
        else {

        }
    }
    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
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
                if (module instanceof ModuleWords) {
                    ((ModuleWords) module).reset();
                    ((ModuleWords) module).clearObjects();
                    ((ModuleWords) module).addRandomObjects();
                    initWords();
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