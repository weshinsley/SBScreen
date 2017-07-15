package com.teapotrecords.sbscreen.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.teapotrecords.sbscreen.SBScreen;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class WebServer {
  private HttpServer server;
  private int port = 8080;
  private WebListener listener = null;
  private SBScreen parent;
  
  public void createServer() {
    try {
      server = HttpServer.create(new InetSocketAddress(port),0);
      
      server.createContext("/",new WebHandler());
      parent.no_events++;
      this.parent.nl_on.setSelected(true);
      this.parent.tf_port.setDisable(true);
      server.start();
      
      parent.no_events--;
    } catch (Exception e) {
      if (e instanceof java.net.BindException) {
        Alert portError = new Alert(AlertType.ERROR, "The network port "+port+" is already in use. Please choose another, or close the other application that is using the port.",ButtonType.OK);
        portError.showAndWait();
        parent.no_events++;
        this.parent.nl_off.setSelected(true);
        this.parent.tf_port.setDisable(false);
        parent.no_events--;
        server=null;
      }
    }
  }
  
  public WebServer(WebListener wl, SBScreen parent)  {
    this.parent=parent;
    listener=wl;
    createServer();
  }
  
  public void setPort(int p) {
    port=p;
  }
  
  public void setEnabled(boolean en) {
    if (server==null) createServer();
    
    if (en) { 
      if (server!=null) {
        server.start();
      }
    } else {
      if (server!=null) {
        server.stop(0);
        server=null;
      }
    }
  }
  
  class WebHandler implements HttpHandler {
     @Override
     public void handle(HttpExchange t) throws IOException {
       HashMap<String,String> key_values = new HashMap<String,String>();
       if (t.getRequestMethod().equals("POST")) {
         
         String query = "";
         InputStreamReader in=null; 
         try {
           in = new InputStreamReader(t.getRequestBody(),"utf-8");
           BufferedReader br = new BufferedReader(in);
           if (br!=null) {
             query = br.readLine();
             if (query!=null) {
               query=URLDecoder.decode(query,"UTF-8");
             }
           }
         
         } catch (Exception e) { 
           e.printStackTrace(); 
         } finally {
           if (in!=null) in.close();
           
         } 
         
         String[] bits = query.split("&");
         for (int i=0; i<bits.length; i++)
           key_values.put(bits[i].substring(0, bits[i].indexOf("=")), bits[i].substring(bits[i].indexOf("=")+1));
         
       }
       String response = "OK";
       t.sendResponseHeaders(200, response.length());
       OutputStream os = t.getResponseBody();
       os.write(response.getBytes());
       os.close();
       if (listener!=null) {
         listener.receiveMessage(key_values);
       }
     }
  }
}
