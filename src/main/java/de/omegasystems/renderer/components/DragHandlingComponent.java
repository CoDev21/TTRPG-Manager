package de.omegasystems.renderer.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.TransferHandler;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.dataobjects.TokenData;

public class DragHandlingComponent implements RenderingComponent {

    @Override
    public void draw(Graphics2D g, Dimension size) {
        // Drawing logic here
    }

    private Renderer renderer;

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;

        renderer.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                Transferable transferable = support.getTransferable();
                try {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : files) {
                        if (!file.isFile())
                            continue;

                        try {
                            ImageIO.read(file);

                            handleImageImport(file);
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

        });

    }

    private void handleImageImport(File file) {
        TokenHandler tokenHandler = renderer.getComponentImplementing(TokenHandler.class);

        if (tokenHandler == null)
            return;

        String filenameWithoutExtension = file.getName().substring(0,
                file.getName().lastIndexOf('.'));

        var tokenData = new TokenData();
        tokenData.getName().setValue(filenameWithoutExtension);
        tokenData.getPictureFile().setValue(file);

        tokenHandler.addToken(new Token(tokenData, tokenHandler));
    }
}
