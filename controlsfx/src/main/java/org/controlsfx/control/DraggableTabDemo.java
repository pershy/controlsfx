/**
 * Copyright (c) 2013, ControlsFX All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of ControlsFX, any associated
 * website, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control;

import java.util.concurrent.Callable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Just a very simple sample application to demonstrate draggable tabs.
 */
public class DraggableTabDemo extends Application {
 
    @Override
    public void start(final Stage primaryStage) {
        DraggableTab tab1 = new DraggableTab("Tab 1");
        tab1.setMoveCallback(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                return false;
            }
        });
        tab1.setClosable(false);
        tab1.setDetachable(false);
        tab1.setContent(new Rectangle(500, 500, Color.BLACK));
        DraggableTab tab2 = new DraggableTab("Tab 2");
        tab2.setMoveCallback(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                return false;
            }
        });
        tab2.setClosable(false);
        tab2.setContent(new Rectangle(500, 500, Color.RED));
        DraggableTab tab3 = new DraggableTab("Tab 3");
        tab3.setClosable(false);
        tab3.setContent(new Rectangle(500, 500, Color.BLUE));
        DraggableTab tab4 = new DraggableTab("Tab 4");
        tab4.setClosable(false);
        tab4.setContent(new Rectangle(500, 500, Color.ORANGE));
        TabPane tabs = new TabPane();
        tabs.getTabs().add(tab1);
        tabs.getTabs().add(tab2);
        tabs.getTabs().add(tab3);
        tabs.getTabs().add(tab4);
 
        StackPane root = new StackPane();
        root.getChildren().add(tabs);
 
        Scene scene = new Scene(root);
 
        primaryStage.setScene(scene);
        primaryStage.show();
 
    }
}