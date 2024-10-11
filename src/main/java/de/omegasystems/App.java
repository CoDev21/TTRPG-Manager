package de.omegasystems;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.omegasystems.components.MenubarComponent;
import de.omegasystems.components.ScrollingComponent;
import de.omegasystems.components.TokenRendererComponent;
import de.omegasystems.components.TokenTooltipComponent;
import de.omegasystems.components.dialog.ChangeValueDialog;
import de.omegasystems.core.Renderer;
import de.omegasystems.dataobjects.MenubarAttributeHolder;

public class App {

    public static void main(String[] args) {
        boolean isDev = false;
        for (String string : args) {
            if ("--dev".equals(string))
                isDev = true;
        }
        final boolean resultIsDev = isDev;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App(resultIsDev).CreateAndShowGUI();
            }
        });

    }

    private static App instance;
    private static File debugRessourceFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources");

    private boolean isDevEnv;

    private JFrame frame;
    private MenubarAttributeHolder toolbarAttributes = new MenubarAttributeHolder();

    public App(boolean isDevEnv) {
        instance = this;
        this.isDevEnv = isDevEnv;
    }

    private void CreateAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("TTRPG Map Manager");
        frame.setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var img = requestImageFromUser();
        if (img == null)
            System.exit(0);

        var gameComponent = new ScrollingComponent(img);
        var renderer = gameComponent.getRenderer();
        frame.add(gameComponent);
        frame.setJMenuBar(new MenubarComponent(toolbarAttributes));

        registerRendererComponents(renderer);

        addMenubarActions();

        frame.setMaximumSize(new Dimension(800, 600));
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationByPlatform(true);

        frame.setVisible(true); // Show the window

    }

    private void registerRendererComponents(Renderer renderer) {
        TokenRendererComponent tokenHandler = new TokenRendererComponent(renderer);
        tokenHandler.registerUIBindings();

        new TokenTooltipComponent(renderer, tokenHandler);
    }

    private void addMenubarActions() {
        getToolbarAttributes().VIEW_GRID_OPEN_SCALE_DIALOG
                .addObserver(abs -> new ChangeValueDialog.DoubleDialog(frame, getToolbarAttributes().VIEW_GRID_SCALE));

        getToolbarAttributes().VIEW_GRID_OPEN_THICKNESS_DIALOG
                .addObserver(
                        a -> new ChangeValueDialog.DoubleDialog(frame, getToolbarAttributes().VIEW_GRID_THICKNESS));

        getToolbarAttributes().VIEW_GRID_OPEN_OFFSET_X_DIALOG
                .addObserver(
                        abs -> new ChangeValueDialog.DoubleDialog(frame, getToolbarAttributes().VIEW_GRID_OFFSET_X));

        getToolbarAttributes().VIEW_GRID_OPEN_OFFSET_Y_DIALOG
                .addObserver(
                        abs -> new ChangeValueDialog.DoubleDialog(frame, getToolbarAttributes().VIEW_GRID_OFFSET_Y));
    }

    /**
     * 
     * @return An image if successfull or null otherwise
     */
    private Image requestImageFromUser() {
        if (isDevEnv)
            try {
                return ImageIO.read(loadResourceFile("/img/Tavern_Battlemap.jpg"));
            } catch (Exception e) {
            }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Choose a map");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", ImageIO.getReaderFileSuffixes()));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                return ImageIO.read(selectedFile);
            } catch (Exception e) {
            }
        }

        return null;
    }

    public MenubarAttributeHolder getToolbarAttributes() {
        return toolbarAttributes;
    }

    public boolean isDevEnv() {
        return isDevEnv;
    }

    public void openErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public static App getInstance() {
        return instance;
    }

    public InputStream loadResourceFile(String path) {
        if (!isDevEnv())
            return App.class.getClassLoader().getResourceAsStream(path);
        try {
            return new FileInputStream(new File(debugRessourceFile, path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Couldn't find file located at: '" + new File(debugRessourceFile, path).getAbsolutePath() + "'");
        }
        return null;
    }
}
