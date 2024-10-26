package de.omegasystems.renderer.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import javax.swing.Timer;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.utility.Observer;

public class TokenTooltipComponent extends MouseAdapter implements RenderingComponent, Observer<TokenHandler> {

    private Renderer renderer;
    private TokenHandler tokenHandler;

    private Token hoveredToken;
    private Token pendingToken;
    private Timer tooltipTimer;
    private Timer fadeInTimer;
    private float opacity;

    private static final int TOOLTIP_DELAY = 500; // Delay in milliseconds
    private static final int FADE_IN_DURATION = 100; // Duration in milliseconds
    private static final int FADE_IN_INTERVAL = 20; // Interval in milliseconds

    public TokenTooltipComponent(TokenHandler th) {
        this.tokenHandler = th;
        if (tokenHandler == null)
            throw new IllegalArgumentException("[" + this.getClass().getCanonicalName()
                    + "] Tokenhandler was null during initialization");
        tokenHandler.addObserver(this);

        tooltipTimer = new Timer(TOOLTIP_DELAY, e -> startFadeIn());
        tooltipTimer.setRepeats(false);

        fadeInTimer = new Timer(FADE_IN_INTERVAL, e -> updateOpacity());
        fadeInTimer.setRepeats(true);
    }

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
        renderer.addMouseListener(this);
        renderer.addMouseMotionListener(this);
    }

    @Override
    public void update(TokenHandler newVal) {
        if (hoveredToken == null)
            return;
        if (tokenHandler.hasToken(hoveredToken))
            return;

        hoveredToken = null;
        notifyChange();
    }

    @Override
    public void draw(Graphics2D g, Dimension canvasSize) {
        // Hovered Token Infobox drawing

        // The second condition should never occur as removed tokens also get cleared
        // from highlighting
        if (hoveredToken == null || !tokenHandler.hasToken(hoveredToken))
            return;

        double scale = 1 / renderer.getTranslationhandler().getScale();

        int distanceBetweenLines = (int) (3 * scale);
        int hPadding = (int) (4 * scale);
        int vPadding = (int) (4 * scale);

        int hTokenDistance = (int) (10 * scale);

        g.setFont(new Font("Georgia", Font.BOLD, (int) (15 * scale)));
        Font f = g.getFont();

        // Calculate bounds of Textbox
        Dimension drawingDimensions = new Dimension();

        Rectangle2D titleBounds = f.getStringBounds(hoveredToken.getName(), g.getFontRenderContext());
        drawingDimensions = new Dimension((int) Math.max(drawingDimensions.getWidth(), titleBounds.getWidth()),
                (int) (drawingDimensions.getHeight() + distanceBetweenLines + titleBounds.getHeight()));
        int titleHeight = (int) titleBounds.getHeight();

        titleBounds = f.getStringBounds(hoveredToken.getFriendStatus().toString(), g.getFontRenderContext());
        drawingDimensions = new Dimension((int) Math.max(drawingDimensions.getWidth(), titleBounds.getWidth()),
                (int) (drawingDimensions.getHeight() + distanceBetweenLines + titleBounds.getHeight()));
        int friendlienessHeight = (int) titleBounds.getHeight();

        titleBounds = f.getStringBounds(hoveredToken.getMovement(), g.getFontRenderContext());
        drawingDimensions = new Dimension((int) Math.max(drawingDimensions.getWidth(), titleBounds.getWidth()),
                (int) (drawingDimensions.getHeight() + distanceBetweenLines + titleBounds.getHeight()));
        int movementHeight = (int) titleBounds.getHeight();

        drawingDimensions = new Dimension((int) (drawingDimensions.getWidth() + hPadding * 2),
                (int) (drawingDimensions.getHeight() + vPadding * 2));

        // Now that we have the bounds of the Strings, we can check to wich side of the
        // Token we can draw it
        int posX = (int) (hoveredToken.getPosition().x);
        int posY = (int) (hoveredToken.getPosition().y);
        int imageSize = tokenHandler.calculateImageSizeFor(hoveredToken);

        int drawingPosX = posX + imageSize + hTokenDistance;
        int drawingPosY = posY;

        if (drawingPosX + drawingDimensions.getWidth() > canvasSize.getWidth())
            drawingPosX = (int) (posX - hTokenDistance - drawingDimensions.getWidth());

        if (drawingPosY + drawingDimensions.getHeight() > canvasSize.getHeight())
            drawingPosY = (int) (canvasSize.getHeight() - drawingDimensions.getHeight());

        // Draw the actual Thing
        g.setColor(new Color(192, 192, 192, (int) (opacity * 255)));
        g.setStroke(new BasicStroke((float) (2.0 * renderer.getTranslationhandler().getScale())));
        g.fillRect(drawingPosX, drawingPosY, (int) drawingDimensions.getWidth(), (int) drawingDimensions.getHeight());
        g.setColor(new Color(0, 0, 0, (int) (opacity * 255)));

        int curPosX = drawingPosX + hPadding;
        int curPosY = drawingPosY + vPadding + titleHeight;

        g.drawString(hoveredToken.getName(), curPosX, curPosY);
        curPosY += distanceBetweenLines + friendlienessHeight;
        g.drawString(hoveredToken.getFriendStatus().toString(), curPosX, curPosY);
        curPosY += distanceBetweenLines + movementHeight;
        g.drawString(hoveredToken.getMovement(), curPosX, curPosY);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (hoveredToken == null)
            return;

        hoveredToken = null;
        tooltipTimer.stop();
        fadeInTimer.stop();
        opacity = 0;
        notifyChange();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        var newToken = tokenHandler.getTokenFromPosition(e);
        if (Objects.equals(newToken, hoveredToken))
            return;

        pendingToken = newToken;
        if (newToken == null) {
            hoveredToken = null;
            tooltipTimer.stop();
            fadeInTimer.stop();
            opacity = 0;
            notifyChange();
        } else {
            tooltipTimer.restart();
        }
    }

    private void startFadeIn() {
        opacity = 0;
        hoveredToken = pendingToken;
        fadeInTimer.restart();
    }

    private void updateOpacity() {
        opacity += (float) FADE_IN_INTERVAL / FADE_IN_DURATION;
        if (opacity >= 1) {
            opacity = 1;
            fadeInTimer.stop();
        }
        notifyChange();
    }

    private void notifyChange() {
        renderer.scheduleRedraw();
    }
}
