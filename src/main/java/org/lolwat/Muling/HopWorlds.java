package org.lolwat.Muling;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.MuleUtil;

public class HopWorlds extends Leaf {
   public boolean isValid() {
      return BankLocation.GRAND_EXCHANGE.getArea(15).contains(Players.getLocal()) && Client.getGameState().equals(GameState.LOGGED_IN) && Worlds.getCurrentWorld() != MuleUtil.getMuleWorld() && MuleUtil.isMuleRequestInProgress();
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

      Logger.log("Hopping to mules world");
      MuleUtil.hopToMulesWorld();
      return (int)Calculations.nextGaussianRandom(350.0D, 250.0D);
   }
}
