package de.omegasystems.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.Renderer.DrawingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.utility.Observer;

public class TokenTooltipComponent extends MouseAdapter implements DrawingComponent, Observer<TokenHandler> {

    private Renderer renderer;
    private TokenHandler tokenHandler;

    private Token hoveredToken;

    public TokenTooltipComponent(Renderer renderer, TokenHandler th) {
        this.renderer = renderer;
        this.tokenHandler = th;
        if (renderer == null || tokenHandler == null)
            throw new IllegalArgumentException("[" + this.getClass().getCanonicalName()
                    + "] Renderer or Tokenhandler were null during initialization");

        tokenHandler.addObserver(this);
        renderer.addRenderCallback(this);
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
    public void draw(Graphics2D g, Dimension canvasSize, double scale) {
        // Hovered Token Infobox drawing

        // The second condition should never occur as removed tokens also get cleared
        // from highlighting
        if (hoveredToken == null || !tokenHandler.hasToken(hoveredToken))
            return;

        int distanceBetweenLines = 3;
        int hPadding = 4;
        int vPadding = 4;

        int hTokenDistance = 10;

        g.setFont(new Font("Georgia", Font.BOLD, 15));
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
        int posX = (int) (hoveredToken.getPosition().x * scale);
        int posY = (int) (hoveredToken.getPosition().y * scale);
        int imageSize = tokenHandler.calculateImageSizeFor(hoveredToken);

        int drawingPosX = posX + imageSize + hTokenDistance;
        int drawingPosY = posY;

        if (drawingPosX + drawingDimensions.getWidth() > canvasSize.getWidth())
            drawingPosX = (int) (posX - hTokenDistance - drawingDimensions.getWidth());

        if (drawingPosY + drawingDimensions.getHeight() > canvasSize.getHeight())
            drawingPosY = (int) (canvasSize.getHeight() - drawingDimensions.getHeight());

        // Draw the actual Thing
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(2.0f));
        g.fillRect(drawingPosX, drawingPosY, (int) drawingDimensions.getWidth(), (int) drawingDimensions.getHeight());
        g.setColor(Color.BLACK);

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
        notifyChange();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        var newToken = tokenHandler.getTokenFromPosition(e);
        if (Objects.equals(newToken, hoveredToken))
            return;
        hoveredToken = newToken;
        notifyChange();
    }

    // @Override
    // public void mouseDragged(MouseEvent e) {
    // if (hoveredToken == null)
    // return;
    // hoveredToken = null;
    // notifyChange();
    // }

    private void notifyChange() {
        renderer.scheduleRedraw();
    }

}
