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
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//----------------------------
public class CellCarrier {

   public static void main(String[] args) {
      SmartCarrier user = new SmartCarrier("Palo Alto");
      user.init();
      user.run();
   }
}

class SmartCarrier {

   private TreeMap<String, ArrayList<Item>> messageMap;
   private String location;
   private static String INPUT_FILE_LOCATION = "src/messages.txt";

   public SmartCarrier() {
      messageMap = new TreeMap<String, ArrayList<Item>>();
      location = "Location not specified";
   }

   public SmartCarrier(String location) {
      messageMap = new TreeMap<String, ArrayList<Item>>();
      this.location = location;
   }

   public void init() {

      String line = null;
      String key = null;
      ArrayList<Item> account = null;
      BufferedReader reader = null;
      Path inputFilePath = Paths.get(INPUT_FILE_LOCATION);

      final String TEXT_MSG_ID = "T";
      final String MEDIA_MSG_ID = "M";
      final String VOICE_MSG_ID = "V";

      try {

         reader = Files.newBufferedReader(inputFilePath,
               StandardCharsets.US_ASCII);
         Item messageItem = null;

         while ((line = reader.readLine()) != null) {

            String[] parts = line.split(",");
            String messageType = parts[0].toUpperCase();

            switch (messageType) {
            case TEXT_MSG_ID: {
               String content = parts[4];
               Text text = new Text(content);
               int time = Integer.parseInt(parts[1]);
               String sender = parts[2];
               String receiver = parts[3];
               double charge = Double.parseDouble(parts[5]);

               Message<Text> textMessage = new Message<Text>(time, sender,
                     receiver, charge, text);
               key = sender;
               messageItem = textMessage;
               break;
            }
            case MEDIA_MSG_ID: {
               double size = Double.parseDouble(parts[4]);
               String format = parts[5];
               Media media = new Media(size, format);
               int time = Integer.parseInt(parts[1]);
               String sender = parts[2];
               String receiver = parts[3];
               double charge = Double.parseDouble(parts[6]);

               Message<Media> mediaMessage = new Message<Media>(time, sender,
                     receiver, charge, media);
               key = sender;
               messageItem = mediaMessage;
               break;
            }
            case VOICE_MSG_ID:
               int duration = Integer.parseInt(parts[4]);
               String format = parts[5];
               Voice voice = new Voice(duration, format);
               int time = Integer.parseInt(parts[1]);
               String sender = parts[2];
               String receiver = parts[3];
               double charge = Double.parseDouble(parts[6]);

               Message<Voice> voiceMessage = new Message<Voice>(time, sender,
                     receiver, charge, voice);
               key = sender;
               messageItem = voiceMessage;
               break;
            }

            if (messageMap.containsKey(key)) {
               account = messageMap.get(key);
               account.add(messageItem);
            }

            else if (!messageMap.containsKey(key)) {
               ArrayList<Item> newEntry = new ArrayList<Item>();
               newEntry.add(messageItem);
               messageMap.put(key, newEntry);
            }
         }
      }
      catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void run() {

      int userInput = 0;
      BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));

      final int LIST_ALL_ACCOUNTS = 1;
      final int ERASE_FIRST_MEDIA = 2;
      final int DISCONNECT_ACCOUNT = 3;
      final int QUIT = 4;

      do {
         System.out.printf("%n%40s%n %42s%n %-20s%n %-34s%n %-21s%n %-7s%n",
               "FOOTHILL WIRELESS at " + location,
               "MESSAGE UTILIZATION AND ACCOUNT ADMIN", "1. List all accounts",
               "2. Erase the first media message", "3. Disconnect account",
               "4. Quit");
         try {
            userInput = Integer.parseInt(reader.readLine());
            switch (userInput) {
            case LIST_ALL_ACCOUNTS:
               this.listAllAccounts();
               break;
            case ERASE_FIRST_MEDIA:
               this.eraseFirstMedia();
               break;
            case DISCONNECT_ACCOUNT:
               this.disconnectAccount();
               break;
            }
         }
         catch (IOException e) {
            e.printStackTrace();
         }
      }
      while (userInput != QUIT);

      System.exit(0);
   }

   private void listAllAccounts() {

      ArrayList<Item> account = null;
      double totalCharges = 0.0;

      System.out.printf("%40s%n", "LIST OF ALL ACCOUNTS");
      System.out.println("--------------------------------------------------");

      for (Map.Entry<String, ArrayList<Item>> entry : messageMap.entrySet()) {
         account = entry.getValue();
         ListIterator<Item> iter = account.listIterator();
         Item element = null;

         System.out.println("Account: " + entry.getKey());
         while (iter.hasNext()) {
            element = iter.next();
            totalCharges += element.getCharge();
            System.out.print(element.toString());
         }
         System.out.printf("%-14s %.2f%n", "Total charges: ", totalCharges);
         System.out.println(
               "------------------------------------------------" + "--");
         totalCharges = 0;
      }
   }

   private void eraseFirstMedia() {
      for (Map.Entry<String, ArrayList<Item>> entry : messageMap.entrySet()) {
         eraseHelper(entry.getValue());
      }
   }

   private static void eraseHelper(List<? extends Item> list) {
      for (Item element : list) {
         if (element instanceof Message<?>) {
            if (((Message<?>) element).getFormat() instanceof Media) {
               list.remove(element);
               break;
            }
         }
      }
   }

   private void disconnectAccount() throws IOException {

      String userInput = null;
      double totalCharges = 0;
      ArrayList<Item> account = null;
      BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));
      try {
         System.out.println("Enter the phone number of the account"
               + " you would like to disconnect: ");
         userInput = reader.readLine();

         if (!messageMap.containsKey(userInput)) {
            throw new InvalidAccountException(userInput);
         }

         else if (messageMap.containsKey(userInput)) {
            account = messageMap.get(userInput);
            ListIterator<Item> iter = account.listIterator();

            while (iter.hasNext()) {
               Item element = iter.next();
               totalCharges += element.getCharge();
            }

            System.out.print("Total charges for account " + userInput + ": ");
            System.out.printf("%.2f%n", totalCharges);

            messageMap.remove(userInput);
         }
      }

      catch (InvalidAccountException e) {
         System.out
               .println("Account " + e.getPhoneNumber() + " does not exist!");
      }
   }

}

class InvalidAccountException extends Throwable {

   private String phoneNumber;

   public InvalidAccountException() {
      phoneNumber = "-----------";
   }

   public InvalidAccountException(String phoneNumber) {
      this.phoneNumber = phoneNumber;
   }

   public void setPhoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
   }

   public String getPhoneNumber() {
      return phoneNumber;
   }
}

/* ----------- Sample run
 
          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
1
                    LIST OF ALL ACCOUNTS
--------------------------------------------------
Account: 1-408-111-0222
   TEXT: Are you going to the movie tonight?      Time:144840960, From:1-408-111-0222, To:1-650-111-0000,
   VOICE: Duration: 120(sec), Format:MPE          Time:144840960, From:1-408-111-0222, To:1-650-222-5555,
   TEXT: Mom said you go home by 11pm             Time:144840960, From:1-408-111-0222, To:1-650-666-9999,
   VOICE: Duration: 231(sec), Format:MP4          Time:144840960, From:1-408-111-0222, To:1-650-852-4774,
   MEDIA: Size: 2.75 MB, Format: GIF              Time:144840960, From:1-408-111-0222, To:1-650-217-2003,
   MEDIA: Size: 1.75 MB, Format: GIF              Time:144840960, From:1-408-111-0222, To:1-650-333-8888,
Total charges:  9.20
--------------------------------------------------
Account: 1-408-222-0222
   TEXT: Happy birhday!!!                         Time:144840960, From:1-408-222-0222, To:1-650-333-6666,
   VOICE: Duration: 670(sec), Format:MOV          Time:144840960, From:1-408-222-0222, To:1-650-812-0011,
Total charges:  7.00
--------------------------------------------------
Account: 1-408-333-0222
   MEDIA: Size: 2.5 MB, Format: JPEG              Time:144840960, From:1-408-333-0222, To:1-650-123-2000,
   TEXT: Java is fun to learn right               Time:144840960, From:1-408-333-0222, To:1-650-213-4444,
   MEDIA: Size: 5.5 MB, Format: GIF               Time:144840960, From:1-408-333-0222, To:1-650-444-6666,
Total charges:  5.75
--------------------------------------------------
Account: 1-408-444-0222
   MEDIA: Size: 3.8 MB, Format: JPG               Time:144840960, From:1-408-444-0222, To:1-650-567-2003,
Total charges:  0.90
--------------------------------------------------
Account: 1-408-555-0222
   TEXT: Can you close the backdoor?              Time:144840960, From:1-408-555-0222, To:1-650-321-3131,
   TEXT: I got to go now Sorry                    Time:144840960, From:1-408-555-0222, To:1-650-991-7801,
   MEDIA: Size: 3.05 MB, Format: PNG              Time:144840960, From:1-408-555-0222, To:1-650-287-2203,
   TEXT: Will come to your house this afternoon   Time:144840960, From:1-408-555-0222, To:1-650-991-7801,
   TEXT: Yeah.                                    Time:144840960, From:1-408-555-0222, To:1-650-991-7801,
   VOICE: Duration: 172(sec), Format:MOV          Time:144840960, From:1-408-555-0222, To:1-650-000-2828,
Total charges:  7.55
--------------------------------------------------
Account: 1-408-666-0333
   TEXT: I'm tired today.                         Time:144840960, From:1-408-666-0333, To:1-650-555-4444,
   MEDIA: Size: 3.5 MB, Format: GIF               Time:144840960, From:1-408-666-0333, To:1-650-123-2000,
Total charges:  5.70
--------------------------------------------------

          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
2

          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
1
                    LIST OF ALL ACCOUNTS
--------------------------------------------------
Account: 1-408-111-0222
   TEXT: Are you going to the movie tonight?      Time:144840960, From:1-408-111-0222, To:1-650-111-0000,
   VOICE: Duration: 120(sec), Format:MPE          Time:144840960, From:1-408-111-0222, To:1-650-222-5555,
   TEXT: Mom said you go home by 11pm             Time:144840960, From:1-408-111-0222, To:1-650-666-9999,
   VOICE: Duration: 231(sec), Format:MP4          Time:144840960, From:1-408-111-0222, To:1-650-852-4774,
   MEDIA: Size: 1.75 MB, Format: GIF              Time:144840960, From:1-408-111-0222, To:1-650-333-8888,
Total charges:  8.00
--------------------------------------------------
Account: 1-408-222-0222
   TEXT: Happy birhday!!!                         Time:144840960, From:1-408-222-0222, To:1-650-333-6666,
   VOICE: Duration: 670(sec), Format:MOV          Time:144840960, From:1-408-222-0222, To:1-650-812-0011,
Total charges:  7.00
--------------------------------------------------
Account: 1-408-333-0222
   TEXT: Java is fun to learn right               Time:144840960, From:1-408-333-0222, To:1-650-213-4444,
   MEDIA: Size: 5.5 MB, Format: GIF               Time:144840960, From:1-408-333-0222, To:1-650-444-6666,
Total charges:  4.50
--------------------------------------------------
Account: 1-408-444-0222
Total charges:  0.00
--------------------------------------------------
Account: 1-408-555-0222
   TEXT: Can you close the backdoor?              Time:144840960, From:1-408-555-0222, To:1-650-321-3131,
   TEXT: I got to go now Sorry                    Time:144840960, From:1-408-555-0222, To:1-650-991-7801,
   TEXT: Will come to your house this afternoon   Time:144840960, From:1-408-555-0222, To:1-650-991-7801,
   TEXT: Yeah.                                    Time:144840960, From:1-408-555-0222, To:1-650-991-7801,
   VOICE: Duration: 172(sec), Format:MOV          Time:144840960, From:1-408-555-0222, To:1-650-000-2828,
Total charges:  4.35
--------------------------------------------------
Account: 1-408-666-0333
   TEXT: I'm tired today.                         Time:144840960, From:1-408-666-0333, To:1-650-555-4444,
Total charges:  0.45
--------------------------------------------------

          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
2

          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
3
Enter the phone number of the account you would like to disconnect: 
1-408-555-0222
Total charges for account 1-408-555-0222: 4.35

          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
1
                    LIST OF ALL ACCOUNTS
--------------------------------------------------
Account: 1-408-111-0222
   TEXT: Are you going to the movie tonight?      Time:144840960, From:1-408-111-0222, To:1-650-111-0000,
   VOICE: Duration: 120(sec), Format:MPE          Time:144840960, From:1-408-111-0222, To:1-650-222-5555,
   TEXT: Mom said you go home by 11pm             Time:144840960, From:1-408-111-0222, To:1-650-666-9999,
   VOICE: Duration: 231(sec), Format:MP4          Time:144840960, From:1-408-111-0222, To:1-650-852-4774,
Total charges:  4.20
--------------------------------------------------
Account: 1-408-222-0222
   TEXT: Happy birhday!!!                         Time:144840960, From:1-408-222-0222, To:1-650-333-6666,
   VOICE: Duration: 670(sec), Format:MOV          Time:144840960, From:1-408-222-0222, To:1-650-812-0011,
Total charges:  7.00
--------------------------------------------------
Account: 1-408-333-0222
   TEXT: Java is fun to learn right               Time:144840960, From:1-408-333-0222, To:1-650-213-4444,
Total charges:  0.25
--------------------------------------------------
Account: 1-408-444-0222
Total charges:  0.00
--------------------------------------------------
Account: 1-408-666-0333
   TEXT: I'm tired today.                         Time:144840960, From:1-408-666-0333, To:1-650-555-4444,
Total charges:  0.45
--------------------------------------------------

          FOOTHILL WIRELESS at Palo Alto
      MESSAGE UTILIZATION AND ACCOUNT ADMIN
 1. List all accounts
 2. Erase the first media message  
 3. Disconnect account
 4. Quit
4
--------------------*/
