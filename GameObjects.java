package com.space.ship.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import java.util.Random;

class SpaceShip {
    private float x, y;
    private float velocityX, velocityY;
    private float maxSpeed = 12f;
    private float acceleration = 0.5f;
    private float friction = 0.94f;
    private int health = 100;
    private int screenX, screenY;
    private Random random = new Random();
    private float engineGlow = 0;
    
    public SpaceShip(float startX, float startY, int screenX, int screenY) {
        this.x = startX;
        this.y = startY;
        this.screenX = screenX;
        this.screenY = screenY;
    }
    
    public void update(VirtualJoystick joystick) {
        if (joystick.isActive()) {
            velocityX += joystick.getForceX() * acceleration;
            velocityY += joystick.getForceY() * acceleration;
            engineGlow = 1.0f;
            
            float speed = (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (speed > maxSpeed) {
                velocityX = (velocityX / speed) * maxSpeed;
                velocityY = (velocityY / speed) * maxSpeed;
            }
        } else {
            engineGlow *= 0.9f;
        }
        
        velocityX *= friction;
        velocityY *= friction;
        
        x += velocityX;
        y += velocityY;
        
        x = Math.max(40, Math.min(screenX - 40, x));
        y = Math.max(40, Math.min(screenY - 40, y));
    }
    
    public void draw(Canvas canvas, Paint paint) {
        RadialGradient shipGradient = new RadialGradient(
            x, y, 35,
            Color.argb(255, 0, 200, 255),
            Color.argb(255, 0, 100, 200),
            Shader.TileMode.CLAMP
        );
        paint.setShader(shipGradient);
        canvas.drawCircle(x, y, 35, paint);
        
        paint.setShader(null);
        paint.setColor(Color.argb(180, 200, 230, 255));
        canvas.drawCircle(x, y, 20, paint);
        
        paint.setColor(Color.argb(255, 150, 200, 255));
        canvas.drawCircle(x, y, 15, paint);
        
        drawWings(canvas, paint);
        drawEngines(canvas, paint);
        drawGlow(canvas, paint);
    }
    
    private void drawWings(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(255, 0, 150, 220));
        canvas.drawRoundRect(x - 45, y - 12, x - 30, y + 12, 10, 10, paint);
        canvas.drawRoundRect(x + 30, y - 12, x + 45, y + 12, 10, 10, paint);
        
        paint.setColor(Color.argb(255, 0, 180, 255));
        canvas.drawRoundRect(x - 42, y - 8, x - 33, y + 8, 5, 5, paint);
        canvas.drawRoundRect(x + 33, y - 8, x + 42, y + 8, 5, 5, paint);
    }
    
    private void drawEngines(Canvas canvas, Paint paint) {
        float enginePower = engineGlow;
        
        RadialGradient leftEngine = new RadialGradient(
            x - 38, y, 15 + enginePower * 10,
            Color.argb(255, 255, (int)(200 + enginePower * 55), 0),
            Color.argb(100, 255, 100, 0),
            Shader.TileMode.CLAMP
        );
        paint.setShader(leftEngine);
        canvas.drawCircle(x - 38, y, 8 + enginePower * 5, paint);
        
        RadialGradient rightEngine = new RadialGradient(
            x + 38, y, 15 + enginePower * 10,
            Color.argb(255, 255, (int)(200 + enginePower * 55), 0),
            Color.argb(100, 255, 100, 0),
            Shader.TileMode.CLAMP
        );
        paint.setShader(rightEngine);
        canvas.drawCircle(x + 38, y, 8 + enginePower * 5, paint);
        
        paint.setShader(null);
    }
    
    private void drawGlow(Canvas canvas, Paint paint) {
        RadialGradient glow = new RadialGradient(
            x, y, 50,
            Color.argb(50, 0, 150, 255),
            Color.argb(0, 0, 100, 200),
            Shader.TileMode.CLAMP
        );
        paint.setShader(glow);
        canvas.drawCircle(x, y, 50, paint);
        paint.setShader(null);
    }
    
    public boolean checkCollision(GameObject other) {
        float dx = x - other.getX();
        float dy = y - other.getY();
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        return distance < (35 + other.getRadius());
    }
    
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    public void reset(float newX, float newY) {
        x = newX;
        y = newY;
        velocityX = 0;
        velocityY = 0;
        health = 100;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelocityX() { return velocityX; }
    public float getVelocityY() { return velocityY; }
    public int getHealth() { return health; }
}

class Planet extends GameObject {
    private int health;
    private int maxHealth;
    private int screenX, screenY;
    private Random random = new Random();
    private float rotation = 0;
    
    public Planet(float x, float y, int health, int screenX, int screenY) {
        super(x, y, 70);
        this.health = health;
        this.maxHealth = health;
        this.screenX = screenX;
        this.screenY = screenY;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        rotation += 1f;
        
        float healthRatio = (float)health / maxHealth;
        int red = (int)(255 * (1 - healthRatio));
        int green = (int)(255 * healthRatio);
        int blue = (int)(150 * healthRatio);
        
        RadialGradient planetGradient = new RadialGradient(
            x, y, radius,
            Color.argb(255, red, green, blue),
            Color.argb(255, red/2, green/2, blue/2),
            Shader.TileMode.CLAMP
        );
        paint.setShader(planetGradient);
        canvas.drawCircle(x, y, radius, paint);
        paint.setShader(null);
        
        drawPlanetDetails(canvas, paint, healthRatio);
        drawAtmosphere(canvas, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(health), x, y + 10, paint);
    }
    
    private void drawPlanetDetails(Canvas canvas, Paint paint, float healthRatio) {
        paint.setColor(Color.argb(200, 50, 50, 50));
        for (int i = 0; i < 4; i++) {
            float angle = rotation + i * 90;
            float continentX = x + (float)Math.cos(Math.toRadians(angle)) * radius * 0.6f;
            float continentY = y + (float)Math.sin(Math.toRadians(angle)) * radius * 0.6f;
            canvas.drawCircle(continentX, continentY, radius * 0.3f, paint);
        }
        
        if (healthRatio > 0.3f) {
            paint.setColor(Color.argb(120, 255, 255, 255));
            for (int i = 0; i < 3; i++) {
                float cloudX = x + (float)Math.cos(Math.toRadians(rotation * 2 + i * 120)) * radius * 0.4f;
                float cloudY = y + (float)Math.sin(Math.toRadians(rotation * 2 + i * 120)) * radius * 0.4f;
                canvas.drawCircle(cloudX, cloudY, radius * 0.2f, paint);
            }
        }
    }
    
    private void drawAtmosphere(Canvas canvas, Paint paint) {
        RadialGradient atmosphere = new RadialGradient(
            x, y, radius + 15,
            Color.argb(80, 100, 200, 255),
            Color.argb(0, 100, 200, 255),
            Shader.TileMode.CLAMP
        );
        paint.setShader(atmosphere);
        canvas.drawCircle(x, y, radius + 15, paint);
        paint.setShader(null);
    }
    
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    public boolean isDestroyed() {
        return health <= 0;
    }
}

class Enemy extends GameObject {
    private float velocityX, velocityY;
    private int screenX, screenY;
    private int level;
    private Random random = new Random();
    private float rotation = 0;
    private float pulse = 0;
    
    public Enemy(int screenX, int screenY, int level) {
        super(0, 0, 45);
        this.screenX = screenX;
        this.screenY = screenY;
        this.level = level;
        initializePosition();
    }
    
    private void initializePosition() {
        int side = random.nextInt(4);
        float speed = 2 + level * 0.5f;
        
        switch (side) {
            case 0:
                x = random.nextFloat() * screenX;
                y = -radius;
                velocityX = (random.nextFloat() - 0.5f) * speed;
                velocityY = speed;
                break;
            case 1:
                x = screenX + radius;
                y = random.nextFloat() * screenY;
                velocityX = -speed;
                velocityY = (random.nextFloat() - 0.5f) * speed;
                break;
            case 2:
                x = random.nextFloat() * screenX;
                y = screenY + radius;
                velocityX = (random.nextFloat() - 0.5f) * speed;
                velocityY = -speed;
                break;
            case 3:
                x = -radius;
                y = random.nextFloat() * screenY;
                velocityX = speed;
                velocityY = (random.nextFloat() - 0.5f) * speed;
                break;
        }
    }
    
    public void update(SpaceShip ship) {
        float dx = ship.getX() - x;
        float dy = ship.getY() - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            float baseSpeed = 2 + level * 0.4f;
            velocityX += (dx / distance) * 0.08f * baseSpeed;
            velocityY += (dy / distance) * 0.08f * baseSpeed;
            
            float currentSpeed = (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            float maxSpeed = 3 + level * 0.5f;
            if (currentSpeed > maxSpeed) {
                velocityX = (velocityX / currentSpeed) * maxSpeed;
                velocityY = (velocityY / currentSpeed) * maxSpeed;
            }
        }
        
        x += velocityX;
        y += velocityY;
        rotation += 4;
        pulse = (float)Math.sin(System.currentTimeMillis() * 0.01) * 0.2f + 0.8f;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        RadialGradient enemyGradient = new RadialGradient(
            x, y, radius * pulse,
            Color.argb(255, 255, 50, 50),
            Color.argb(255, 150, 0, 0),
            Shader.TileMode.CLAMP
        );
        paint.setShader(enemyGradient);
        canvas.drawCircle(x, y, radius * pulse, paint);
        
        paint.setShader(null);
        paint.setColor(Color.argb(255, 255, 200, 200));
        canvas.drawCircle(x, y, radius * 0.6f * pulse, paint);
        
        drawSpikes(canvas, paint);
        drawEyes(canvas, paint);
        drawDangerAura(canvas, paint);
    }
    
    private void drawSpikes(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(255, 200, 0, 0));
        int spikeCount = 8;
        
        for (int i = 0; i < spikeCount; i++) {
            float angle = rotation + i * (360 / spikeCount);
            float spikeLength = radius * 1.3f;
            float spikeX = x + (float)Math.cos(Math.toRadians(angle)) * spikeLength;
            float spikeY = y + (float)Math.sin(Math.toRadians(angle)) * spikeLength;
            
            canvas.drawCircle(spikeX, spikeY, 8, paint);
        }
    }
    
    private void drawEyes(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(255, 0, 255, 0));
        
        float leftEyeX = x - radius * 0.3f;
        float leftEyeY = y - radius * 0.2f;
        canvas.drawCircle(leftEyeX, leftEyeY, 6, paint);
        
        float rightEyeX = x + radius * 0.3f;
        float rightEyeY = y - radius * 0.2f;
        canvas.drawCircle(rightEyeX, rightEyeY, 6, paint);
        
        paint.setColor(Color.BLACK);
        canvas.drawCircle(leftEyeX, leftEyeY, 3, paint);
        canvas.drawCircle(rightEyeX, rightEyeY, 3, paint);
    }
    
    private void drawDangerAura(Canvas canvas, Paint paint) {
        RadialGradient aura = new RadialGradient(
            x, y, radius + 25,
            Color.argb(60, 255, 0, 0),
            Color.argb(0, 255, 0, 0),
            Shader.TileMode.CLAMP
        );
        paint.setShader(aura);
        canvas.drawCircle(x, y, radius + 25, paint);
        paint.setShader(null);
    }
    
    public boolean isOutOfScreen() {
        return x < -100 || x > screenX + 100 || y < -100 || y > screenY + 100;
    }
}

class Star {
    private float x, y;
    private float size;
    private float speed;
    private float brightness;
    private Random random = new Random();
    
    public Star(float x, float y, float size, float speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.brightness = random.nextFloat() * 0.7f + 0.3f;
    }
    
    public void update(float shipVelX, float shipVelY) {
        x -= shipVelX * speed * 0.1f;
        y -= shipVelY * speed * 0.1f;
        
        if (x < -20) x = 1080;
        if (x > 1080) x = -20;
        if (y < -20) y = 1920;
        if (y > 1920) y = -20;
    }
    
    public void draw(Canvas canvas, Paint paint) {
        int alpha = (int)(255 * brightness);
        paint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawCircle(x, y, size, paint);
        
        paint.setColor(Color.argb(alpha/2, 255, 255, 255));
        canvas.drawCircle(x, y, size * 2, paint);
    }
}

class VirtualJoystick {
    private float centerX, centerY;
    private float baseRadius, handleRadius;
    private float handleX, handleY;
    private boolean isActive = false;
    
    public VirtualJoystick(float centerX, float centerY, float baseRadius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.baseRadius = baseRadius;
        this.handleRadius = baseRadius * 0.4f;
        resetHandle();
    }
    
    public void setActive(boolean active, float touchX, float touchY) {
        isActive = active;
        if (active) {
            float dx = touchX - centerX;
            float dy = touchY - centerY;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (distance <= baseRadius) {
                handleX = touchX;
                handleY = touchY;
            } else {
                handleX = centerX + (dx / distance) * baseRadius;
                handleY = centerY + (dy / distance) * baseRadius;
            }
        } else {
            resetHandle();
        }
    }
    
    private void resetHandle() {
        handleX = centerX;
        handleY = centerY;
    }
    
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(180, 80, 80, 80));
        canvas.drawCircle(centerX, centerY, baseRadius, paint);
        
        paint.setColor(Color.argb(220, 200, 200, 200));
        canvas.drawCircle(handleX, handleY, handleRadius, paint);
        
        paint.setColor(Color.argb(255, 100, 100, 100));
        canvas.drawCircle(handleX, handleY, handleRadius * 0.5f, paint);
    }
    
    public float getForceX() {
        return (handleX - centerX) / baseRadius;
    }
    
    public float getForceY() {
        return (handleY - centerY) / baseRadius;
    }
    
    public boolean isActive() {
        return isActive;
    }
}

abstract class GameObject {
    protected float x, y;
    protected int radius;
    
    public GameObject(float x, float y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    public abstract void draw(Canvas canvas, Paint paint);
    
    public float getX() { return x; }
    public float getY() { return y; }
    public int getRadius() { return radius; }
                            }
