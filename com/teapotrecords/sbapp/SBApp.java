package com.teapotrecords.sbapp;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SBApp extends Application {
  final static String version = "0.1";
  Menu mFile = new Menu("_File");
  MenuBar topMenu = new MenuBar();
  MenuItem miExit = new MenuItem("E_xit");
  SBEventHandler eh = new SBEventHandler();
  
  public void initMenus() {
    topMenu.getMenus().addAll(mFile);
    mFile.getItems().addAll(miExit);
    miExit.setMnemonicParsing(true);
    miExit.setOnAction(eh);
  }
  
  public static void main(String[] args) {
    launch(args);

  }

  @Override
  public void start(Stage stage) throws Exception {
    initMenus();
    stage.setTitle("BFree "+version);
    Scene scene = new Scene(new VBox(), 600,480);
    ((VBox) scene.getRoot()).getChildren().addAll(topMenu);
    stage.setScene(scene);
    stage.show();
    
    
  }
  
  
  
  class SBEventHandler implements EventHandler {
    public void handle(Event t) {
      if (t.getSource()==miExit) {
        System.out.println("Exit");
      }
    }
  }
}
