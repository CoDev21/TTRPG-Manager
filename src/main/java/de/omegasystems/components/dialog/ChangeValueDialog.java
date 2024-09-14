package de.omegasystems.components.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.omegasystems.dataobjects.AbstractAttributeHolder;

public abstract class ChangeValueDialog<T> extends JDialog {

    public ChangeValueDialog(JFrame parent, AbstractAttributeHolder.Property<T> toChange) {
        super(parent);
        setTitle("Enter a new Value:");
        final JTextField textField = new JTextField(""+toChange.getValue(), 16);
        JPanel north = new JPanel();
        north.add(textField);
        JButton send = new JButton("send text");
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                if (!isValidInput(text)) {
                    JOptionPane.showMessageDialog(new JFrame(), "Invalid Value", "Invalid Value",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                toChange.setValue(parseValue(text));
                dispose();
            }
        });
        JPanel south = new JPanel();
        south.add(send);
        getContentPane().add(north, "North");
        getContentPane().add(south, "South");
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    protected abstract boolean isValidInput(String input);

    protected abstract T parseValue(String input);

    public static class StringDialog extends ChangeValueDialog<String> {

        public StringDialog(JFrame parent, AbstractAttributeHolder.Property<String> toChange) {
            super(parent, toChange);
        }

        @Override
        protected boolean isValidInput(String input) {
            return true;
        }

        @Override
        protected String parseValue(String input) {
            return input;
        }

    }

    public static class IntegerDialog extends ChangeValueDialog<Integer> {

        public IntegerDialog(JFrame parent, AbstractAttributeHolder.Property<Integer> toChange) {
            super(parent, toChange);
        }

        @Override
        protected boolean isValidInput(String input) {
            try {
                Integer.parseInt(input);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        protected Integer parseValue(String input) {
            return Integer.parseInt(input);
        }

    }

    public static class DoubleDialog extends ChangeValueDialog<Double> {

        public DoubleDialog(JFrame parent, AbstractAttributeHolder.Property<Double> toChange) {
            super(parent, toChange);
        }

        @Override
        protected boolean isValidInput(String input) {
            try {
                Double.parseDouble(input);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        protected Double parseValue(String input) {
            return Double.parseDouble(input);
        }

    }
}
