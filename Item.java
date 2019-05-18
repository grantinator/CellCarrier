//------------------------------
//@version 1.0 6-25-2018
//@author Grant Baum
//File Name: CellCarrier.java
//Program Purpose: CellCarrier.java is a simplified cell service provider.
//The program stores individual accounts, identified by the owners number,
//and allows the user either: print all their accounts with charges and
//message info, delete the first media type message from every account,
//or delete an entire account. The user can do any of these options as 
//many times as they like until they choose to quit the application. 
//Revision History:
//Date        Programmer Name    ChangeID    Description
//06/18/2018  Grant Baum       20163888     Initial Implementation
//----------------------------
public class Item {

   private int time;
   private String from;
   private String to;
   private double charge;

   public Item() {
      time = 0;
      from = "-----------";
      to = "------------";
      charge = 0.0;
   }

   public Item(int time, String from, String to, double charge) {
      this.time = time;
      this.from = from;
      this.to = to;
      this.charge = charge;
   }

   public String toString() {
      String returnString = String.format("%-15s %-20s %-18s%n",
            "Time:" + time + ",", "From:" + from + ",", "To:" + to + ",");
      return returnString;
   }

   public int getTime() {
      return time;
   }

   public String getFrom() {
      return from;
   }

   public String getTo() {
      return to;
   }

   public double getCharge() {
      return charge;
   }

   public void setTime(int time) {
      this.time = time;
   }

   public void setFrom(String sender) {
      this.from = sender;
   }

   public void setTo(String receiver) {
      this.to = receiver;
   }

   public void setCharge(double charge) {
      this.charge = charge;
   }
}

class Message<T> extends Item {

   private T format;

   public Message() {
      super();
      format = null;
   }

   public Message(int time, String from, String to, double charge, T format) {
      super(time, from, to, charge);
      this.format = format;
   }

   public String toString() {

      String returnString = format.toString();
      returnString += super.toString();

      return returnString;
   }

   public T getFormat() {
      return format;
   }

   public void setFormat(T format) {
      this.format = format;
   }
}

class Text {

   private String content;

   public Text() {
      content = "";
   }

   public Text(String text) {
      content = text;
   }

   public String toString() {
      return String.format("%-48s", "\tTEXT: " + content);
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }
}

class Media {

   private double size;
   private String format;

   public Media() {
      size = 0;
      format = "";
   }

   public Media(double size, String format) {
      this.size = size;
      this.format = format;
   }

   public String toString() {
      return String.format("%-48s",
            "\tMEDIA: Size: " + size + " MB, Format: " + format);
   }

   public double getSize() {
      return size;
   }

   public String getFormat() {
      return format;
   }

   public void setSize(double size) {
      this.size = size;
   }

   public void setFormat(String format) {
      this.format = format;
   }
}

class Voice {

   private int duration;
   private String format;

   public Voice() {
      duration = 0;
      format = "";
   }

   public Voice(int duration, String format) {
      this.duration = duration;
      this.format = format;
   }

   public String toString() {
      return String.format("%-48s",
            "\tVOICE: Duration: " + duration + "(sec), Format:" + format);
   }

   public int getDuration() {
      return duration;
   }

   public String getFormat() {
      return format;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   public void setFormat(String format) {
      this.format = format;
   }
}
