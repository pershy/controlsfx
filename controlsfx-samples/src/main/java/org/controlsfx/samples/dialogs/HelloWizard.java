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
package org.controlsfx.samples.dialogs;

import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.Wizard.WizardPage;
import org.controlsfx.samples.Utils;

public class HelloWizard extends ControlsFXSample {

	@Override
	public String getSampleName() {
		return "Wizards";
	}

	@Override
	public String getJavaDocURL() {
		return Utils.JAVADOC_BASE + "org/controlsfx/dialog/Wizard.html";
	}

	@Override
	public Node getPanel(final Stage stage) {
		Button button1 = new Button("Simple Wizard!");
        button1.setOnAction(e -> showWizard());
        
        Button button2 = new Button("Branching Wizard!");
        button2.setOnAction(e -> showBranchingWizard());
        
        VBox vbox = new VBox(10, button1, button2);
        vbox.setPadding(new Insets(10));
        return vbox;
	}
	
	private void showWizard() {
        // define pages to show
        WizardPage page1 = new WizardPage() {
            @Override public Node getContent() {
                return new Label("Page 1");
            }
        };
        
        WizardPage page2 = new WizardPage() {
            @Override public Node getContent() {
                return new Label("Page 2");
            }
        };
        
        WizardPage page3 = new WizardPage() {
            @Override public Node getContent() {
                return new Label("Page 3");
            }
        };
        
        // create wizard
        Wizard wizard = new Wizard();
        wizard.getPages().addAll(page1, page2, page3);
        
        // show wizard
        wizard.show();
    }
    
    private void showBranchingWizard() {
        // define pages to show.
        // Because page1 references page2, we need to declare page2 first.
        final WizardPage page2 = new WizardPage() {
            public Node getContent() {
                return new Label("Page 2");
            }
        };
        
        final WizardPage page1 = new WizardPage() {
            final CheckBox checkBox = new CheckBox("Skip the second page");
            
            public Node getContent() {
                VBox vbox = new VBox(10, new Label("Page 1"), checkBox);
                return vbox;
            }
            
            @Override public void updatePages(Wizard wizard) {
                List<WizardPage> pages = wizard.getPages();
                if (checkBox.isSelected()) {
                    pages.remove(page2);
                } else {
                    if (! pages.contains(page2)) {
                        pages.add(1, page2);
                    }
                }
            }
        };
        
        final WizardPage page3 = new WizardPage() {
            public Node getContent() {
                return new Label("Page 3");
            }
        };
        
        // create wizard
        Wizard wizard = new Wizard();
        wizard.getPages().addAll(page1, page2, page3);
        
        // show wizard
        wizard.show();
    }

	public static void main(String[] args) {
		Application.launch(args);
	}
}
