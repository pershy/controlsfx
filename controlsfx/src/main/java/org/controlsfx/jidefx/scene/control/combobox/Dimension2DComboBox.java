/*
 * @(#)Dimension2DComboBox.java 5/19/2013
 *
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */

package org.controlsfx.jidefx.scene.control.combobox;

import org.controlsfx.jidefx.scene.control.field.Dimension2DField;
import org.controlsfx.jidefx.scene.control.field.FormattedTextField;

import javafx.geometry.Dimension2D;

/**
 * A {@code FormattedComboBox} for {@link Dimension2D}.
 */
public class Dimension2DComboBox extends ValuesComboBox<Dimension2D> {
    public Dimension2DComboBox() {
        this(new Dimension2D(0, 0));
    }

    public Dimension2DComboBox(Dimension2D value) {
        super(value);
    }


    @Override
    protected FormattedTextField<Dimension2D> createFormattedTextField() {
        return new Dimension2DField();
    }

    @Override
    protected void initializeComboBox() {
        super.initializeComboBox();
        setPopupContentFactory(((Dimension2DField) getEditor()).getPopupContentFactory());
    }
}
