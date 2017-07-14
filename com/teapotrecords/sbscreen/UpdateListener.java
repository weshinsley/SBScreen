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
  public SBScreen parent = null;
  private String remember_html = "";

  
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
          String html = new String(b64decode.decode(b64code));
          html=html.replaceAll("\n", "<br/>");
          if (command.equals(LYRICS_TAG)) displayHTML(html);
        }
      }
    });
    parent.webEngine.reload();
  }
  

  public synchronized void refresh() {
    displayHTML(remember_html);
  }
  
  public synchronized void displayHTML(String html) {
    remember_html=html;
    html="<body style=\"overflow-x:hidden;overflow-y:hidden\"><p style=\"text-align:center;font-family:"+parent.cb_fonts.getSelectionModel().getSelectedItem()+";"+
        "font-size:"+parent.sp_fontsize.getValue()+"pt;"+
        "color:"+Tools.toHex(parent.cp_fontcol)+";"+
        (parent.tb_shadow.isSelected()?"text-shadow:1px 1px "+Tools.toHex(parent.cp_shadow)+";":"")+"\">"+html+"</p></body>";
    if (parent.webEngine!=null) parent.webEngine.loadContent(html);
  }
  

}
