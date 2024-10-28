package org.lolwat;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;

public class MuleUtil {
   public static String muleUsername;
   public static Integer muleWorld;
   public static Integer mulePort;
   public static boolean muleRequestInProgress = false;
   public static MulePurpose mulePurpose;
   public static Integer goldToKeep;
   public static Integer goldToMuleAt;
   public static MuleState muleState;
   public static boolean skipNextBankCycle;
   public static long muleStartTime;

   public static void hopToMulesWorld() {
      Logger.log("Hopping to mules world - " + muleWorld);
      if (!WorldHopper.hopWorld(muleWorld)) {
         Logger.log("Failed to hop to mules world");
      }

   }

   public static void makeMuleRequest(int requestedGold) {
      if (requestedGold == 0) {
         setMulePurpose(MulePurpose.GIVING_GOLD);
         setMuleState(MuleState.GETTING_GOLD);
      } else {
         setMulePurpose(MulePurpose.TAKING_GOLD);
         setMuleState(MuleState.MULING);
      }

      String hostname = "127.0.0.1";

      try {
         Socket socket = new Socket(hostname, getMulePort());
         Throwable var3 = null;

         try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(Players.getLocal().getName() + ":" + (getMulePurpose().equals(MulePurpose.TAKING_GOLD) ? "take" : "give") + ":" + requestedGold);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            Logger.log(line);
            setMuleWorld(Integer.parseInt(line.split(":")[1]));
            setMuleUsername(line.split(":")[0]);
            setMuleRequestInProgress(true);
            WatScript.currentState = WatScript.State.MULING;
            setMuleStartTime(Instant.now().getEpochSecond());
            writer.println("bye");
         } catch (Throwable var18) {
            var3 = var18;
            throw var18;
         } finally {
            if (socket != null) {
               if (var3 != null) {
                  try {
                     socket.close();
                  } catch (Throwable var17) {
                     var3.addSuppressed(var17);
                  }
               } else {
                  socket.close();
               }
            }

         }
      } catch (UnknownHostException var20) {
         Logger.log(var20);
         Logger.log("Failed to connect to mule - UnknownHost");
         setMuleRequestInProgress(false);
      } catch (IOException var21) {
         Logger.log(var21);
         Logger.log("Failed to connect to mule - IO");
         setMuleRequestInProgress(false);
      }

   }

   public static void setMuleUsername(String muleUsername) {
      MuleUtil.muleUsername = muleUsername;
   }

   public static String getMuleUsername() {
      return muleUsername;
   }

   public static void setMuleWorld(Integer muleWorld) {
      MuleUtil.muleWorld = muleWorld;
   }

   public static Integer getMuleWorld() {
      return muleWorld;
   }

   public static void setMulePort(Integer mulePort) {
      MuleUtil.mulePort = mulePort;
   }

   public static Integer getMulePort() {
      return mulePort;
   }

   public static void setMuleRequestInProgress(boolean muleRequestInProgress) {
      MuleUtil.muleRequestInProgress = muleRequestInProgress;
   }

   public static boolean isMuleRequestInProgress() {
      return muleRequestInProgress;
   }

   public static void setMulePurpose(MulePurpose mulePurpose) {
      MuleUtil.mulePurpose = mulePurpose;
   }

   public static MulePurpose getMulePurpose() {
      return mulePurpose;
   }

   public static void setGoldToKeep(Integer goldToKeep) {
      MuleUtil.goldToKeep = goldToKeep;
   }

   public static Integer getGoldToKeep() {
      return goldToKeep;
   }

   public static void setGoldToMuleAt(Integer goldToMuleAt) {
      MuleUtil.goldToMuleAt = goldToMuleAt;
   }

   public static Integer getGoldToMuleAt() {
      return goldToMuleAt;
   }

   public static void setMuleState(MuleState muleState) {
      MuleUtil.muleState = muleState;
   }

   public static MuleState getMuleState() {
      return muleState;
   }

   public static void setSkipNextBankCycle(boolean skipNextBankCycle) {
      MuleUtil.skipNextBankCycle = skipNextBankCycle;
   }

   public static boolean isSkipNextBankCycle() {
      return skipNextBankCycle;
   }

   public static void setMuleStartTime(long muleStartTime) {
      MuleUtil.muleStartTime = muleStartTime;
   }

   public static long getMuleStartTime() {
      return muleStartTime;
   }

   public static enum MuleState {
      GETTING_GOLD,
      MULING;
   }

   public static enum MulePurpose {
      GIVING_GOLD,
      TAKING_GOLD;
   }
}
