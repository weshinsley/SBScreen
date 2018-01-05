package com.teapotrecords.sbapp;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

public class SBApp {
  final static String version = "0.1";
  JFrame sbApp = new JFrame();
  JMenu mFile = new JMenu("File");
  JMenuBar topMenu = new JMenuBar();
  JMenuItem miNew = new JMenuItem("New");;
  JMenuItem miOpen = new JMenuItem("Open");
  JMenuItem miExit = new JMenuItem("Exit");
  SBEventHandler eh = new SBEventHandler();  
  public void initMenus() {
    miNew.setMnemonic(KeyEvent.VK_N);
    miOpen.setMnemonic(KeyEvent.VK_O);
    mFile.setMnemonic(KeyEvent.VK_F);
    miExit.setMnemonic(KeyEvent.VK_X);
    mFile.add(miNew);
    mFile.add(miOpen);
    mFile.add(miExit);
    topMenu.add(mFile);
    miExit.addActionListener(eh);
    sbApp.setJMenuBar(topMenu);
  }    
  
  
  public static void main(String[] args) {
    final SBApp sb = new SBApp();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        sb.runApp();
      }
    });
  }

  private void runApp() {
    sbApp = new JFrame("BFree "+version);
    sbApp.setSize(640, 480);
    sbApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initMenus();
    sbApp.setVisible(true);
    
    
    
  }
  
  
  
  class SBEventHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (e.getSource()==miExit) {
        System.exit(0);
      } 
    }
    
  }
}
