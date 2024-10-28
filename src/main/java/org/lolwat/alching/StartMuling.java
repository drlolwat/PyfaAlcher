package org.lolwat.alching;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.lolwat.MuleUtil;
import org.lolwat.Settings;

public class StartMuling extends Leaf {
   public boolean isValid() {
      return Settings.isMulingEnabled() && MuleUtil.getGoldToMuleAt() <= Inventory.count("Coins");
   }

   public int onLoop() {
      MuleUtil.makeMuleRequest(0);
      return 2000;
   }
}
