package de.omegasystems.renderer;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.omegasystems.dataobjects.MenubarAttributeHolder;
import de.omegasystems.utility.AbstractAttributeHolder;

public class MenubarComponent extends JMenuBar {

    public MenubarComponent(MenubarAttributeHolder dataHolder) {
        var MAP = new JMenu("Map");
        MAP.add(createTextActionTrigger("Import", null, dataHolder.MAP_IMPORT));
        MAP.add(createTextActionTrigger("Save", null, dataHolder.MAP_SAVE));
        MAP.add(createTextActionTrigger("Swap", null, dataHolder.MAP_SWAP));

        var TOKEN = new JMenu("Token");
        TOKEN.add(createTextActionTrigger("Create", null, dataHolder.TOKEN_CREATE));
        TOKEN.add(createTextActionTrigger("Edit", null, dataHolder.TOKEN_EDIT));
        TOKEN.add(createTextActionTrigger("Create Multiple", null, dataHolder.TOKEN_CREATE_MULTIPLE));
        TOKEN.addSeparator();
        TOKEN.add(createTextActionTrigger("Change Token Size", null, dataHolder.TOKEN_OPEN_SIZE_DIALOG));
        TOKEN.addSeparator();
        TOKEN.add(createTextActionTrigger("Change Outline Thickness", null,
                dataHolder.TOKEN_OPEN_OUTLINE_THICKNESS_DIALOG));

        var GRID_SUBMENU = new JMenu("Grid");
        GRID_SUBMENU.add(createCheckbox("Enable Grid", null, dataHolder.VIEW_GRID_ENABLED));
        GRID_SUBMENU.add(
                createTextActionTrigger("Change Size", null, dataHolder.VIEW_GRID_OPEN_SCALE_DIALOG));
        GRID_SUBMENU.add(createTextActionTrigger("Change Thickness", null,
                dataHolder.VIEW_GRID_OPEN_THICKNESS_DIALOG));
        GRID_SUBMENU.add(createTextActionTrigger("Change X Offset", null,
                dataHolder.VIEW_GRID_OPEN_OFFSET_X_DIALOG));
        GRID_SUBMENU.add(createTextActionTrigger("Change Y Offset", null,
                dataHolder.VIEW_GRID_OPEN_OFFSET_Y_DIALOG));

        var VIEW = new JMenu("View");
        VIEW.add(GRID_SUBMENU);
        VIEW.add(createCheckbox("Smooth Scrolling", null, dataHolder.VIEW_ANIMATION_SMOOTH_SCROLLING_ENABLED));

        add(MAP);
        add(TOKEN);
        add(VIEW);
    }

    public static JCheckBoxMenuItem createCheckbox(String name, Icon icon,
            AbstractAttributeHolder.Property<Boolean> coupledValue) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, icon);

        if (coupledValue != null) {
            item.setEnabled(coupledValue.getValue());
            item.setAction(new AbstractAction(name, icon) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    coupledValue.setValue(item.isSelected());
                }
            });
            coupledValue.addObserver(newVal -> {
                item.setSelected(newVal);
            });
        }

        return item;
    }

    public static JMenuItem createTextActionTrigger(String name, Icon icon,
            AbstractAttributeHolder.Action<Void> coupledValue) {
        JMenuItem item = new JMenuItem(name, icon);

        if (coupledValue != null) {
            item.setEnabled(coupledValue.canTriggerAction().getValue());
            coupledValue.canTriggerAction().addObserver(enabled -> item.setEnabled(enabled));
            item.setAction(new AbstractAction(name, icon) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    coupledValue.triggerAction(null);
                }
            });
        }

        return item;
    }

}
