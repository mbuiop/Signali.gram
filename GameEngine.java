package com.space.ship.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine extends SurfaceView implements Runnable {
    private Thread gameThread;
    private volatile boolean playing;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private int screenWidth, screenHeight;
    
    private SpaceShip spaceShip;
    private VirtualJoystick joystick;
    private List<Planet> planets;
    private List<Enemy> enemies;
    private List<Star> stars;
    private GameState gameState;
    private ParticleSystem particleSystem;
    private Random random;
    
    private long lastTime;

    public GameEngine(Context context, int screenX, int screenY) {
        super(context);
        this.screenWidth = screenX;
        this.screenHeight = screenY;
        
        initializeEngine();
    }

    private void initializeEngine() {
        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        random = new Random();
        
        spaceShip = new SpaceShip(screenWidth / 2, screenHeight / 2, screenWidth, screenHeight);
        joystick = new VirtualJoystick(screenWidth / 2, screenHeight - 200, 120);
        planets = new ArrayList<>();
        enemies = new ArrayList<>();
        stars = new ArrayList<>();
        gameState = new GameState();
        particleSystem = new ParticleSystem();
        
        createStars();
        startNewLevel();
        
        setFocusable(true);
    }

    private void createStars() {
        for (int i = 0; i < 200; i++) {
            stars.add(new Star(
                random.nextInt(screenWidth),
                random.nextInt(screenHeight),
                random.nextFloat() * 3 + 1,
                random.nextFloat() * 0.5f + 0.1f
            ));
        }
    }

    private void startNewLevel() {
        planets.clear();
        enemies.clear();
        
        for (int i = 0; i < 20; i++) {
            float x = random.nextFloat() * (screenWidth - 200) + 100;
            float y = random.nextFloat() * (screenHeight - 400) + 100;
            int health = gameState.getCurrentLevel() * 10 + 50;
            planets.add(new Planet(x, y, health, screenWidth, screenHeight));
        }
        
        for (int i = 0; i < 10; i++) {
            enemies.add(new Enemy(screenWidth, screenHeight, gameState.getCurrentLevel()));
        }
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            controlFPS();
        }
    }

    private void update() {
        spaceShip.update(joystick);
        
        for (Enemy enemy : enemies) {
            enemy.update(spaceShip);
            
            if (spaceShip.checkCollision(enemy)) {
                particleSystem.createExplosion(spaceShip.getX(), spaceShip.getY(), 50, Color.RED);
                gameState.shipDestroyed();
                resetGame();
                return;
            }
        }
        
        for (int i = planets.size() - 1; i >= 0; i--) {
            Planet planet = planets.get(i);
            if (spaceShip.checkCollision(planet)) {
                planet.takeDamage(25);
                particleSystem.createImpact(planet.getX(), planet.getY(), 20, Color.CYAN);
                
                if (planet.isDestroyed()) {
                    planets.remove(i);
                    gameState.planetDestroyed();
                    particleSystem.createExplosion(planet.getX(), planet.getY(), 80, Color.YELLOW);
                }
            }
        }
        
        particleSystem.update();
        
        for (Star star : stars) {
            star.update(spaceShip.getVelocityX(), spaceShip.getVelocityY());
        }
        
        if (planets.isEmpty()) {
            gameState.nextLevel();
            startNewLevel();
        }
        
        enemies.removeIf(enemy -> enemy.isOutOfScreen());
        if (enemies.size() < 10 && random.nextInt(100) < 2) {
            enemies.add(new Enemy(screenWidth, screenHeight, gameState.getCurrentLevel()));
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            
            drawCinematicBackground(canvas);
            particleSystem.draw(canvas, paint);
            
            for (Planet planet : planets) {
                planet.draw(canvas, paint);
            }
            
            for (Enemy enemy : enemies) {
                enemy.draw(canvas, paint);
            }
            
            spaceShip.draw(canvas, paint);
            joystick.draw(canvas, paint);
            drawHUD(canvas);
            
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawCinematicBackground(Canvas canvas) {
        RadialGradient gradient = new RadialGradient(
            screenWidth / 2, screenHeight / 2, screenHeight,
            Color.argb(255, 5, 5, 25),
            Color.argb(255, 0, 0, 10),
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        paint.setShader(null);
        
        for (Star star : stars) {
            star.draw(canvas, paint);
        }
        
        drawNebulas(canvas);
    }

    private void drawNebulas(Canvas canvas) {
        paint.setColor(Color.argb(30, 50, 100, 255));
        canvas.drawCircle(screenWidth * 0.3f, screenHeight * 0.2f, 300, paint);
        
        paint.setColor(Color.argb(25, 150, 50, 200));
        canvas.drawCircle(screenWidth * 0.7f, screenHeight * 0.6f, 250, paint);
        
        paint.setColor(Color.argb(20, 255, 50, 50));
        canvas.drawCircle(screenWidth * 0.5f, screenHeight * 0.8f, 200, paint);
    }

    private void drawHUD(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setShadowLayer(3, 2, 2, Color.BLACK);
        
        canvas.drawText("LEVEL: " + gameState.getCurrentLevel(), 30, 50, paint);
        canvas.drawText("PLANETS: " + planets.size() + "/20", 30, 100, paint);
        canvas.drawText("SCORE: " + gameState.getScore(), screenWidth - 300, 50, paint);
        canvas.drawText("COINS: " + formatCoins(gameState.getCoins()), screenWidth - 300, 100, paint);
        
        paint.setShadowLayer(0, 0, 0, 0);
    }

    private String formatCoins(long coins) {
        if (coins >= 1000000) {
            return String.format("%.1fM", coins / 1000000.0);
        } else if (coins >= 1000) {
            return String.format("%.1fK", coins / 1000.0);
        }
        return String.valueOf(coins);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (y > screenHeight - 400) {
                    joystick.setActive(true, x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                joystick.setActive(false, x, y);
                break;
        }
        return true;
    }

    private void controlFPS() {
        try {
            long currentTime = System.currentTimeMillis();
            long sleepTime = 16 - (currentTime - lastTime);
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
            lastTime = currentTime;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        spaceShip.reset(screenWidth / 2, screenHeight / 2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pauseGame() {
        playing = false;
    }

    public void resumeGame() {
        if (!playing) {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void destroyGame() {
        playing = false;
        gameState.saveGame(getContext());
    }
                         }
