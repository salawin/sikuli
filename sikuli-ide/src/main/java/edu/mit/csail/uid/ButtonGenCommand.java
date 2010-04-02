package edu.mit.csail.uid;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.net.URL;


public class ButtonGenCommand extends JButton implements ActionListener{
   String _cmd;
   String[] _params;

   public ButtonGenCommand(String cmd, String... params){
      super(getRichRepresentation(cmd, params, false));
      _cmd = cmd;
      _params = params;
      setToolTipText(getRichRepresentation(cmd, params, true));
      setHorizontalAlignment(SwingConstants.LEFT);
      addActionListener(this);
   }

   static String getParamHTML(String p, boolean first, boolean showOptParam){
      URL imgPattern = SikuliIDE.class.getResource("/icons/capture-small.png");
      String item = "";
      if(!first)
         item += ", ";
      if(p.equals("PATTERN"))
         item += "<img src=\"" + imgPattern + "\">";
      else{
         if(p.startsWith("[") && p.endsWith("]")){
            if(showOptParam)
               item += "<i>" + p +"</i>";
         }
         else{
            if(p.startsWith("_"))
               item += "<u>" + p.substring(1,2) + "</u>" + p.substring(2);
            else
               item += p;
         }
      }
      return !item.equals(", ")? item : "";
   }

   static String getRichRepresentation(String cmd, String[] params, boolean showOptParam){
      String ret = "<html><table><tr><td valign=\"middle\"><b>" + cmd + "</b>(";
      int count = 0;
      for(String p : params){
         String item = getParamHTML(p, count==0, showOptParam);
         if(!item.equals(""))
            ret += "<td valign=\"middle\">" + item;
         count++;
      }
      ret += "<td>)";
      return ret;
   }

   static String getTextRepresentation(String cmd, String[] params){
      String ret = "" + cmd + "(";
      int count = 0;
      for(String p : params){
         ret += p;
         if(++count < params.length)
            ret += ", ";
      }
      ret += ")";
      return ret;
   }

   public String toString(){
      return getTextRepresentation(_cmd, _params);
   }

   public void actionPerformed(ActionEvent ae){
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            insertCommand();
         }
      });
   }
   
   public void insertCommand() {
      SikuliIDE ide = SikuliIDE.getInstance();
      SikuliPane pane = ide.getCurrentCodePane();
      UserPreferences pref = UserPreferences.getInstance();
      int endPos = -1, endPosLen = 0;
      boolean first = true;
      pane.insertString(_cmd + "(");
      for(String p : _params){
         if( p.equals("PATTERN") ){
            Document doc = pane.getDocument();
            Element root = doc.getDefaultRootElement();
            int pos = pane.getCaretPosition();
            int lineIdx = root.getElementIndex(pos);
            Element line = root.getElement(lineIdx);
            CaptureButton btnCapture = new CaptureButton(pane, line);
            if(!first) pane.insertString(", ");
            pane.insertComponent(btnCapture);
            if( pref.getAutoCaptureForCmdButtons() ){
               btnCapture.capture(300);
            }
            first = false;
            continue;
         }
         if(!p.startsWith("[")){
            if(!first) pane.insertString(", ");
            if(p.startsWith("_") ){
               endPos = pane.getCaretPosition();
               p = p.substring(1);
            }
            endPosLen = p.length();
            pane.insertString(p);
            first = false;
         }
      }
      pane.insertString(")");
      (new SikuliEditorKit.InsertBreakAction()).insertBreak(pane);
      if(endPos>=0){
         pane.requestFocus();
         pane.setCaretPosition(endPos);
         pane.setSelectionStart(endPos);
         pane.setSelectionEnd(endPos+endPosLen);
         System.out.println("sel: " + pane.getSelectedText());
      }
   }

}
