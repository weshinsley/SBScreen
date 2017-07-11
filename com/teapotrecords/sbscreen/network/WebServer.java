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

public class WebServer {
  private HttpServer server;
  private int port = 8080;
  private WebListener listener = null;
  private boolean enabled = false;
  
  public WebServer(WebListener wl) throws IOException {
    server = HttpServer.create(new InetSocketAddress(port),0);
    server.createContext("/",new WebHandler());
    listener=wl;
  }
  
  public void setPort(int p) throws IOException {
    setEnabled(false);
    port=p;
    setEnabled(true);
  }
  
  public void setEnabled(boolean en) throws IOException {
    if (enabled!=en) {
      if (en) server.start();
      else server.stop(0);
      enabled=en;
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