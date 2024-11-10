package de.omegasystems.renderer.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.dataobjects.Friendlieness;
import de.omegasystems.dataobjects.TokenData;
import de.omegasystems.dataobjects.TokenSize;
import de.omegasystems.renderer.ComponentBuilder;

public class TokenDialog extends JDialog {

    /**
     * Modifies (or deletes) the already existing Token
     * 
     * @param parent
     * @param existingToken
     */
    public TokenDialog(JFrame parent, Token existingToken) {
        this(parent, existingToken.createDataObject(), existingToken.getTokenHandler(), existingToken);
    }

    /**
     * Creates a new token
     * 
     * @param parent
     * @param tokenHandler
     */
    public TokenDialog(JFrame parent, TokenHandler tokenHandler) {
        this(parent, new TokenData(), tokenHandler, null);
    }

    public TokenDialog(JFrame parent, TokenData data, TokenHandler handler, Token token) {
        super(parent);
        setTitle(token == null ? "Create New Token" : "Edit " + data.getName().getValue() + "");

        var mainPanel = new JPanel();
        var mainLayout = new GridLayout(2, 1);
        mainLayout.setHgap(5);
        mainLayout.setVgap(5);
        mainPanel.setLayout(mainLayout);

        var topPanel = new JPanel();
        var topLayout = new GridLayout(1, 2);
        topLayout.setVgap(20);
        topPanel.setLayout(topLayout);

        var topRightPanel = new JPanel();
        topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));

        var topLeftPanel = new JPanel();
        topLeftPanel.setLayout(new BoxLayout(topLeftPanel, BoxLayout.Y_AXIS));

        var descriptionEditor = ComponentBuilder.createEditorPane(data.getDescription());

        var imageChooser = ComponentBuilder.createImagePathSelector(parent, data.getPictureFile(),
                Token.getPlaceholderImage());
        imageChooser.setImageSize(
                new Dimension(Token.getPlaceholderImage().getWidth(null), Token.getPlaceholderImage().getWidth(null)));
        var nameChooser = ComponentBuilder.createTextField(16, data.getName());
        // nameChooser.setPreferredSize(imageChooser.getMinimumSize());

        var friendlienessChooser = ComponentBuilder.createEnumRadioButtons(data.getFriendStatus(), Friendlieness.class);
        var sizeClassChooser = ComponentBuilder.createEnumSlider("Size: ", data.getSizeClass(), TokenSize.class);
        var manualSizeChooser = ComponentBuilder.createDoubleTextField(5, data.getSize(), 1);
        var movementEditor = ComponentBuilder.createTextField(12, data.getMovement());

        var sendButton = ComponentBuilder.createCallbackButton(token != null ? "Save Changes" : "Create Token", () -> {
            if (token != null)
                token.updateAllValues(data);
            else
                handler.addToken(new Token(data, handler));
            dispose();
        });

        sendButton.setBackground(Color.GREEN);
        sendButton.setForeground(Color.RED);
        sendButton.setOpaque(false);

        topLeftPanel.add(imageChooser);
        topLeftPanel.add(Box.createVerticalStrut(10));
        topLeftPanel.add(nameChooser);

        topRightPanel.add(sizeClassChooser);
        topRightPanel.add(manualSizeChooser);
        topRightPanel.add(new Label("Friendlieness:", Label.LEFT));
        friendlienessChooser.forEach(btn -> topRightPanel.add(btn));
        topRightPanel.add(Box.createVerticalStrut(20));
        topRightPanel.add(movementEditor);

        topPanel.add(topLeftPanel);
        topPanel.add(topRightPanel);

        // bottomPanel.add(descriptionEditor, JLayeredPane.DEFAULT_LAYER);
        // bottomPanel.add(sendButton, JLayeredPane.PALETTE_LAYER);

        mainPanel.add(topPanel);
        mainPanel.add(descriptionEditor);

        // JPanel buttonPanel = new JPanel();
        // buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        // buttonPanel.add(sendButton);

        // JLayeredPane layers = new JLayeredPane();

        // layers.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        // layers.add(buttonPanel, JLayeredPane.MODAL_LAYER);

        add(mainPanel);
        add(sendButton, BorderLayout.SOUTH);

        int eb = 5;
        getRootPane().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(eb, eb, eb, eb), // outer border
                BorderFactory.createEmptyBorder()));

        // JPanel south = new JPanel();
        // south.add(send);
        // getContentPane().add(north, "North");
        // getContentPane().add(south, "South");
        // pack();

        changeFont(this, new Font("Georgia", Font.BOLD, sendButton.getFont().getSize()));

        pack();
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void changeFont(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                changeFont(child, font);
            }
        }
    }

}
