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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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

        // Top left panel
        var topLeftPanel = new JPanel();
        topLeftPanel.setLayout(new BoxLayout(topLeftPanel, BoxLayout.Y_AXIS));

        var imageChooser = ComponentBuilder.createImagePathSelector(parent, data.getPictureFile(),
                Token.getPlaceholderImage());
        imageChooser.setImageSize(
                new Dimension(Token.getPlaceholderImage().getWidth(null), Token.getPlaceholderImage().getWidth(null)));
        var nameChooser = ComponentBuilder.createTextField(16, data.getName());
        nameChooser.setMaximumSize(
                new Dimension(imageChooser.getMinimumSize().width, nameChooser.getPreferredSize().height));

        imageChooser.setAlignmentX(CENTER_ALIGNMENT);

        topLeftPanel.add(imageChooser);
        topLeftPanel.add(Box.createVerticalStrut(10));
        topLeftPanel.add(Box.createVerticalGlue());
        topLeftPanel.add(nameChooser);

        // Top right panel
        var topRightPanel = new JPanel();
        topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));
        topRightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRightPanel.setFocusable(true);

        // The size chooser panel
        JPanel sizePanel = new JPanel(new BorderLayout());
        var sizeClassChooser = ComponentBuilder.createEnumSlider("Size: ", data.getSizeClass(), TokenSize.class);
        var manualSizeChooser = ComponentBuilder.createDoubleTextField(3, data.getSize(), 1);
        manualSizeChooser.setMaximumSize(manualSizeChooser.getPreferredSize());
        sizePanel.add(sizeClassChooser.slider(), BorderLayout.CENTER);
        sizePanel.add(manualSizeChooser, BorderLayout.EAST);
        

        var friendlienessLabel = new JLabel("Friendlieness:");
        var friendlienessChooser = ComponentBuilder.createEnumRadioButtons(data.getFriendStatus(), Friendlieness.class);
        var movementEditor = ComponentBuilder.createTextField(12, data.getMovement());

        // Prevent the textfield and panel from using up all the vertical space
        movementEditor.setMaximumSize(new Dimension(Integer.MAX_VALUE, movementEditor.getPreferredSize().height));
        sizePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, sizePanel.getPreferredSize().height));

        friendlienessLabel
                .setMaximumSize(new Dimension(Integer.MAX_VALUE, friendlienessLabel.getPreferredSize().height));

        friendlienessChooser.forEach(elem -> elem.setAlignmentX(LEFT_ALIGNMENT));

        // Add all components
        topRightPanel.add(leftAlignedInHorizontalBox(sizeClassChooser.label()));
        topRightPanel.add(sizePanel);
        topRightPanel.add(Box.createGlue());
        topRightPanel.add(leftAlignedInHorizontalBox(friendlienessLabel));
        friendlienessChooser.forEach(btn -> topRightPanel.add(leftAlignedInHorizontalBox(btn)));
        topRightPanel.add(Box.createGlue());
        topRightPanel.add(movementEditor);

        // Other panels
        var mainPanel = new JPanel();
        var mainLayout = new GridLayout(2, 1);
        mainLayout.setHgap(8);
        mainLayout.setVgap(8);
        mainPanel.setLayout(mainLayout);

        var topPanel = new JPanel();
        var topLayout = new GridLayout(1, 2);
        topLayout.setHgap(30);
        topPanel.setLayout(topLayout);

        var descriptionEditor = ComponentBuilder.createEditorPane(data.getDescription());

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

        topPanel.add(topLeftPanel);
        topPanel.add(topRightPanel);

        mainPanel.add(topPanel);
        mainPanel.add(descriptionEditor);

        add(mainPanel);
        add(sendButton, BorderLayout.SOUTH);

        int eb = 5;
        getRootPane().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(eb, eb, eb, eb), // outer border
                BorderFactory.createEmptyBorder()));

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

    private static Box leftAlignedInHorizontalBox(Component component) {
        Box box = Box.createHorizontalBox();
        box.add(component);
        box.add(Box.createHorizontalGlue());
        return box;
    }

}
