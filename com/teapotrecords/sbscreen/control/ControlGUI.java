package com.teapotrecords.sbscreen.control;

import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ControlGUI extends Stage {
  public ControlGUI() {
    super();
    VBox layout = new VBox();
    layout.setPadding(new Insets(10));;
    layout.setSpacing(8);
    
    HBox displayLine = new HBox();
    Text display = new Text("Display");
    displayLine.getChildren().add(display);
    ToggleButton tb_display_on = new ToggleButton("On");
    ToggleButton tb_display_off = new ToggleButton("Off");
    final ToggleGroup tg_display = new ToggleGroup();
    tb_display_on.setToggleGroup(tg_display);
    tb_display_off.setToggleGroup(tg_display);
    displayLine.getChildren().add(tb_display_on);
    displayLine.getChildren().add(tb_display_off);
  }
}
