package de.omegasystems.dataobjects;

import de.omegasystems.utility.AbstractAttributeHolder;

public class MenubarAttributeHolder extends AbstractAttributeHolder {

    public Action<Void> MAP_IMPORT = new Action<>();
    public Action<Void> MAP_SAVE = new Action<>();
    public Action<Void> MAP_SWAP = new Action<>();

    public Action<Void> TOKEN_CREATE = new Action<>();
    public Action<Void> TOKEN_CREATE_MULTIPLE = new Action<>();
    public Action<Void> TOKEN_EDIT = new Action<>();
    public Action<Void> TOKEN_OPEN_SIZE_DIALOG = new Action<>();
    public Action<Void> TOKEN_OPEN_OUTLINE_THICKNESS_DIALOG = new Action<>();
    public Property<Double> TOKEN_SIZE = new Property<>(64.0);
    public Property<Double> TOKEN_OUTLINE_THICKNESS = new Property<>(4.0);

    public Property<Boolean> VIEW_ANIMATION_SMOOTH_SCROLLING_ENABLED = new Property<>(true);
    public Property<Boolean> VIEW_GRID_ENABLED = new Property<>(false);
    public Property<Double> VIEW_GRID_THICKNESS = new Property<>(2.0);
    public Property<Double> VIEW_GRID_SCALE = new Property<>(64.0);
    public Property<Double> VIEW_GRID_OFFSET_X = new Property<>(0.0);
    public Property<Double> VIEW_GRID_OFFSET_Y = new Property<>(0.0);
    public Action<Void> VIEW_GRID_OPEN_THICKNESS_DIALOG = new Action<>();
    public Action<Void> VIEW_GRID_OPEN_SCALE_DIALOG = new Action<>();
    public Action<Void> VIEW_GRID_OPEN_OFFSET_X_DIALOG = new Action<>();
    public Action<Void> VIEW_GRID_OPEN_OFFSET_Y_DIALOG = new Action<>();

}
