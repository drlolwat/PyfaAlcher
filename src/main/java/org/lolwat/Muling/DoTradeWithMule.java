package org.lolwat.Muling;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.MuleUtil;
import org.lolwat.WatScript;

public class DoTradeWithMule extends Leaf {
   public boolean isValid() {
      return BankLocation.GRAND_EXCHANGE.getArea(15).contains(Players.getLocal()) && MuleUtil.isMuleRequestInProgress() && Trade.isOpen();
   }

   public int onLoop() {
      if (!Inventory.contains("Coins") && MuleUtil.getMulePurpose().equals(MuleUtil.MulePurpose.GIVING_GOLD)) {
         Logger.log("No coins but wanting to trade mule - getting gold");
         MuleUtil.setMuleState(MuleUtil.MuleState.GETTING_GOLD);
         return 2000;
      } else {
         if (Trade.getTradingWith().equals(MuleUtil.getMuleUsername())) {
            if (MuleUtil.getMulePurpose().equals(MuleUtil.MulePurpose.GIVING_GOLD) && Trade.isOpen(1)) {
               Logger.log("Adding coins - giving");
               Trade.addItem("Coins", Inventory.count("Coins") - MuleUtil.getGoldToKeep());
               Sleep.sleepUntil(() -> {
                  return Trade.getMyItems() != null && Trade.getMyItems().length > 0;
               }, 10000L, 100L);
               if (Trade.getMyItems() != null && Trade.getMyItems().length > 0) {
                  Logger.log("Accepting trade - giving");
                  Trade.acceptTrade();
                  Sleep.sleepUntil(() -> {
                     return Trade.isOpen(2);
                  }, 5000L, 500L);
               } else {
                  Logger.log("Failed to add coins - giving");
               }
            }

            if (MuleUtil.getMulePurpose().equals(MuleUtil.MulePurpose.TAKING_GOLD) && Trade.isOpen(1) && Trade.getTheirItems() != null && Trade.getTheirItems().length > 0) {
               Logger.log("Accepting trade - taking");
               Trade.acceptTrade();
               Sleep.sleepUntil(() -> {
                  return Trade.isOpen(2);
               }, 2000L, 500L);
            }

            if (Trade.isOpen(2)) {
               Logger.log("Accepting trade 2");
               Trade.acceptTrade();
               Sleep.sleepUntil(() -> {
                  return !Trade.isOpen();
               }, 5000L, 500L);
               if (!Trade.isOpen()) {
                  WatScript.members = Client.isMembers();
                  Logger.log("Finished muling");
                  if (!WatScript.members) {
                     WorldHopper.hopWorld(Worlds.getRandomWorld((world) -> {
                        return world.isNormal() && world.isF2P() && world.getMinimumLevel() < Skills.getTotalLevel();
                     }));
                  } else {
                     WorldHopper.hopWorld(Worlds.getRandomWorld((world) -> {
                        return world.isNormal() && world.isMembers() && world.getMinimumLevel() < Skills.getTotalLevel();
                     }));
                  }

                  WatScript.currentState = WatScript.State.ALCHING;
                  MuleUtil.setMuleRequestInProgress(false);
               }
            }
         } else {
            Trade.close();
         }

         return (int)Calculations.nextGaussianRandom(350.0D, 250.0D);
      }
   }
}
