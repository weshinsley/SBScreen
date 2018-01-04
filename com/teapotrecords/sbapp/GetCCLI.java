package com.teapotrecords.sbapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetCCLI {
  
  public static int getSongNumber(ArrayList<String> ccli) {
    int i=0; 
    while (ccli.get(i).indexOf("<strong>Song Number</strong>")==-1) i++;
    while (ccli.get(i).indexOf("<li itemprop=\"identifier\">")==-1) i++;
    String s = ccli.get(i);
    s = s.substring(s.indexOf(">")+1);
    s = s.substring(0, s.indexOf("<"));
    return Integer.parseInt(s);
  }
  
  public static ArrayList<String> getAuthors(ArrayList<String> ccli) {
    int i=0; 
    ArrayList<String> authors = new ArrayList<String>();
    while (ccli.get(i).indexOf("<strong>Authors</strong>")==-1) i++;
    i++;
    while (!ccli.get(i).trim().toUpperCase().equals("</DIV>")) {
      if (ccli.get(i).indexOf("<span itemprop=\"name\">")!=-1) {
        String s = ccli.get(i);
        s = s.substring(s.indexOf(">")+1);
        s = s.substring(0, s.indexOf("<"));
        authors.add(s);
      }
      i++;
    }
    return authors;
  }
  
  public static ArrayList<String> getCopyrights(ArrayList<String> ccli) {
    int i=0; 
    ArrayList<String> copyrights = new ArrayList<String>();
    while (ccli.get(i).indexOf("<strong>Copyrights</strong>")==-1) i++;
    i++;
    while (!ccli.get(i).trim().toUpperCase().equals("</DIV>")) {
      if (ccli.get(i).trim().toUpperCase().equals("<LI>")) i++;
      else if (ccli.get(i).trim().toUpperCase().equals("</LI>")) i++; 
      else if (ccli.get(i).trim().toUpperCase().equals("<UL>")) i++;
      else if (ccli.get(i).trim().toUpperCase().equals("</UL>")) i++;
      else {
        String s = ccli.get(i).trim();
        copyrights.add(s);
        i++;
      }
    }
    return copyrights;
  }
  
  public static String getTitle(ArrayList<String> ccli) {
    int i=0;
    while (ccli.get(i).indexOf("<title>")==-1) i++;
    String s = ccli.get(i);
    s = s.substring(s.indexOf(">")+1);
    s = s.substring(0,s.indexOf("- Song Search"));
    return s;
  }
  
  public static String getAltTitle(ArrayList<String> ccli) {
    int i=0; 
    while (ccli.get(i).indexOf("<li itemprop=\"alternateName\">")==-1) i++;
    i++;
    return ccli.get(i).trim();
  }
  
  public static void main(String[] args) {
    ArrayList<String> ccli_web = new ArrayList<String>();
    String cclino = "3933987";
    String s = "https://uk.search.ccli.com/search/results/?searchText="+cclino;
    InputStream is = null;
    URL url;
    String line;
    BufferedReader br;
    try {
      url = new URL(s);
      is = url.openStream();  // throws an IOException
      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) ccli_web.add(line);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (is != null) is.close();
      } catch (IOException ioe) {}
    }
    System.out.println(getTitle(ccli_web));
    System.out.println(getAltTitle(ccli_web));
    System.out.println(getSongNumber(ccli_web));
    System.out.println(getAuthors(ccli_web));
    System.out.println(getCopyrights(ccli_web));
    
  }
}
