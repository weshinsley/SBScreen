package com.teapotrecords.sbscreen.network;

import java.util.HashMap;

public interface WebListener {
  public void receiveMessage(HashMap<String,String> args);

}
