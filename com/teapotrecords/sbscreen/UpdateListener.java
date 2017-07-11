package com.teapotrecords.sbscreen;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;

import com.teapotrecords.sbscreen.network.WebListener;

import javafx.application.Platform;

public class UpdateListener implements WebListener {
  private final String LYRICS_TAG = "lyrics";
  private final String COMMAND_TAG = "command";
  private Decoder b64decode = Base64.getDecoder();
  private SBScreen parent;
  
  public UpdateListener(SBScreen parent) {
    this.parent=parent;
  }
  @Override
  public void receiveMessage(HashMap<String, String> hash) {
    
    final HashMap<String,String> x = hash;
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        final String command = x.get(COMMAND_TAG);
        final String b64code = x.get(LYRICS_TAG);
        if (b64code!=null) {
          final String html = new String(b64decode.decode(b64code));
          if (command.equals(LYRICS_TAG)) displayHTML(html);
        }
      }
    });
    parent.webEngine.reload();
  }
  
  public synchronized void displayHTML(String html) {
    html="<p style=\"text-align:center;font-family:Arial,Calibri;font-size:28pt;color:#ffffff;text-shadow:2px 2px #000000;\">"+html+"</p>";
    parent.webEngine.loadContent(html);
  }
  

}
