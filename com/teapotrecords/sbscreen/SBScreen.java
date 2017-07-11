package com.teapotrecords.sbscreen;


import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;

import com.sun.webkit.WebPage;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class SBScreen extends Application {
  Stage displayStage;  
  
  // Control GUI components
  
  final RadioButton rb_on = new RadioButton("ON");
  final RadioButton rb_off = new RadioButton("OFF");
  final Label l_display = new Label("Display");
  final Label l_location = new Label("Location (x,y)");
  final Label l_size = new Label("Size (w,h)");
  final ToggleGroup tg_display = new ToggleGroup();
  final HBox hb_display = new HBox();
  final HBox hb_location = new HBox();
  final HBox hb_size = new HBox();
  final GridPane grid = new GridPane();
  final TextField tf_x  = new TextField();
  final TextField tf_y  = new TextField();
  final TextField tf_w  = new TextField();
  final TextField tf_h  = new TextField();
  final Button b_detect = new Button("Detect Fullscreen");
  final Button b_backdrop = new Button("Backdrop");
  final TextField tf_backdrop = new TextField();
  
  final StackPane displayStageSP = new StackPane();
  Scene displayScene = null;
  final WebView browser = new WebView();
  Media m;
  MediaPlayer mp;
  MediaView mv;
  final ImageView iv = new ImageView();
  Image image;
  
  
  public void showDisplayScreen() {
    displayStage.setWidth(Integer.parseInt(tf_w.getText()));
    displayStage.setHeight(Integer.parseInt(tf_h.getText()));
    displayStage.setX(Integer.parseInt(tf_x.getText()));
    displayStage.setY(Integer.parseInt(tf_y.getText()));
    displayStageSP.setStyle("-fx-background-color: BLACK");
    displayScene.setFill(Color.BLACK); 
    
    
    String backdrop = tf_backdrop.getText().toUpperCase();
    if ((backdrop.endsWith(".MP4")) || (backdrop.endsWith(".MOV")) || (backdrop.endsWith(".AVI")) || (backdrop.endsWith(".WMV"))) {
      m = new Media(new File(tf_backdrop.getText()).toURI().toString());
      mp = new MediaPlayer(m);
      mv = new MediaView(mp);
      displayStageSP.getChildren().add(mv);      
      mp.setCycleCount(MediaPlayer.INDEFINITE);
      final DoubleProperty width = mv.fitWidthProperty();
      final DoubleProperty height = mv.fitHeightProperty();
      width.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
      height.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));
      mv.setPreserveRatio(true);

      
    } else {
      image = new Image(new File(tf_backdrop.getText()).toURI().toString());
      iv.setPreserveRatio(true);
      iv.setCache(true);
      iv.setFitHeight(displayStage.getHeight());
      iv.setFitWidth(displayStage.getWidth());
      iv.setStyle("-fx-background-color: BLACK");      
      iv.setImage(image);
      displayStageSP.getChildren().add(iv);
      
    }
    WebEngine webEngine = browser.getEngine();
    webEngine.documentProperty().addListener(new WebDocumentListener(webEngine));
    webEngine.loadContent("<p style=\"text-align:center; font-family:Calibri; color:#ffffff; font-size:24pt;\">Jesus is alive! Jesus is alive!<br/>He has risen from the grave and He's alive!</p>");
    displayStageSP.getChildren().add(browser);
    displayStage.show();
    if ((backdrop.endsWith(".MP4")) || (backdrop.endsWith(".MOV")) || (backdrop.endsWith(".AVI")) || (backdrop.endsWith(".WMV"))) mp.play();
  }
  
  public void hideDisplayScreen() {
    if (mp!=null) mp.stop();
    displayStage.hide();
    while (displayStageSP.getChildren().size()>0) displayStageSP.getChildren().remove(0);
    mv=null;
    mp=null;
    m=null;
    
  }
  
  
  @Override
  public void start(Stage primaryStage) throws Exception {
    // Initialise display pane.
    
    displayStage = new Stage(StageStyle.UNDECORATED);
    displayStage.setAlwaysOnTop(true);
    displayScene = new Scene(displayStageSP, displayStage.getWidth(), displayStage.getHeight(),Color.BLACK);
    displayStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    displayStage.setScene(displayScene);
        
    // Setup Control GUI.
    primaryStage.setTitle("Songbase Screen");
    
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20,20,20,20));
    
    Scene scene = new Scene(grid, 300,220);
    primaryStage.setScene(scene);
    
    // Display ON/OFF line
    
    grid.add(l_display,0,1);
    rb_on.setToggleGroup(tg_display);
    rb_off.setToggleGroup(tg_display);
    hb_display.getChildren().add(rb_off);
    rb_on.setPadding(new Insets(0,10,0,10));
    rb_off.setPadding(new Insets(0,10,0,10));
    rb_off.setSelected(true);
    hb_display.getChildren().add(rb_on);
    grid.add(hb_display, 1, 1);
    
    rb_on.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        showDisplayScreen();
      }
    });
    
    rb_off.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        hideDisplayScreen();
        
      }
    });
    // Location line
    
    grid.add(l_location,0,3);
    tf_x.setMaxWidth(50);
    tf_y.setMaxWidth(50);    
    hb_location.getChildren().add(tf_x);
    hb_location.getChildren().add(tf_y);    
    grid.add(hb_location, 1, 3);
    
    // Size line
    
    grid.add(l_size,0,4);
    tf_w.setMaxWidth(50);
    tf_h.setMaxWidth(50);    
    hb_size.getChildren().add(tf_w);
    hb_size.getChildren().add(tf_h);    
    grid.add(hb_size, 1, 4);
    
    // Detect
    
    grid.add(b_detect, 1, 5);
    b_detect.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        List<String> choices = new ArrayList<>();
        int scr_no=1;
        ObservableList<Screen> screens = Screen.getScreens();
        for (Screen scr : screens) {
          Rectangle2D bounds = scr.getBounds();
          choices.add(scr_no+": "+(int)bounds.getWidth()+"x"+(int)bounds.getHeight()+" at ("+(int)bounds.getMinX()+","+(int)bounds.getMinY()+")");
          scr_no++;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(choices.size()-1),choices);
        dialog.setTitle("Screen detection");
        dialog.setHeaderText("Screens Detected:");
        dialog.setContentText("Choose screen: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
          String r = result.get();
          int pick = Integer.parseInt(r.substring(0, r.indexOf(":")))-1;
          Screen scr = screens.get(pick);
          Rectangle2D bounds = scr.getBounds();
          tf_w.setText(String.valueOf((int)bounds.getWidth()));
          tf_h.setText(String.valueOf((int)bounds.getHeight()));
          tf_x.setText(String.valueOf((int)bounds.getMinX()));
          tf_y.setText(String.valueOf((int)bounds.getMinY()));          
        }
      }
    });
    
    // Backdrop
    
    final Stage _stage = primaryStage;
    b_backdrop.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose BackDrop Image/Movie");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new ExtensionFilter("Movie Files", "*.mp4", "*.avi", "*.wmv", "*.mov"),
                new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(_stage);
        if (selectedFile != null) {
          tf_backdrop.setText(selectedFile.getPath());
        } 
      }
    });
    grid.add(b_backdrop,0,6);
    grid.add(tf_backdrop, 1, 6);
    primaryStage.show();
    
    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        hideDisplayScreen();
        displayStage.close();
        System.exit(0);
      }
    });       
    
  }
  
  public static void main(String[] args) {
    launch(args);
  }
  
  protected class WebDocumentListener implements ChangeListener<Document> {
    private final WebEngine webEngine;
    
    public WebDocumentListener(WebEngine webEngine) {
      this.webEngine=webEngine;
    }
    
    @Override
    public void changed(ObservableValue<? extends Document> arg0, Document arg1, Document arg2) {
      try {
        Field f = webEngine.getClass().getDeclaredField("page");
        f.setAccessible(true);
        com.sun.webkit.WebPage page = (WebPage) f.get(webEngine);
        page.setBackgroundColor((new java.awt.Color(0,0,0,0)).getRGB());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  

}
