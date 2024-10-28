package org.lolwat.Muling;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.utilities.Logger;
import org.lolwat.MuleUtil;
import org.lolwat.WatScript;

import java.time.Instant;

public class Timeout extends Leaf {
   public boolean isValid() {
      return Instant.now().getEpochSecond() - MuleUtil.getMuleStartTime() > 300L;
   }

   public int onLoop() {
      Logger.log("Mule timeout - reverting to selling");
      WatScript.currentState = WatScript.State.ALCHING;
      MuleUtil.setMuleRequestInProgress(false);
      return (int)Calculations.nextGaussianRandom(350.0D, 250.0D);
   }
}
