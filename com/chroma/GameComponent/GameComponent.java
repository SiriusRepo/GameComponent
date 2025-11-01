package com.chroma.GameComponent;

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
    private double fps = 1.0 / dt;
    private double fpsSmoothing = 0.9;
    
    public int mouseX, mouseY = 0;
    public double mouseWheelDelta, mouseWheelTotal = 0;
    public boolean mouseLeft, mouseRight, mouseMid, mouseInScreen, mouseLeftClicked, mouseRightClicked, mouseMidClicked = false;
    
    private Map<Integer, Boolean> keyHeld = new HashMap<>();
    private Map<Integer, Boolean> keyPressed = new HashMap<>();
    private Map<Integer, Boolean> keyClicked = new HashMap<>();

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
            requestInputFocus();
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
     * @param desiredFPS Desired frames per second
     */
    public void setDesiredFPS(double desiredFPS) {
        dt = 1.0 / desiredFPS;
    }

    /**
     * Sets the update interval directly.
     * @param desiredDelta Desired time step in seconds
     */
    public void setDesiredDelta(double desiredDelta) {
        dt = desiredDelta;
    }
    
    /**
     * Returns the current estimated frames per second.
     */
    public double getFPS() {
        return fps;
    }
    
    /**
     * Determines if a certain key is held.
     * @param keyCode The key-code of the key
     */
    public boolean isKeyHeld(int keyCode) {
    	return keyHeld.getOrDefault(keyCode, false);
    }
    
    /**
     * Determines if a certain key is pressed.
     * Holding the key makes this fire repeatedly.
     * @param keyCode The key-code of the key
     */
    public boolean isKeyPressed(int keyCode) {
    	return keyPressed.getOrDefault(keyCode, false);
    }
    
    /**
     * Determines if a certain key is clicked.
     * Fires when the key starts being pressed.
     * @param keyCode The key-code of the key
     */
    public boolean isKeyClicked(int keyCode) {
    	return keyClicked.getOrDefault(keyCode, false);
    }
    
    /**
     * Resets the input detection.
     * Includes held keys, clicked keys, pressed keys and mouse buttons, position, status and wheel.
     */
    public void resetInput() {
        keyHeld.clear();
        keyClicked.clear();
        keyPressed.clear();
        mouseLeft = mouseRight = mouseMid = mouseLeftClicked = mouseRightClicked = mouseMidClicked = mouseInScreen = false;
        mouseWheelDelta = mouseWheelTotal = 0;
    }
    
    /**
     * Sets the FPS Smoothing.
     * @param smoothing The desired smoothing.
     * Tip: With a higher value, the FPS is smoother, so it takes longer to catch up.
     * With a lower value, the FPS is chopier, so it adjusts faster.
     * Default smoothing is 0.9.
     */
    public void setFPSSmoothing(double smoothing) {
    	fpsSmoothing = smoothing;
    }
    
    /**
     * Helper function to focus the user to the window, 
     * making it successfully detect inputs.
     */
    public void requestInputFocus() {
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void run() {
        start(); // Subclass-defined initialization

        long lastTime = System.nanoTime();
        double delta = 0;
        long fpsTimer = System.nanoTime();
        

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / (1_000_000_000.0 * dt);
            lastTime = now;

            if (resumed) {
                boolean shouldUpdate = delta >= 1;
                while (delta >= 1) {
                    update(dt); // Subclass-defined update
                    delta--;
                }
                
                if (shouldUpdate) {
                	repaint();
                	fps = fps * fpsSmoothing + (1.0e9 / (now - fpsTimer)) * (1 - fpsSmoothing);
                	fpsTimer = now;
                	
                	if (mouseWheelDelta != 0) {
                		mouseWheelDelta = 0;
                	}
                	
                	keyClicked.clear();
                	keyPressed.clear();
                	mouseLeftClicked = mouseRightClicked = mouseMidClicked = false;
                }
            } else {
                delta = 0;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
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
    	int code = e.getExtendedKeyCode();
    	keyPressed.put(code, true);
    	if (keyHeld.getOrDefault(code, false)) return;
    	keyHeld.put(code, true);
    	keyClicked.put(code, true);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    	keyHeld.put(e.getExtendedKeyCode(), false);
    }
}
