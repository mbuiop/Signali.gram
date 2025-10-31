package com.space.ship.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ParticleSystem {
    private List<Particle> particles;
    private Random random;
    
    public ParticleSystem() {
        particles = new ArrayList<>();
        random = new Random();
    }
    
    public void createExplosion(float x, float y, int count, int baseColor) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * 360;
            float speed = 2 + random.nextFloat() * 8;
            float size = 2 + random.nextFloat() * 5;
            int life = 20 + random.nextInt(30);
            
            int r = Color.red(baseColor);
            int g = Color.green(baseColor);
            int b = Color.blue(baseColor);
            
            particles.add(new Particle(
                x, y,
                (float)Math.cos(Math.toRadians(angle)) * speed,
                (float)Math.sin(Math.toRadians(angle)) * speed,
                size,
                Color.argb(255, r, g, b),
                life
            ));
        }
    }
    
    public void createImpact(float x, float y, int count, int baseColor) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * 360;
            float speed = 1 + random.nextFloat() * 4;
            float size = 1 + random.nextFloat() * 3;
            int life = 10 + random.nextInt(20);
            
            int r = Math.min(255, Color.red(baseColor) + 50);
            int g = Math.min(255, Color.green(baseColor) + 50);
            int b = Math.min(255, Color.blue(baseColor) + 50);
            
            particles.add(new Particle(
                x, y,
                (float)Math.cos(Math.toRadians(angle)) * speed,
                (float)Math.sin(Math.toRadians(angle)) * speed,
                size,
                Color.argb(255, r, g, b),
                life
            ));
        }
    }
    
    public void update() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }
    
    public void draw(Canvas canvas, Paint paint) {
        for (Particle p : particles) {
            p.draw(canvas, paint);
        }
    }
}

class Particle {
    private float x, y;
    private float velocityX, velocityY;
    private float size;
    private int color;
    private int life;
    private int maxLife;
    
    public Particle(float x, float y, float velocityX, float velocityY, float size, int color, int life) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.size = size;
        this.color = color;
        this.life = life;
        this.maxLife = life;
    }
    
    public void update() {
        x += velocityX;
        y += velocityY;
        velocityX *= 0.98f;
        velocityY *= 0.98f;
        life--;
    }
    
    public void draw(Canvas canvas, Paint paint) {
        float lifeRatio = (float)life / maxLife;
        int alpha = (int)(255 * lifeRatio);
        int particleColor = Color.argb(alpha, 
            Color.red(color), Color.green(color), Color.blue(color));
        
        paint.setColor(particleColor);
        canvas.drawCircle(x, y, size * lifeRatio, paint);
        
        paint.setColor(Color.argb(alpha/2, 255, 255, 255));
        canvas.drawCircle(x, y, size * lifeRatio * 2, paint);
    }
    
    public boolean isDead() {
        return life <= 0;
    }
}
