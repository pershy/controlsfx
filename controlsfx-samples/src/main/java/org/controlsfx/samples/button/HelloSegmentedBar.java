/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.samples.button;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SegmentedBar;
import org.controlsfx.samples.Utils;

public class HelloSegmentedBar extends ControlsFXSample {
    
    @Override public String getSampleName() {
        return "SegmentedBar";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SegmentedBar.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        int row = 0;

        // Only buttons
        {
            grid.add(new Label("SegmentedBar with normal Buttons: "), 0, row++);

            Button b1 = new Button("day");
            Button b2 = new Button("week");
            Button b3 = new Button("month");
            Button b4 = new Button("year");

            SegmentedBar bar = new SegmentedBar(b1, b2, b3, b4);
            grid.add(bar, 1, row++);
        }

        // Only ToggleButtons
        {
            grid.add(new Label("SegmentedBar with Toggle-Buttons: "), 0, row++);

            ToggleButton b1 = new ToggleButton("day");
            ToggleButton b2 = new ToggleButton("week");
            ToggleButton b3 = new ToggleButton("month");
            ToggleButton b4 = new ToggleButton("year");

            SegmentedBar bar = new SegmentedBar(b1, b2, b3, b4);
            grid.add(bar, 1, row++);
        }

        // Only Combo-Boxes
        {
            grid.add(new Label("SegmentedBar with  ComboBoxes: "), 0, row++);

            ComboBox<String> b1 = new  ComboBox<>();
            ComboBox<String> b2 = new  ComboBox<>();
            ComboBox<String> b3 = new  ComboBox<>();
            ComboBox<String> b4 = new  ComboBox<>();

            SegmentedBar bar = new SegmentedBar(b1, b2, b3, b4);
            grid.add(bar, 1, row++);
        }

        // Only Split-Menu-Buttons
        {
            grid.add(new Label("SegmentedBar with normal SplitMenuButton: "), 0, row++);

            SplitMenuButton b1 = new SplitMenuButton(new MenuItem("Item 1"), new MenuItem("Item 2"));
            SplitMenuButton b2 = new SplitMenuButton(new MenuItem("Item 1"), new MenuItem("Item 2"));
            SplitMenuButton b3 = new SplitMenuButton(new MenuItem("Item 1"), new MenuItem("Item 2"));
            SplitMenuButton b4 = new SplitMenuButton(new MenuItem("Item 1"), new MenuItem("Item 2"));

            SegmentedBar bar = new SegmentedBar(b1, b2, b3, b4);
            grid.add(bar, 1, row++);
        }

        // Mixed
        {
            grid.add(new Label("SegmentedBar with Buttons and a ComboBox: "), 0, row++);

            Button c1 = new Button("day");
            Button c2 = new Button("week");
            Button c3 = new Button("month");
            ComboBox<String> c4 = new ComboBox();

            c4.getItems().addAll("This", "is", "so", "cool!");

            SegmentedBar bar = new SegmentedBar(c1, c2, c3, c4);
            grid.add(bar, 1, row++);
        }




        return grid;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
