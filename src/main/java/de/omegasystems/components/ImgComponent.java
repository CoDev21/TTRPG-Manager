package de.omegasystems.components;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import java.awt.Image;

public class ImgComponent extends JLabel {
    
    public ImgComponent(Image img) {
        super(new ImageIcon(img));
    }
    
}
