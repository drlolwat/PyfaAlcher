package org.lolwat.alching;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.MuleUtil;
import org.lolwat.Settings;
import org.lolwat.WatScript;

import java.time.Instant;

public class BuyItem extends Leaf {
   public boolean isValid() {
      return Inventory.getEmptySlots() == 26 || !Inventory.contains("Nature rune") || !Equipment.contains("Staff of fire");
   }

   public int onLoop() {
      WatScript.members = Client.isMembers();
      if (Client.getMembershipLeft() >= 1 && Worlds.getCurrent().isF2P()) {
         if (GrandExchange.isOpen()) {
            GrandExchange.close();
            Sleep.sleepUntil(() -> {
               return !GrandExchange.isOpen();
            }, 5000L, 10L);
         }

         if (Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> {
               return !Bank.isOpen();
            }, 5000L, 10L);
         }

         WorldHopper.hopWorld(Worlds.getRandomWorld((world) -> {
            return world.isNormal() && world.isMembers() && world.getMinimumLevel() < 1000 && world.getPopulation() > 50;
         }));
         Sleep.sleepTicks(5);
         Sleep.sleepUntil(() -> {
            return !Worlds.getCurrent().isF2P() && Client.getGameState().equals(GameState.LOGGED_IN);
         }, 60000L, 10L);
      }

      if (!GrandExchange.isOpen()) {
         GrandExchange.open();
         Sleep.sleepUntil(() -> {
            return GrandExchange.isOpen();
         }, 2000L, 100L);
      }

      if (GrandExchange.isOpen()) {
         if (Inventory.count("Coins") < 50000 && GrandExchange.getUsedSlots() == 0 && Settings.isMulingEnabled()) {
            MuleUtil.makeMuleRequest(MuleUtil.getGoldToKeep());
            return 1000;
         }

         int goldBefore;
         int purchasePrice;
         if (!Inventory.contains("Nature rune")) {
            goldBefore = Inventory.count("Coins");
            GrandExchange.buyItem("Nature rune", Math.min(1000, Inventory.count("Coins") / (int)((double)LivePrices.get("Nature rune") * 1.25D)), (int)((double)LivePrices.get("Nature rune") * 1.25D));
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 10000L, 1000L);
            if (GrandExchange.isReadyToCollect()) {
               GrandExchange.collect();
               Sleep.sleepTicks(2);
               if (Inventory.contains("Nature rune")) {
                  int currentGold = Inventory.count("Coins");
                  int itemCount = Inventory.count("Nature rune");
                  goldBefore = goldBefore - currentGold;
                  purchasePrice = goldBefore / itemCount;
                  WatScript.natureRunePrice = purchasePrice;
                  Logger.log("Bought " + itemCount + " Nature Runes for " + goldBefore + " gold. Price per item: " + purchasePrice);
               }

               return 1000;
            }

            GrandExchange.collect();
         }

         if (!Equipment.contains("Staff of fire")) {
            GrandExchange.buyItem("Staff of fire", 1, LivePrices.get("Staff of fire") * 2);
            Sleep.sleepUntil(() -> {
               return GrandExchange.isReadyToCollect();
            }, 10000L, 1000L);
            if (GrandExchange.isReadyToCollect()) {
               GrandExchange.collect();
               Sleep.sleepUntil(() -> {
                  return Inventory.contains("Staff of fire");
               }, 5000L, 100L);
               if (Inventory.contains("Staff of fire")) {
                  GrandExchange.close();
                  Sleep.sleepUntil(() -> {
                     return !GrandExchange.isOpen();
                  }, 5000L, 100L);
                  Sleep.sleepTicks(5);
                  Inventory.get("Staff of fire").interact("Wield");
               }

               return 1000;
            }

            GrandExchange.collect();
         }

         long purchaseTime = Instant.now().getEpochSecond();
         Item itemToBuy = WatScript.findProfitableItem();
         if (itemToBuy == null) {
            Logger.log("No Item to buy");
            return 0;
         }

         goldBefore = Inventory.count("Coins");
         purchasePrice = itemToBuy.getHighAlchValue() - WatScript.natureRunePrice - (WatScript.members ? 150 : 50);
         int numberToBuy = Inventory.count("Coins") / purchasePrice;
         int availableBuyLimit = WatScript.howManyCanIBuy(itemToBuy);
         int quantityToBuy = Math.min(1000, Math.min(availableBuyLimit, numberToBuy));
         GrandExchange.buyItem(itemToBuy.getID(), quantityToBuy, purchasePrice);
         Sleep.sleepUntil(() -> {
            return GrandExchange.isReadyToCollect();
         }, 10000L, 1000L);
         int currentGold;
         int itemCount;
         int goldSpent;
         int pricePerItem;
         if (GrandExchange.isReadyToCollect()) {
            WatScript.madePurchase(itemToBuy, numberToBuy, purchaseTime);
            GrandExchange.collect();
            Sleep.sleepUntil(() -> {
               return Inventory.contains(itemToBuy.getName());
            }, 5000L, 100L);
            Sleep.sleepTicks(1);
            if (Inventory.contains(itemToBuy.getName())) {
               currentGold = Inventory.count("Coins");
               itemCount = Inventory.count(itemToBuy.getName());
               goldSpent = goldBefore - currentGold;
               pricePerItem = goldSpent / itemCount;
               WatScript.buyPrice.put(itemToBuy.getID(), pricePerItem);
               WatScript.buyPrice.put(itemToBuy.getNotedItemID(), pricePerItem);
               Logger.log("Bought " + itemCount + " " + itemToBuy.getName() + " for " + goldSpent + " gold. Price per item: " + pricePerItem);
               WatScript.totalLootValue += itemCount * itemToBuy.getHighAlchValue();
               WatScript.totalLootValue -= itemCount * WatScript.natureRunePrice;
               WatScript.totalLootValue -= itemCount * pricePerItem;
            }
         } else {
            WatScript.madePurchase(itemToBuy, (Integer)WatScript.buyLimits.get(itemToBuy.getID()), purchaseTime - 12600L);
            GrandExchange.cancelAll();
            Sleep.sleepUntil(() -> {
               return GrandExchange.isReadyToCollect();
            }, 10000L, 1000L);
            if (GrandExchange.isReadyToCollect()) {
               GrandExchange.collect();
            }

            Sleep.sleepUntil(() -> {
               return Inventory.contains(itemToBuy.getName());
            }, 5000L, 100L);
            Sleep.sleepTicks(1);
            if (Inventory.contains(itemToBuy.getName())) {
               currentGold = Inventory.count("Coins");
               itemCount = Inventory.count(itemToBuy.getName());
               goldSpent = goldBefore - currentGold;
               pricePerItem = goldSpent / itemCount;
               WatScript.buyPrice.put(itemToBuy.getID(), pricePerItem);
               WatScript.buyPrice.put(itemToBuy.getNotedItemID(), pricePerItem);
               Logger.log("Bought " + itemCount + " " + itemToBuy.getName() + " for " + goldSpent + " gold. Price per item: " + pricePerItem);
               WatScript.totalLootValue += itemCount * itemToBuy.getHighAlchValue();
               WatScript.totalLootValue -= itemCount * WatScript.natureRunePrice;
               WatScript.totalLootValue -= itemCount * pricePerItem;
            }
         }

         if (GrandExchange.getUsedSlots() > 0) {
            GrandExchange.cancelAll();
            Sleep.sleepUntil(() -> {
               return GrandExchange.isReadyToCollect();
            }, 10000L, 1000L);
            if (GrandExchange.isReadyToCollect()) {
               GrandExchange.collect();
            }
         }
      }

      return 0;
   }
}
