package com.teapotrecords.sbscreen;


import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;

import com.sun.webkit.WebPage;
import com.teapotrecords.sbscreen.network.WebServer;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
  final Label l_font = new Label("Font Family");
  final Label l_fontsize = new Label("Font Size");
  final Label l_cols = new Label("Text Colour");
  final Label l_port = new Label("Network Port");  
  final ToggleGroup tg_display = new ToggleGroup();
  final HBox hb_display = new HBox();
  final HBox hb_location = new HBox();
  final HBox hb_size = new HBox();
  final HBox hb_cols = new HBox();
  final GridPane grid = new GridPane();
  final TextField tf_x  = new TextField();
  final TextField tf_y  = new TextField();
  final TextField tf_w  = new TextField();
  final TextField tf_h  = new TextField();
  final TextField tf_port = new TextField();
  final Button b_detect = new Button("Detect Fullscreen");
  final Button b_backdrop = new Button("Backdrop");
  final ColorPicker cp_fontcol = new ColorPicker(Color.WHITE);
  final ColorPicker cp_shadow = new ColorPicker(Color.GRAY);
  final CheckBox tb_shadow = new CheckBox("Shadow");
  final TextField tf_backdrop = new TextField();
  ChoiceBox cb_fonts;
  final Spinner<Integer> sp_fontsize = new Spinner<Integer>();  
  
  final StackPane displayStageSP = new StackPane();
  Scene displayScene = null;
  final WebView browser = new WebView();
  Media m;
  MediaPlayer mp;
  MediaView mv;
  final ImageView iv = new ImageView();
  Image image;
  
  private WebServer webServer =  null;
  private UpdateListener updater = null;
  public WebEngine webEngine = null;
    
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
    webEngine = browser.getEngine();
    webEngine.documentProperty().addListener(new WebDocumentListener(webEngine));
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
    
    int gridy=0;
    
    updater = new UpdateListener(this);
    webServer = new WebServer(updater);
    webServer.setEnabled(true);
    
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
    
    Scene scene = new Scene(grid, 300,280);
    primaryStage.setScene(scene);
    
    // Display ON/OFF line
    
    grid.add(l_display,0,gridy);
    rb_on.setToggleGroup(tg_display);
    rb_off.setToggleGroup(tg_display);
    hb_display.getChildren().add(rb_off);
    rb_on.setPadding(new Insets(0,10,0,10));
    rb_off.setPadding(new Insets(0,10,0,10));
    rb_off.setSelected(true);
    hb_display.getChildren().add(rb_on);
    grid.add(hb_display, 1, gridy++);
    
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
    
    grid.add(l_location,0,gridy);
    tf_x.setMaxWidth(50);
    tf_y.setMaxWidth(50);    
    hb_location.getChildren().add(tf_x);
    hb_location.getChildren().add(tf_y);    
    grid.add(hb_location, 1, gridy++);
    
    // Size line
    
    grid.add(l_size,0,gridy);
    tf_w.setMaxWidth(50);
    tf_h.setMaxWidth(50);    
    hb_size.getChildren().add(tf_w);
    hb_size.getChildren().add(tf_h);    
    grid.add(hb_size, 1, gridy++);
    
    // Detect
    
    grid.add(b_detect, 1, gridy++);
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
    tf_backdrop.setMaxWidth(150);
    grid.add(b_backdrop,0,gridy);
    grid.add(tf_backdrop, 1, gridy++);
    primaryStage.show();
    
    // Font family.
    
    List<String> fonts = javafx.scene.text.Font.getFamilies();
    cb_fonts = new ChoiceBox(FXCollections.observableArrayList(fonts));
    cb_fonts.setMaxWidth(150);
    String default_font;
    if (fonts.contains("Calibri")) default_font="Calibri";
    else if (fonts.contains("Arial")) default_font="Arial";
    else if (fonts.contains("Lucida Grande")) default_font="Lucida Grande";
    else if (fonts.contains("sans-serif")) default_font="sans-serif";
    else default_font = fonts.get(0);
    cb_fonts.getSelectionModel().select(default_font);
    grid.add(l_font, 0, gridy);
    grid.add(cb_fonts, 1, gridy++);
    
    // Font size
    
    sp_fontsize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8,64, 28));
    grid.add(l_fontsize, 0, gridy);
    grid.add(sp_fontsize, 1, gridy++);
    
    // Colours
    
    grid.add(l_cols, 0, gridy);
    grid.add(cp_fontcol,1,gridy++);
    grid.add(tb_shadow,0,gridy);
    grid.add(cp_shadow, 1, gridy++);
        
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
