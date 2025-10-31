package com.space.ship.game;

import android.content.Context;
import android.content.SharedPreferences;

public class GameState {
    private static final String PREFS_NAME = "SpaceShipGame";
    private long coins;
    private int score;
    private int currentLevel;
    private int destroyedPlanets;

    public GameState() {
        coins = 1000000;
        score = 0;
        currentLevel = 1;
        destroyedPlanets = 0;
    }
    
    public void planetDestroyed() {
        destroyedPlanets++;
        score += 100 * currentLevel;
        coins += 50000 * currentLevel;
    }
    
    public void shipDestroyed() {
        score = Math.max(0, score - 50);
        coins = Math.max(1000000, coins - 100000);
    }
    
    public void nextLevel() {
        currentLevel++;
        destroyedPlanets = 0;
        coins += 1000000;
        score += 1000;
    }
    
    public void saveGame(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("coins", coins);
        editor.putInt("score", score);
        editor.putInt("level", currentLevel);
        editor.apply();
    }
    
    public void loadGame(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        coins = prefs.getLong("coins", 1000000);
        score = prefs.getInt("score", 0);
        currentLevel = prefs.getInt("level", 1);
    }
    
    public long getCoins() { return coins; }
    public int getScore() { return score; }
    public int getCurrentLevel() { return currentLevel; }
    public int getDestroyedPlanets() { return destroyedPlanets; }
}
