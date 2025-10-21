package example;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.chroma.GameComponent

public class CountingPanel extends GameComponent {

    private static final long serialVersionUID = 1L;
    private double counter = 0;
    private Font font;

    public CountingPanel() {
        font = new Font("Consolas", Font.BOLD, 64);
    }

    @Override
    public void start() {
        // Initialization logic if needed
    }

    @Override
    public void update(double dt) {
        // Count up at 1 per second
        counter += dt;
    }

    @Override
    protected void paint(Graphics2D g) {
        // Enable smooth text rendering
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw number
        g.setFont(font);
        g.setColor(Color.WHITE);
        String text = String.format("%.2f", counter);

        int textWidth = g.getFontMetrics().stringWidth(text);
        int textHeight = g.getFontMetrics().getAscent();

        g.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2);
    }
}
