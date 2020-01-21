package com.teapotrecords.sbscreen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.w3c.dom.Document;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class TestWebView extends Application {
  
  public void start(Stage stage) throws Exception {
    StackPane stackpane = new StackPane();
    Scene scene = new Scene(stackpane, stage.getWidth(), stage.getHeight(), Color.BLACK);
    stage.setScene(scene);
    scene.setFill(Color.BLACK);
    stackpane.setStyle("-fx-background-color: BLACK");
    WebView webview = new WebView();
    stackpane.getChildren().add(webview);
    WebEngine webengine = webview.getEngine();
    webengine.documentProperty().addListener(new WebDocumentListener(webengine));
    webengine.loadContent("<p style='color:white'>Hello World</p>");
    
    stage.show();
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
        Method meth = page.getClass().getMethod("setBackgroundColor",  int.class);
        meth.setAccessible(true);
        meth.invoke(page, (new java.awt.Color(0, 0, 0, 0)).getRGB());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
