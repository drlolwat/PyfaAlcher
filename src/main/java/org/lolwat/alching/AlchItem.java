package org.lolwat.alching;

import org.dreambot.api.Client;
import org.dreambot.api.ClientSettings;
import org.dreambot.api.data.GameState;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.InteractionEvent;
import org.dreambot.api.input.mouse.destination.impl.PointDestination;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatScript;

import java.util.Iterator;
import java.util.List;

public class AlchItem extends Leaf {
   public boolean isValid() {
      return Inventory.getEmptySlots() != 26;
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

      if (ClientSettings.isTradeDelayEnabled()) {
         ClientSettings.toggleTradeDelay(false);
      }

      if (PlayerSettings.getBitValue(6091) < 100000) {
         if (GrandExchange.isOpen()) {
            GrandExchange.close();
         }

         if (Bank.isOpen()) {
            Bank.close();
         }

         if (PlayerSettings.getBitValue(13131) != 1 || PlayerSettings.getBitValue(4679) == 0 || PlayerSettings.getBitValue(4678) == 0 || PlayerSettings.getBitValue(4680) == 0 || PlayerSettings.getBitValue(14803) == 0 || PlayerSettings.getBitValue(10532) == 0) {
            if (!Tabs.isOpen(Tab.OPTIONS)) {
               Logger.log("Opening options");
               Tabs.open(Tab.OPTIONS);
            }

            WidgetChild allSettings;
            if (PlayerSettings.getBitValue(9683) != 0) {
               Logger.log("Opening Control Settings");
               allSettings = Widgets.get((widgetChild) -> {
                  return widgetChild.hasAction("Controls");
               });
               if (allSettings != null && allSettings.isVisible()) {
                  allSettings.interact();
                  Sleep.sleepUntil(() -> {
                     return PlayerSettings.getBitValue(9683) == 0;
                  }, 3000L, 500L);
               }
            }

            allSettings = Widgets.get((widgetChild) -> {
               return widgetChild.getText().equals("All Settings");
            });
            if (allSettings != null && allSettings.isVisible()) {
               Logger.log("Opening Settings");
               allSettings.interact();
               Sleep.sleepUntil(() -> {
                  return Widgets.getWidget(134) != null;
               }, 3000L, 500L);
            }

            Logger.log("Opening Warnings");
            Widgets.getWidget(134).getChild(24).getChild(79).interact();
            Sleep.sleepUntil(() -> {
               return Widgets.getWidgetChildrenContainingText("Minimum item value needed for Alchemy spells warning").size() > 0;
            }, 5000L, 100L);
            List<WidgetChild> warningWidgets = Widgets.getWidgetChildrenContainingText("Minimum item value needed for Alchemy spells warning");
            if (warningWidgets != null) {
               Iterator var3 = warningWidgets.iterator();

               while(var3.hasNext()) {
                  WidgetChild widget = (WidgetChild)var3.next();
                  if (widget.getText().equals("Minimum item value needed for Alchemy spells warning")) {
                     Logger.log("Setting high alch warning value");
                     Mouse.move(Widgets.getWidget(134).getChild(14).getChild(1).getRectangle());
                     Mouse.scrollDownUntil(5000, () -> {
                        return Widgets.getWidget(134).getChild(13).getRectangle().contains(widget.getRectangle());
                     });
                     Widgets.get(new int[]{widget.getParentID(), widget.getID(), widget.getIndex() + 4}).interact();
                     Sleep.sleepUntil(() -> {
                        return Dialogues.canEnterInput();
                     }, 5000L, 1000L);
                     Keyboard.type("200000", true);
                  }
               }
            }
         }

         ClientSettings.closeSettingsInterface();
      }

      if (GrandExchange.isOpen()) {
         GrandExchange.close();
         Sleep.sleepUntil(() -> {
            return !GrandExchange.isOpen();
         }, 2000L, 100L);
      }

      if (!GrandExchange.isOpen()) {
         if (WatScript.itemToAlch != null && Inventory.count(WatScript.itemToAlch.getName()) != 0) {
            WatScript.itemToAlch = Inventory.get(WatScript.itemToAlch.getName());
         } else {
            WatScript.itemToAlch = Inventory.get((item) -> {
               return !item.getName().equals("Coins") && !item.getName().equals("Nature rune");
            });
         }

         if (Inventory.get(WatScript.itemToAlch.getName()).getSlot() != 15) {
            Inventory.drag(Inventory.get(WatScript.itemToAlch.getName()), 15);
            Sleep.sleepUntil(() -> {
               return Inventory.get(WatScript.itemToAlch.getName()).getSlot() == 15;
            }, 2000L, 100L);
            Sleep.sleep(500L, 1200L);
            WatScript.itemToAlch = Inventory.get(WatScript.itemToAlch.getName());
         }

         Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY);
         Sleep.sleepUntil(() -> {
            return Tabs.isOpen(Tab.INVENTORY);
         }, 5000L, 100L);
         if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleepUntil(() -> {
               return Tabs.isOpen(Tab.INVENTORY);
            }, 5000L, 100L);
         }

         InteractionEvent event;
         if (WatScript.itemToAlch != null) {
            event = new InteractionEvent(new PointDestination(WatScript.itemToAlch.getDestination().getSuitablePoint()));
            event.interact("Cast", false, false);
         }

         Sleep.sleepUntil(() -> {
            return Tabs.isOpen(Tab.MAGIC);
         }, 5000L, 100L);
         Sleep.sleep(600L, 1200L);
         if (Tabs.isOpen(Tab.MAGIC)) {
            Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY);
            Sleep.sleepUntil(() -> {
               return Tabs.isOpen(Tab.INVENTORY);
            }, 5000L, 100L);
            if (!Tabs.isOpen(Tab.INVENTORY)) {
               Tabs.open(Tab.INVENTORY);
               Sleep.sleepUntil(() -> {
                  return Tabs.isOpen(Tab.INVENTORY);
               }, 5000L, 100L);
            }

            if (WatScript.itemToAlch != null) {
               event = new InteractionEvent(new PointDestination(WatScript.itemToAlch.getDestination().getSuitablePoint()));
               event.interact("Cast", false, false);
            }
         }

         Sleep.sleepUntil(() -> {
            return Players.getLocal().getAnimation() == -1;
         }, 5000L, 100L);
         Sleep.sleep(600L, 1200L);
      }

      return 0;
   }
}
