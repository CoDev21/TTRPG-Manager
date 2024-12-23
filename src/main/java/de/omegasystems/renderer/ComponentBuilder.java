package de.omegasystems.renderer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.omegasystems.utility.AbstractAttributeHolder;
import de.omegasystems.utility.AbstractAttributeHolder.Property;

public abstract class ComponentBuilder {

    public static <T extends Enum<?>> JPanel createEnumSlider(String toTitle,
            AbstractAttributeHolder.Property<T> coupledValue,
            Class<T> values) {

        final String title = (toTitle != null ? toTitle + "" : "");

        Map<T, Integer> options = new HashMap<>();
        T[] enumValues = values.getEnumConstants();

        JSlider slider = new JSlider(0, enumValues.length - 1, 0);
        slider.setSnapToTicks(true);
        slider.setMajorTickSpacing(1);
        slider.setMinorTickSpacing(1);

        JLabel label = new JLabel(title + enumValues[0].toString());

        if (coupledValue != null) {

            int defaultVal = 0;
            for (int i = 0; i < enumValues.length; i++) {
                // Select current enum value
                if (enumValues[i].equals(coupledValue.getValue()))
                    defaultVal = i;
                // Create reverse lookup map
                options.put(enumValues[i], i);
            }
            slider.setValue(defaultVal);
            label.setText(title + enumValues[defaultVal].toString());

            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    label.setText(title + enumValues[slider.getValue()].toString());
                    if (slider.getValueIsAdjusting())
                        return;
                    coupledValue.setValue(enumValues[slider.getValue()]);
                }
            });

            // Hook up an observer to change the ui if an internal change occurs
            coupledValue.addObserver(newVal -> {
                slider.setValue(options.get(newVal));
                label.setText(title + newVal.toString());
            });

        }

        // Create a neat layout to accomodate the enum slider

        var panel = new JPanel();
        // slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        // descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label);
        panel.add(slider);
        return panel;

    }

    public static <T extends Enum<?>> List<JRadioButton> createEnumRadioButtons(
            AbstractAttributeHolder.Property<T> coupledValue,
            Class<T> values) {
        Map<T, JRadioButton> options = new HashMap<>();
        List<JRadioButton> ret = new ArrayList<>();
        ButtonGroup group = new ButtonGroup();

        if (coupledValue != null) {

            for (var enumVal : values.getEnumConstants()) {
                JRadioButton item = new JRadioButton(enumVal.toString(), enumVal.equals(coupledValue.getValue()));
                group.add(item);
                ret.add(item);
                options.put(enumVal, item);

                // Hook up an observer to every radiobutton (because you can't attach it to the
                // ButtonGroup)
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        coupledValue.setValue(enumVal);
                    }
                });
            }

            // Hook up an observer to change the ui if an internal change occurs
            coupledValue.addObserver(newVal -> group.setSelected(options.get(newVal).getModel(), false));
            if (coupledValue.getValue() != null)
                group.setSelected(options.get(coupledValue.getValue()).getModel(), false);

        }

        return ret;

    }

    public static JButton createCallbackButton(String title, Runnable toExecute) {
        JButton send = new JButton(title);
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toExecute.run();
            }
        });
        return send;
    }

    public static JTextField createTextField(int coulums, AbstractAttributeHolder.Property<String> coupledValue) {

        JTextField item = new JTextField(coupledValue.getValue(), coulums);
        item.setEditable(true);

        item.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                coupledValue.setValue(item.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                coupledValue.setValue(item.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                coupledValue.setValue(item.getText());
            }
        });

        // Hook up an observer to change the ui if an internal change occurs
        // coupledValue.addObserver(newVal -> item.setText(newVal));
        return item;
    }

    public static JTextArea createEditorPane(AbstractAttributeHolder.Property<String> coupledValue) {

        JTextArea item = new JTextArea(coupledValue.getValue());
        item.setEditable(true);
        // item.setPreferredSize(new Dimension(width, height));

        item.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                coupledValue.setValue(item.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                coupledValue.setValue(item.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                coupledValue.setValue(item.getText());
            }
        });

        // Hook up an observer to change the ui if an internal change occurs
        // coupledValue.addObserver(newVal -> item.setText(newVal));
        return item;
    }

    public static ImagePathSelector createImagePathSelector(JFrame parent,
            AbstractAttributeHolder.Property<File> coupledValue, Image defaultImage) {
        return new ImagePathSelector(parent, coupledValue, defaultImage);
    }

    public static class ImagePathSelector extends JLabel {

        private Property<File> path;
        private static File lastPath = new File(System.getProperty("user.dir"));

        public ImagePathSelector(JFrame parent, Property<File> currentPath, Image defaultImage) {
            super(new ImageIcon(loadFileOrPlaceholderImage(currentPath.getValue(), defaultImage)));

            this.path = currentPath;

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    File newPath = requestImageFromUser(parent);
                    if (newPath != null) {
                        currentPath.setValue(newPath);
                        var img = loadFileOrPlaceholderImage(currentPath.getValue(), defaultImage);
                        Image dimg = img.getScaledInstance(getWidth(), getHeight(),
                                Image.SCALE_SMOOTH);
                        setIcon(new ImageIcon(dimg));
                        revalidate();
                        if (getParent() != null)
                            getParent().revalidate();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }
            });
        }

        public Property<File> getPath() {
            return path;
        }

        private static Image loadFileOrPlaceholderImage(File filePath, Image backupImage) {
            try {
                return ImageIO.read(filePath);
            } catch (Exception e) {
                return backupImage;
            }
        }

        private File requestImageFromUser(JFrame parent) {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser
                    .setCurrentDirectory((path.getValue() != null && path.getValue().exists())
                            ? path.getValue()
                            : lastPath);

            fileChooser.setDialogTitle("Choose a map");
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Image files", ImageIO.getReaderFileSuffixes()));
            int result = fileChooser.showOpenDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null && selectedFile.exists()) {
                    lastPath = selectedFile;
                    try {
                        ImageIO.read(selectedFile);
                        return selectedFile;
                    } catch (Exception e) {
                    }
                }
            }

            return null;
        }

    }

}
