package com.teapotrecords.sbscreen;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;

import com.teapotrecords.sbscreen.network.WebListener;

import javafx.application.Platform;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class UpdateListener implements WebListener {
  private final String LYRICS_TAG = "lyrics";
  private final String COMMAND_TAG = "command";
  private Decoder b64decode = Base64.getDecoder();
  public SBScreen parent = null;

  
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
  
  public String toHex(ColorPicker cp) {
    Color c = cp.getValue();
    int r = (int) (c.getRed()*100);
    int g = (int) (c.getGreen()*100);
    int b = (int) (c.getGreen()*100);
    return "#"+Integer.toHexString(r)+Integer.toHexString(g)+Integer.toHexString(b);
    
  }
  public synchronized void displayHTML(String html) {
    html="<p style=\"text-align:center;font-family:"+parent.cb_fonts.getSelectionModel().getSelectedItem()+";"+
        "font-size:"+parent.sp_fontsize.getValue()+"pt;"+
        "color:"+toHex(parent.cp_fontcol)+";"+
        (parent.tb_shadow.isSelected()?"text-shadow:2px 2px "+toHex(parent.cp_shadow)+";":"")+"\">"+html+"</p>";
    if (parent.webEngine!=null) parent.webEngine.loadContent(html);
  }
  

}
