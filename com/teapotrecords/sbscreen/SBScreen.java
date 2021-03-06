package com.teapotrecords.sbscreen;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
import javafx.util.Duration;

public class SBScreen extends Application {
  public final static String sbs_version = "0.28";
  public final static String date = "16th July 2017";
  
  Stage displayStage;
  boolean unsaved_changes = false;
  
  // Control GUI components
  public int no_events = 0;
  final RadioButton rb_on = new RadioButton("ON");
  final RadioButton rb_off = new RadioButton("OFF");
  public final RadioButton nl_on = new RadioButton("ON");
  public final RadioButton nl_off = new RadioButton("OFF");
  final ObservableList<String> config_choices = FXCollections.observableArrayList();
  final ChoiceBox<String> cb_configs = new ChoiceBox<String>(config_choices);
  final Label l_display = new Label("Display");
  final Label l_location = new Label("Location (x,y)");
  final Label l_size = new Label("Size (w,h)");
  final Label l_font = new Label("Font Family");
  final Label l_fontsize = new Label("Font Size");
  final Label l_cols = new Label("Text Colour");
  final Label l_port = new Label("Network Port");
  final Label l_neton = new Label("Net Listener");
  final Label l_config = new Label("Saved Settings:");
  final ToggleGroup tg_display = new ToggleGroup();
  final ToggleGroup tg_net = new ToggleGroup();
  final HBox hb_display = new HBox();
  final HBox hb_net = new HBox();
  final HBox hb_location = new HBox();
  final HBox hb_size = new HBox();
  final HBox hb_cols = new HBox();
  final HBox hb_configs = new HBox();
  final GridPane grid = new GridPane();
  final TextField tf_x = new TextField();
  final TextField tf_y = new TextField();
  final TextField tf_w = new TextField();
  final TextField tf_h = new TextField();
  public final TextField tf_port = new TextField("8080");
  final Button b_detect = new Button("Detect Fullscreen");
  final Button b_backdrop = new Button("Backdrop");
  final Button b_saveConfig = new Button("Save");
  final Button b_saveAsConfig = new Button("Save As");
  final Button b_delConfig = new Button("Delete");
  final Button b_netInfo = new Button("Net Info");
  final Button b_appInfo = new Button("App Info");  
  final ColorPicker cp_fontcol = new ColorPicker(Color.WHITE);
  final ColorPicker cp_shadow = new ColorPicker(Color.GRAY);
  final CheckBox tb_shadow = new CheckBox("Shadow");
  final TextField tf_backdrop = new TextField();
  ChoiceBox<String> cb_fonts;
  final Spinner<Integer> sp_fontsize = new Spinner<Integer>();

  final StackPane displayStageSP = new StackPane();
  Scene displayScene = null;
  final WebView browser = new WebView();
  Media m;
  MediaPlayer mp;
  MediaView mv;
  final ImageView iv = new ImageView();
  Image image;
  Element configs_xml;

  private WebServer webServer = null;
  private UpdateListener updater = null;
  public WebEngine webEngine = null;
  
  public void showDisplayScreen(boolean smoothly) {
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
      if (System.getProperty("os.name").equals("Mac OS X")) {
        mp.setCycleCount(1);
        mp.setOnEndOfMedia(new Runnable() {
          public void run() {
            mp.seek(Duration.ZERO);
            mp.play();
          }
        });
      } else {
        mp.setCycleCount(MediaPlayer.INDEFINITE);
      }
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
    webEngine.loadContent("<p></p>");
    displayStageSP.getChildren().add(browser);
    
    if (!smoothly) displayStage.show();
    if ((backdrop.endsWith(".MP4")) || (backdrop.endsWith(".MOV")) || (backdrop.endsWith(".AVI")) || (backdrop.endsWith(".WMV")))
      mp.play();
  }

  public void hideDisplayScreen(boolean smoothly) {
    if (mp != null) mp.stop();
    if (!smoothly) displayStage.hide();
    while (displayStageSP.getChildren().size() > 0) displayStageSP.getChildren().remove(0);
    mv = null;
    mp = null;
    m = null;
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    int gridy = 0;
    updater = new UpdateListener(this);
    
    primaryStage.getIcons().add(new Image("file:resources/sbscreen_icon.png"));
    
    
    
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 20, 20, 20));

    // Display ON/OFF line

    grid.add(l_display, 0, gridy);
    rb_on.setToggleGroup(tg_display);
    rb_off.setToggleGroup(tg_display);
    hb_display.getChildren().add(rb_off);
    rb_on.setPadding(new Insets(0, 10, 0, 10));
    rb_off.setPadding(new Insets(0, 10, 0, 10));
    rb_off.setSelected(true);
    hb_display.getChildren().add(rb_on);
    grid.add(hb_display, 1, gridy++);

    rb_on.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        showDisplayScreen(false);
      }
    });

    rb_off.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        hideDisplayScreen(false);

      }
    });

    grid.add(l_config, 0, gridy);
    grid.add(cb_configs, 1, gridy++);
    hb_configs.getChildren().add(b_saveConfig);
    hb_configs.getChildren().add(b_saveAsConfig);
    hb_configs.getChildren().add(b_delConfig);

    grid.add(hb_configs, 1, gridy++);

    // Location line

    grid.add(l_location, 0, gridy);
    tf_x.setMaxWidth(50);
    tf_y.setMaxWidth(50);
    hb_location.getChildren().add(tf_x);
    hb_location.getChildren().add(tf_y);
    grid.add(hb_location, 1, gridy++);

    // Size line

    grid.add(l_size, 0, gridy);
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
        int scr_no = 1;
        ObservableList<Screen> screens = Screen.getScreens();
        for (Screen scr : screens) {
          Rectangle2D bounds = scr.getBounds();
          choices.add(scr_no + ": " + (int) bounds.getWidth() + "x"
              + (int) bounds.getHeight() + " at (" + (int) bounds.getMinX()
              + "," + (int) bounds.getMinY() + ")");
          scr_no++;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(choices.size() - 1), choices);
        dialog.setTitle("Screen detection");
        dialog.setHeaderText("Screens Detected:");
        dialog.setContentText("Choose screen: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
          String r = result.get();
          int pick = Integer.parseInt(r.substring(0, r.indexOf(":"))) - 1;
          Screen scr = screens.get(pick);
          Rectangle2D bounds = scr.getBounds();
          tf_w.setText(String.valueOf((int) bounds.getWidth()));
          tf_h.setText(String.valueOf((int) bounds.getHeight()));
          tf_x.setText(String.valueOf((int) bounds.getMinX()));
          tf_y.setText(String.valueOf((int) bounds.getMinY()));
          unsaved_changes = true;
          b_saveConfig.setDisable(false);
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
            new ExtensionFilter("Pictures and Movies", "*.png", "*.jpg", "*.gif","*.mp4", "*.avi", "*.wmv", "*.mov"),
            new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(_stage);
        if (selectedFile != null) {
          tf_backdrop.setText(selectedFile.getPath());
        }
        unsaved_changes = true;
        b_saveConfig.setDisable(false);
        if (rb_on.isSelected()) {
          hideDisplayScreen(true);
          showDisplayScreen(true);
        }
      }
    });
    tf_backdrop.setEditable(false);
    tf_backdrop.setMaxWidth(150);
    grid.add(b_backdrop, 0, gridy);
    grid.add(tf_backdrop, 1, gridy++);
    
    // Font family.

    List<String> fonts = javafx.scene.text.Font.getFamilies();
    cb_fonts = new ChoiceBox<String>(FXCollections.observableArrayList(fonts));
    cb_fonts.setMaxWidth(150);
    String default_font;
    if (fonts.contains("Calibri")) default_font = "Calibri";
    else if (fonts.contains("Arial")) default_font = "Arial";
    else if (fonts.contains("Lucida Grande")) default_font = "Lucida Grande";
    else if (fonts.contains("sans-serif")) default_font = "sans-serif";
    else default_font = fonts.get(0);
    cb_fonts.getSelectionModel().select(default_font);
    grid.add(l_font, 0, gridy);
    grid.add(cb_fonts, 1, gridy++);

    // Font size

    sp_fontsize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 64, 28));
    grid.add(l_fontsize, 0, gridy);
    grid.add(sp_fontsize, 1, gridy++);

    // Colours

    grid.add(l_cols, 0, gridy);
    grid.add(cp_fontcol, 1, gridy++);
    grid.add(tb_shadow, 0, gridy);
    grid.add(cp_shadow, 1, gridy++);

    // Network

    grid.add(l_neton, 0, gridy);
    nl_on.setToggleGroup(tg_net);
    nl_off.setToggleGroup(tg_net);
    hb_net.getChildren().add(nl_off);
    nl_on.setPadding(new Insets(0, 10, 0, 10));
    nl_off.setPadding(new Insets(0, 10, 0, 10));
    no_events++;
    nl_off.setSelected(true);
    no_events--;
    hb_net.getChildren().add(nl_on);
    grid.add(hb_net, 1, gridy++);
    
    nl_on.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        if (no_events==0) {
          no_events++;
          webServer.setEnabled(true);
          no_events--;
        }
      }
    });

    nl_off.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        if (no_events==0) {
          no_events++;
          webServer.setEnabled(false);
          tf_port.setDisable(false);
          no_events--;
        }
      }
    });

    grid.add(l_port, 0, gridy);
    grid.add(tf_port, 1, gridy++);
    
    tf_port.setDisable(false);
    
    tf_port.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
        int p = 8080;
        try {
          p = Integer.parseInt(tf_port.getText());
          if (p < 0) p = 8080;
        } catch (Exception e) {
        }
        tf_port.setText(String.valueOf(p));
        webServer.setPort(p);
        unsaved_changes = true;
        b_saveConfig.setDisable(false);
      }
    });

    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        hideDisplayScreen(false);
        displayStage.close();
        System.exit(0);
      }
    });
    
    grid.add(b_appInfo, 0, gridy);
    grid.add(b_netInfo, 1,gridy++);
    b_netInfo.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        String s="",t="";
        int c=0;
        try {
          Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
          while (nets.hasMoreElements()) {
            NetworkInterface intf = nets.nextElement();
            Enumeration<InetAddress> ips = intf.getInetAddresses();
            while (ips.hasMoreElements()) {
              InetAddress ia = ips.nextElement();
              if (!ia.isLoopbackAddress()) {
                t=ia.getHostAddress().toString()+"\n";
                c++;
                if (t.indexOf(".")>0) s+=t;
              }
            }
          }
        } catch (Exception ex) {}
        Alert info = new Alert(AlertType.INFORMATION);
        info.setTitle("Network Information");
        if (c==1) info.setHeaderText("Listening for requests on this IP address:");
        else if (c>1) info.setHeaderText("Listening for requests on these IP addresses:");
        else if (c<=0) info.setHeaderText("Error - I don't seem to have an IP address to listen with.");
        info.setContentText(s);
        info.show();
        
      }
    });
    
    b_appInfo.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        Alert info = new Alert(AlertType.INFORMATION);
        info.setTitle("About SBScreen");
        info.setHeaderText("SBScreen "+sbs_version);
        info.setContentText(date+"\nwes@teapotrecords.co.uk\nhttps://github.com/weshinsley/SBScreen"); 
        info.show();
      }
    });


    
    b_saveConfig.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        Node configTag = Tools.getTagWhereAttr(configs_xml, "config", "name", cb_configs.getValue().toString());
        GUItoXML(configTag);
        Tools.writeXML(configs_xml, "configs.xml");
        unsaved_changes = false;
        b_saveConfig.setDisable(true);
      }
    });

    b_saveAsConfig.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        TextInputDialog tid = new TextInputDialog();
        tid.setTitle("Please enter name for new config");
        tid.setHeaderText(null);
        
        Optional<String> result = tid.showAndWait();
        if (result.isPresent()) {
          String res = result.get().trim();
          if (Tools.getTagWhereAttr(configs_xml, "config", "name", res) != null) {
            Alert dupError = new Alert(AlertType.ERROR, "A configuration with that name already exists.", ButtonType.OK);
            dupError.showAndWait();
          } else {
            Element n = Tools.addTag(configs_xml, "config");
            Tools.setAttribute(n, "name", res);
            GUItoXML(n);
            Tools.setAttribute(Tools.getTag(configs_xml, "recent"), "name", res);
            Tools.writeXML(configs_xml, "configs.xml");
            unsaved_changes = false;
            b_saveConfig.setDisable(true);
            config_choices.add(res);
            FXCollections.sort(config_choices);
            cb_configs.getSelectionModel().select(res);
            
          }
        }
      }
    });

    b_delConfig.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        if (Tools.countChildren(configs_xml, "config") <= 1) {
          Alert zeroError = new Alert(AlertType.ERROR,"Can't delete the only configuration...", ButtonType.OK);
          zeroError.showAndWait();
        } else {
          Alert confirmDel = new Alert(AlertType.CONFIRMATION, "Confirm deleting this configuration...", ButtonType.OK, ButtonType.CANCEL);
          Optional<ButtonType> result = confirmDel.showAndWait();
          if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            Node x = Tools.getTagWhereAttr(configs_xml, "config", "name", cb_configs.getValue().toString());
            x.getParentNode().removeChild(x);
            int index = config_choices.indexOf(cb_configs.getValue().toString());
            config_choices.remove(index);
            while (index >= config_choices.size()) index--;
            cb_configs.getSelectionModel().select(index);
            Tools.setAttribute(Tools.getTag(configs_xml, "recent"), "name", cb_configs.getValue().toString());
            Tools.writeXML(configs_xml, "configs.xml");
            unsaved_changes = false;
            b_saveConfig.setDisable(true);
          }
        }
      }
    });

    cb_configs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String number, String number2) {
        boolean proceed = (!unsaved_changes);
        if (unsaved_changes) {
          Alert confirmDel = new Alert(AlertType.CONFIRMATION,
              "There are unsaved changes to this configuration. Proceed anyway?",
              ButtonType.OK, ButtonType.CANCEL);
          Optional<ButtonType> result = confirmDel.showAndWait();
          if ((result.isPresent()) && (result.get() == ButtonType.OK)) proceed = true;
        }
        
        if (proceed) {
          cb_configs.getSelectionModel().select(number2);
          Node config = Tools.getTagWhereAttr(configs_xml, "config", "name",cb_configs.getValue().toString());
          XMLtoGUI(config);
          Tools.setAttribute(Tools.getTag(configs_xml, "recent"), "name", cb_configs.getValue().toString());
          Tools.writeXML(configs_xml, "configs.xml");
          unsaved_changes = false;
          b_saveConfig.setDisable(true);
        } else {
          cb_configs.getSelectionModel().select(number);
        }
      }
    });

    EventHandler<ActionEvent> unsaveAndResizeEvent = new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        changeTF();
      }
    };

    EventHandler<ActionEvent> unsaveAndRefreshEvent = new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        unsaved_changes = true;
        b_saveConfig.setDisable(false);
        updater.refresh();
      }
    };

    tf_x.setOnAction(unsaveAndResizeEvent);
    tf_y.setOnAction(unsaveAndResizeEvent);
    tf_w.setOnAction(unsaveAndResizeEvent);
    tf_h.setOnAction(unsaveAndResizeEvent);
    
    
    sp_fontsize.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        unsaved_changes = true;
        b_saveConfig.setDisable(false);
        updater.refresh();

      }
    });

    cb_fonts.setOnAction(unsaveAndRefreshEvent);
    cp_fontcol.setOnAction(unsaveAndRefreshEvent);
    cp_shadow.setOnAction(unsaveAndRefreshEvent);
    tb_shadow.setOnAction(unsaveAndRefreshEvent);

    displayStage = new Stage(StageStyle.UNDECORATED);
    displayStage.getIcons().add(new Image("file:resources/sbscreen_icon.png"));
    displayStage.setAlwaysOnTop(true);
    displayScene = new Scene(displayStageSP, displayStage.getWidth(), displayStage.getHeight(), Color.BLACK);
    displayStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    displayStage.setScene(displayScene);
    displayStage.setResizable(false);
    
    displayStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(WindowEvent we) {
        hideDisplayScreen(false);
        rb_off.setSelected(true);
      }
    });
    
    displayScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent e) {
        if (e.getCode()==KeyCode.ESCAPE) {
          hideDisplayScreen(false);
          rb_off.setSelected(true);
        }
      }
    });
    
       
    loadXML();
    b_saveConfig.setDisable(true);
    unsaved_changes = false;

    webServer = new WebServer(updater, this);
    webEngine = browser.getEngine();
    webEngine.documentProperty().addListener(new WebDocumentListener(webEngine));
    Scene scene = new Scene(grid);
    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent e) {
        if (e.getCode()==KeyCode.ESCAPE) {
          hideDisplayScreen(false);
          rb_off.setSelected(true);
        }
      }
    });

    primaryStage.setScene(scene);
    primaryStage.sizeToScene();
    primaryStage.setTitle("Songbase Screen");
    primaryStage.setResizable(false);
    primaryStage.show();
    
    // Catch tabs on text fields.
    
    
    
    tf_w.setOnKeyReleased(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent ke) {
        if (ke.getCode()==KeyCode.TAB) updateVal(tf_w);
      }
    });
    
    tf_h.setOnKeyReleased(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent ke) {
        if (ke.getCode()==KeyCode.TAB) updateVal(tf_h);
      }
    });
    
    tf_x.setOnKeyReleased(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent ke) {
        if (ke.getCode()==KeyCode.TAB) updateVal(tf_x);
      }
    });
    
    tf_y.setOnKeyReleased(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent ke) {
        if (ke.getCode()==KeyCode.TAB) updateVal(tf_y);
      }
    });
    
    tf_port.setOnKeyReleased(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent ke) {
        if (ke.getCode()==KeyCode.TAB) updateVal(tf_y);
      }
    });


  }
  

  void changeTF() {
    unsaved_changes = true;
    b_saveConfig.setDisable(false);
    displayStage.setX(Integer.parseInt(tf_x.getText()));
    displayStage.setY(Integer.parseInt(tf_y.getText()));
    displayStage.setWidth(Integer.parseInt(tf_w.getText()));
    displayStage.setHeight(Integer.parseInt(tf_h.getText()));
  }
  
  void updateVal(TextField tf) {
    try {
      Integer.parseInt(tf.getText());
      changeTF();
    } catch (Exception e) { tf.setText("0"); }
  }
  
  public void createDummyXML() {
    try {
      PrintWriter PW = new PrintWriter(new File("configs.xml"));
      PW.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
      PW.println("<configs>");
      PW.println("  <recent name=\"Default Config\"/>");
      PW.println("    <config name=\"Default Config\" x=\"1920\" y=\"0\" w=\"1920\" h=\"1080\" b=\"\" f=\"Arial\" fs=\"28\" c=\"#ffffff\" sh=\"1\" sc=\"#808080\" p=\"8080\" />");
      PW.println("</configs>");
      PW.close();
    } catch (Exception e) {
    }
  }

  public void GUItoXML(Node tag) {
    Tools.setAttribute(tag, "x", tf_x.getText());
    Tools.setAttribute(tag, "y", tf_y.getText());
    Tools.setAttribute(tag, "w", tf_w.getText());
    Tools.setAttribute(tag, "h", tf_h.getText());
    Tools.setAttribute(tag, "p", tf_port.getText());
    Tools.setAttribute(tag, "b", tf_backdrop.getText());
    Tools.setAttribute(tag, "sh", (tb_shadow.isSelected() ? "1" : "0"));
    Tools.setAttribute(tag, "fs", String.valueOf(sp_fontsize.getValueFactory().getValue()));
    Tools.setAttribute(tag, "f", cb_fonts.getSelectionModel().getSelectedItem().toString());
    Tools.setAttribute(tag, "c", Tools.toHex(cp_fontcol));
    Tools.setAttribute(tag, "sc", Tools.toHex(cp_shadow));
  }

  public void XMLtoGUI(Node tag) {
    tf_x.setText(Tools.getAttribute(tag, "x"));
    tf_y.setText(Tools.getAttribute(tag, "y"));
    tf_w.setText(Tools.getAttribute(tag, "w"));
    tf_h.setText(Tools.getAttribute(tag, "h"));
    tf_port.setText(Tools.getAttribute(tag, "p"));
    tf_backdrop.setText(Tools.getAttribute(tag, "b"));
    tb_shadow.setSelected(Tools.getAttribute(tag, "sh").equals("1"));
    sp_fontsize.getValueFactory().setValue(Integer.parseInt(Tools.getAttribute(tag, "fs")));
    cb_fonts.getSelectionModel().select(Tools.getAttribute(tag, "f"));
    Tools.fromHex(Tools.getAttribute(tag, "c"), cp_fontcol);
    Tools.fromHex(Tools.getAttribute(tag, "sc"), cp_shadow);
    displayStage.setX(Integer.parseInt(tf_x.getText()));
    displayStage.setY(Integer.parseInt(tf_y.getText()));
    displayStage.setWidth(Integer.parseInt(tf_w.getText()));
    displayStage.setHeight(Integer.parseInt(tf_h.getText()));
    updater.refresh();

  }

  public void loadXML() {
    if (!new File("configs.xml").exists()) createDummyXML();
    configs_xml = Tools.loadDocument("configs.xml");
    int countConfigs = Tools.countChildren(configs_xml, "config");
    if (countConfigs == 0) {
      createDummyXML();
      configs_xml = Tools.loadDocument("configs.xml");
    }
    for (int i = 0; i < Tools.countChildren(configs_xml, "config"); i++) {
      config_choices.add(Tools.getAttribute(Tools.getChildNo(configs_xml, "config", i), "name"));
    }
    FXCollections.sort(config_choices);
    String recent = Tools.getAttribute(Tools.getTag(configs_xml, "recent"),"name");
    if (!config_choices.contains(recent)) {
      recent = Tools.getAttribute(Tools.getChildNo(configs_xml,"config",0),"name");
      Tools.setAttribute(Tools.getTag(configs_xml, "recent"),"name",recent);
      Tools.writeXML(configs_xml, "configs.xml");
    }
    cb_configs.getSelectionModel().select(recent);
  }

  public static void main(String[] args) {
    launch(args);
  }

  protected class WebDocumentListener implements ChangeListener<Document> {
    private final WebEngine wdl_webEngine;

    public WebDocumentListener(WebEngine webEngine) {
      wdl_webEngine = webEngine;
    }

    @Override
    public void changed(ObservableValue<? extends Document> arg0, Document arg1, Document arg2) {
      try {
        Field f = wdl_webEngine.getClass().getDeclaredField("page");
        f.setAccessible(true);
        Object page = f.get(wdl_webEngine);
        Method meth = page.getClass().getMethod("setBackgroundColor",int.class);
        meth.setAccessible(true);
        meth.invoke(page, (new java.awt.Color(0, 0, 0, 0)).getRGB());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
