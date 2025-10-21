package com.chroma.number.rpg.core;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Abstract base class for a game component with a fixed timestep loop.
 * Subclasses must implement lifecycle and rendering methods.
 * 
 * @author Chroma
 */
public abstract class GameComponent extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final long serialVersionUID = 1L;

    // Time step for updates (seconds per frame)
    private double dt = 1.0 / 60.0;
    private double fps = 0.0;
    
    public int mouseX, mouseY = 0;
    public double mouseWheelDelta, mouseWheelTotal = 0;
    public boolean mouseLeft, mouseRight, mouseMid, mouseInScreen, mouseLeftClicked, mouseRightClicked, mouseMidClicked = false;
    
    private Map<Integer, Boolean> keyMap = new HashMap<>();
    private Map<Integer, Boolean> keyTouched = new HashMap<>();

    // Thread control flags
    private volatile boolean running = false;
    private volatile boolean resumed = true;

    // Internal thread reference
    private Thread gameThread;

    // Initialization flag for rendering
    private boolean initialized = false;
    
    public GameComponent() {
    	addMouseListener(this);
    	addMouseMotionListener(this);
    	addMouseWheelListener(this);
    	addKeyListener(this);
    }

    /**
     * Starts the game loop in a new thread.
     */
    public void startGame() {
        if (gameThread == null || !gameThread.isAlive()) {
            running = true;
            gameThread = new Thread(this, "GameComponent-Thread");
            gameThread.start();
        }
    }

    /**
     * Stops the game loop and waits for thread to finish.
     */
    public void stopGame() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Pauses the game loop.
     */
    public void pauseGame() {
        resumed = false;
    }

    /**
     * Resumes the game loop.
     */
    public void resumeGame() {
        resumed = true;
    }

    /**
     * Sets the update interval based on desired frame rate.
     * @param frameRate Target frames per second
     */
    public void setFrameRate(double frameRate) {
        dt = 1.0 / frameRate;
    }

    /**
     * Sets the update interval directly.
     * @param delta Time step in seconds
     */
    public void setDelta(double delta) {
        dt = delta;
    }
    
    /**
     * Returns the current estimated frames per second.
     */
    public double getFPS() {
        return fps;
    }
    
    public boolean keyPressed(int keyCode) {
    	return keyMap.getOrDefault(keyCode, false);
    }
    
    public boolean keyTouched(int keyCode) {
    	return keyTouched.getOrDefault(keyCode, false);
    }
    
    public void resetInput() {
        keyMap.clear();
        keyTouched.clear();
        mouseLeft = mouseRight = mouseMid = mouseLeftClicked = mouseRightClicked = mouseMidClicked = mouseInScreen = false;
        mouseWheelDelta = mouseWheelTotal = 0;
    }
    
    public void requestInputFocus() {
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void run() {
        start(); // Subclass-defined initialization

        long lastTime = System.nanoTime();
        double nsPerUpdate = 1_000_000_000.0 * dt;
        double delta = 0;
        long fpsTimer = System.nanoTime();
        

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            if (resumed) {
                boolean shouldUpdate = delta >= 1;
                while (delta >= 1) {
                    update(dt); // Subclass-defined update
                    delta--;
                }
                
                if (shouldUpdate) {
                	repaint();
                	fps = 1.0 / (now - fpsTimer) * 1_000_000_000.0;
                	fpsTimer = now;
                	
                	if (mouseWheelDelta != 0) {
                		mouseWheelDelta = 0;
                	}
                	
                	keyTouched.clear();
                	mouseLeftClicked = mouseRightClicked = mouseMidClicked = false;
                }
            } else {
                delta = 0;
            }

            Thread.yield();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!initialized) {
            initialized = true;
            return;
        }
        paint((Graphics2D) g); // Subclass-defined rendering
    }

    // Abstract lifecycle methods

    /**
     * Invoked once at the beginning of the game loop.
     * Use this to initialize game state, resources, or setup logic.
     */
    protected abstract void start();

    /**
     * Called on every update tick of the game loop.
     * @param dt Time in seconds since the last update (delta time)
     */
    protected abstract void update(double dt);

    /**
     * Called whenever the screen needs to be repainted.
     * @param g The graphics context used for rendering
     */
    protected abstract void paint(Graphics2D g);
    
    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1 -> mouseLeft = true;
            case MouseEvent.BUTTON2 -> mouseMid = true;
            case MouseEvent.BUTTON3 -> mouseRight = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1 -> mouseLeft = false;
            case MouseEvent.BUTTON2 -> mouseMid = false;
            case MouseEvent.BUTTON3 -> mouseRight = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseWheelDelta = e.getPreciseWheelRotation();
        mouseWheelTotal += mouseWheelDelta;
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
    	mouseInScreen = true;
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    	mouseInScreen = false;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    	switch (e.getButton()) {
        case MouseEvent.BUTTON1 -> mouseLeftClicked = true;
        case MouseEvent.BUTTON2 -> mouseMidClicked = true;
        case MouseEvent.BUTTON3 -> mouseRightClicked = true;
    }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {
    	keyMap.put(e.getExtendedKeyCode(), true);
    	keyTouched.put(e.getExtendedKeyCode(), true);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    	keyMap.put(e.getExtendedKeyCode(), false);
    }
}
