package de.omegasystems.renderer.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.core.TokenSelectionHandler;
import de.omegasystems.dataobjects.Friendlieness;
import de.omegasystems.dataobjects.TokenData;

public class DragDropHandlingComponent implements RenderingComponent {

    public static final DataFlavor TOKEN_FLAVOR;
    static {
        try {
            TOKEN_FLAVOR = new DataFlavor("application/x-token-list; class=java.util.ArrayList");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static record TokenTransferData(
            File pictureFile,
            String name,
            String description,
            Double size,
            Integer initiative,
            String movement,
            Friendlieness friendStatus) implements Serializable {
    }

    private static TokenTransferData createTransferData(Token token) {
        return new TokenTransferData(token.getPictureFile(), token.getName(), token.getDescription(), token.getSize(),
                token.getInitiative(), token.getMovement(), token.getFriendStatus());
    }

    private static Token createToken(TokenHandler tokenHandler, TokenTransferData data) {
        TokenData tokenData = new TokenData();
        tokenData.getPictureFile().setValue(data.pictureFile);
        tokenData.getName().setValue(data.name);
        tokenData.getDescription().setValue(data.description);
        tokenData.getSize().setValue(data.size);
        tokenData.getInitiative().setValue(data.initiative);
        tokenData.getMovement().setValue(data.movement);
        tokenData.getFriendStatus().setValue(data.friendStatus);
        return new Token(tokenData, tokenHandler);
    }

    /**
     * A class that represents a list of tokens that can be transfered between two
     * apps (or windows)
     */
    public static class TokenTransferable implements Transferable {
        private final ArrayList<TokenTransferData> tokens;

        public TokenTransferable(Collection<? extends Token> rawTokens) {
            this.tokens = new ArrayList<>();
            rawTokens.forEach(token -> tokens.add(createTransferData(token)));
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { TOKEN_FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return TOKEN_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (TOKEN_FLAVOR.equals(flavor)) {
                return tokens;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    private Renderer renderer;

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;

        renderer.setTransferHandler(transferHandler);
        renderer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point mouseLocation = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), renderer.getFrame().getContentPane());
    
                // Use the content pane's bounds for the bounds check
                Rectangle contentBounds = renderer.getFrame().getContentPane().getBounds();
                if (contentBounds.contains(mouseLocation))
                    return;
                transferHandler.exportAsDrag(renderer.getRenderingComponent(), e, TransferHandler.COPY);
            }
        });

    }

    @Override
    public void draw(Graphics2D g, Dimension size) {
    }

    private boolean handleImportTokens(List<TokenTransferData> tokens, Point sourcePos) {
        TokenHandler tokenHandler = renderer.getComponentImplementing(TokenHandler.class);
        if (tokenHandler == null)
            return false;

        for (TokenTransferData tokenData : tokens) {
            var token = createToken(tokenHandler, tokenData);
            var srcPos = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(sourcePos);
            token.setPosition(new Point2D.Double(srcPos.getX(), srcPos.getY()));
            tokenHandler.addToken(token);
        }

        return true;

    }

    private void handleExportTokensDone(List<TokenData> tokens) {
        var tokenSelectionHandler = renderer.getComponentImplementing(TokenSelectionHandler.class);
        if (tokenSelectionHandler == null)
            return;
        tokenSelectionHandler.removeAllHighlightedTokens();
    }

    private void handleImport(Point sourcePos, File file) {
        TokenHandler tokenHandler = renderer.getComponentImplementing(TokenHandler.class);

        if (tokenHandler == null)
            return;

        String filenameWithoutExtension = file.getName().substring(0,
                file.getName().lastIndexOf('.'));

        var tokenData = new TokenData();
        tokenData.getName().setValue(filenameWithoutExtension);
        tokenData.getPictureFile().setValue(file);

        var token = new Token(tokenData, tokenHandler);
        var srcPos = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(sourcePos);
        token.setPosition(new Point2D.Double(srcPos.getX(), srcPos.getY()));
        tokenHandler.addToken(token);
    }

    private TransferHandler transferHandler = new TransferHandler() {
        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || support.isDataFlavorSupported(TOKEN_FLAVOR);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            Transferable transferable = support.getTransferable();
            try {
                if (support.isDataFlavorSupported(TOKEN_FLAVOR)) {
                    return handleImportTokens((ArrayList<TokenTransferData>) transferable.getTransferData(TOKEN_FLAVOR),
                            support.getDropLocation().getDropPoint());
                }

                List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : files) {
                    if (!file.isFile())
                        continue;

                    try {
                        ImageIO.read(file);

                        handleImport(support.getDropLocation().getDropPoint(), file);
                    } catch (IOException e) {
                        System.err.println("Failed to read image file: " + file.getName());
                        e.printStackTrace();
                    }
                }
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            var tokenSelectionHandler = renderer.getComponentImplementing(TokenSelectionHandler.class);
            if (tokenSelectionHandler == null) {
                return null;
            }
            return new TokenTransferable(tokenSelectionHandler.getAllHighlightedTokens());
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) {
                try {
                    handleExportTokensDone((ArrayList<TokenData>) data.getTransferData(TOKEN_FLAVOR));
                } catch (UnsupportedFlavorException | IOException e) {
                }
            }
        }

    };
}
