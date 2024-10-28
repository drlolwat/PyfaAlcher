package org.lolwat.Muling;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.lolwat.MuleUtil;

public class TradeMule extends Leaf {
   public boolean isValid() {
      return BankLocation.GRAND_EXCHANGE.getArea(15).contains(Players.getLocal()) && MuleUtil.isMuleRequestInProgress() && Players.closest(new String[]{MuleUtil.getMuleUsername()}) != null && !Trade.isOpen();
   }

   public int onLoop() {
      if (Bank.isOpen()) {
         Bank.close();
         Logger.log("Closing Bank");
         Sleep.sleepUntil(() -> {
            return !Bank.isOpen();
         }, 5000L, 100L);
      }

      if (GrandExchange.isOpen()) {
         GrandExchange.close();
         Logger.log("Closing Grand Exchange");
         Sleep.sleepUntil(() -> {
            return !GrandExchange.isOpen();
         }, 5000L, 100L);
      }

      Player mule = Players.closest(new String[]{MuleUtil.getMuleUsername()});
      if (mule != null) {
         Logger.log("Trading mule");
         Trade.tradeWithPlayer(MuleUtil.getMuleUsername());
         Sleep.sleepUntil(Trade::isOpen, 15000L, 500L);
         if (Trade.isOpen()) {
            Logger.log("Traded mule successfully");
         }
      }

      return (int)Calculations.nextGaussianRandom(350.0D, 250.0D);
   }
}
