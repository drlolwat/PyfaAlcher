package org.lolwat;

import com.google.gson.*;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.frameworks.treebranch.Branch;
import org.dreambot.api.script.frameworks.treebranch.Leaf;
import org.dreambot.api.script.frameworks.treebranch.TreeScript;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.Muling.DoTradeWithMule;
import org.lolwat.Muling.HopWorlds;
import org.lolwat.Muling.Timeout;
import org.lolwat.Muling.TradeMule;
import org.lolwat.alching.AlchItem;
import org.lolwat.alching.BuyItem;
import org.lolwat.alching.StartMuling;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@ScriptManifest(
        category = Category.MISC,
        author = "Pyfa",
        name = "Pyfa Alcher",
        description = "Alchs items for a profit",
        version = 0.2D
)
public class WatScript extends TreeScript implements ExperienceListener, ItemContainerListener {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    public static long timeSinceEat = Instant.now().getEpochSecond();
    public static JsonObject latestPrice = null;
    public static int natureRunePrice = LivePrices.get("Nature rune");
    public static int alchCount = 0;
    public static boolean members = false;
    public static JsonArray mapping = null;
    public static Timer timer = new Timer();
    public static Item itemToAlch = null;
    public static HashMap<Integer, Integer> buyLimits = new HashMap<>();
    public static HashMap<Integer, Integer> buyPrice = new HashMap<>();
    public static HashMap<Integer, Integer> profitableItems = new HashMap<>();
    public static HashMap<Integer, HashMap<Long, Integer>> purchases = new HashMap<>();
    public static WatScript.State currentState;
    public static String profileName;
    public static Font font;
    public static Color chatboxCoverColour;
    public static Branch alchingBranch;
    public static Branch buyingBranch;
    public static Branch mulingBranch;
    public static boolean launchedGUI;
    public static int totalLootValue;
    public static Long activityLastLogged;
    private final RenderingHints aa;
    private static final Gson gson = new Gson();

    public WatScript() {
        this.aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void getLatestPrices() throws IOException {
        latestPrice = getJson(new URL("https://prices.pyfa.dev/api/v1/prices/item_prices")).getAsJsonObject();
    }

    public static void getMapping() throws IOException {
        URLConnection connection = (new URL("https://cdn.pyfa.dev/mapping.json")).openConnection();
        String userAgent = "Price trends";
        connection.setRequestProperty("User-Agent", userAgent);
        InputStream inputStream = connection.getInputStream();
        String json = readFromInputStream(inputStream);
        inputStream.close();

        mapping = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement element : mapping) {
            JsonObject item = element.getAsJsonObject();
            if (item.has("limit") && !item.get("limit").isJsonNull()) {
                Integer itemID = item.get("id").getAsInt();
                Integer buyLimit = item.get("limit").getAsInt();
                buyLimits.put(itemID, buyLimit);
            }
        }
    }

    public static Item findProfitableItem() {
        try {
            getLatestPrices();
        } catch (IOException var7) {
            Logger.log("Failed to get latest prices");
            return null;
        }

        profitableItems = new HashMap<>();
        Item mostProfitableItem = null;
        int profitFromItem = 0;
        JsonObject data = latestPrice.getAsJsonObject("data");

        for (String key : data.keySet()) {
            JsonObject item = data.getAsJsonObject(key);
            if (key != null && item.has("avgLowPrice") && !item.get("avgLowPrice").isJsonNull() &&
                    item.has("avgHighPrice") && !item.get("avgHighPrice").isJsonNull() &&
                    item.has("lowPriceVolume") && !item.get("lowPriceVolume").isJsonNull() &&
                    item.has("highPriceVolume") && !item.get("highPriceVolume").isJsonNull()) {

                Item runescapeItem = new Item(Integer.parseInt(key), 1);
                Logger.log(runescapeItem.getName());
                int highAlchProfit = runescapeItem.getHighAlchValue() - ((item.get("avgHighPrice").getAsInt() + item.get("avgLowPrice").getAsInt()) / 2 + natureRunePrice);

                if ((item.get("highPriceVolume").getAsInt() + item.get("lowPriceVolume").getAsInt()) / 2 > 30 && highAlchProfit > ConfigManager.getInstance().getConfigInt("min_profit") &&
                        (!runescapeItem.isMembersOnly() || (members && runescapeItem.isMembersOnly()))) {

                    if (howManyCanIBuy(runescapeItem) > 0) {
                        if (mostProfitableItem == null) {
                            mostProfitableItem = runescapeItem;
                            profitFromItem = highAlchProfit;
                        }

                        if (highAlchProfit > profitFromItem) {
                            profitFromItem = highAlchProfit;
                            mostProfitableItem = runescapeItem;
                        }

                        Logger.log(runescapeItem.getName() + " - " + highAlchProfit + " - Number I can buy: " + howManyCanIBuy(runescapeItem));
                        profitableItems.put(runescapeItem.getID(), highAlchProfit);
                    }
                }
            }
        }
        return mostProfitableItem;
    }

    public static int madePurchase(Item item, int amount, long purchaseTime) {
        if (!purchases.containsKey(item.getID())) {
            purchases.put(item.getID(), new HashMap<>());
        }

        purchases.get(item.getID()).put(purchaseTime, amount);
        return 0;
    }

    public static int howManyCanIBuy(Item item) {
        long currentTime = Instant.now().getEpochSecond();
        if (!purchases.containsKey(item.getID())) {
            return buyLimits.get(item.getID());
        } else {
            int amountBoughtInLimit = 0;
            for (Long timestamp : purchases.get(item.getID()).keySet()) {
                if (timestamp > currentTime - 14400L) {
                    amountBoughtInLimit += purchases.get(item.getID()).get(timestamp);
                }
            }
            return buyLimits.get(item.getID()) - amountBoughtInLimit;
        }
    }

    public static JsonObject getJson(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        String userAgent = "Price trends";
        connection.setRequestProperty("User-Agent", userAgent);
        InputStream inputStream = connection.getInputStream();
        String json = readFromInputStream(inputStream);
        inputStream.close();
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public void onInventoryItemChanged(Item incoming, Item existing) {
    }

    public void onStart(String... args) {
        profileName = args[0];

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().loadFromProfile(profileName);
        }
    }

    public void onStart() {
        profileName = "";

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().loadFromProfile("default");
        }
    }

    public void onExit() {
        if (Settings.getSettingsGUI() != null) {
            Settings.getSettingsGUI().dispose();
        }
    }

    public void onPaint(Graphics g) {
        Graphics2D gg;
        if (!launchedGUI && Client.getGameState().equals(GameState.LOGGED_IN) && Players.getLocal() != null && Skills.getTotalLevel() > 0) {
            launchedGUI = true;
            new MuleUtil();
            gg = (Graphics2D)Client.getCanvas().getGraphics();
            Font original = gg.getFont();
            Font bigger = original.deriveFont(16.0F);
            bigger = bigger.deriveFont(1);
            font = bigger;
            timer.start();
            if (profileName.equals("")) {
                new Settings();
            } else {
                new Settings(profileName);
            }

            SwingUtilities.invokeLater(Settings::createGUI);

            try {
                getMapping();
            } catch (IOException var11) {
                Logger.log("Failed to get mapping");
                return;
            }

            alchingBranch.addLeaves(new Leaf[]{new StartMuling(), new BuyItem(), new AlchItem()});
            mulingBranch.addLeaves(new Leaf[]{new Timeout(), new HopWorlds(), new TradeMule(), new DoTradeWithMule()});
            this.addBranches(new Branch[]{mulingBranch, alchingBranch});
            itemToAlch = new Item(1127, 1);
            currentState = WatScript.State.ALCHING;
            if (Worlds.getCurrent().isMembers()) {
                members = true;
            }
        }

        gg = (Graphics2D)g;
        gg.setRenderingHints(this.aa);
        gg.setFont(font);
        WidgetChild nameWidget = Widgets.getChildWidget(162, 55);
        if (nameWidget != null) {
            gg.setColor(Color.BLACK);
            gg.fillRect(nameWidget.getX(), nameWidget.getY(), nameWidget.getWidth(), nameWidget.getHeight());
        }

        WidgetChild chatWidget = Widgets.getChildWidget(162, 34);
        if (chatWidget != null) {
            long elapsedTime = timer.elapsed() / 1000L;
            gg.setColor(chatboxCoverColour);
            gg.fillRect(chatWidget.getX(), chatWidget.getY(), chatWidget.getWidth(), chatWidget.getHeight());
            gg.setColor(Color.WHITE);
            float hoursElapsed = (float)elapsedTime / 3600.0F;
            gg.drawString("Alchs: " + alchCount, chatWidget.getX() + 20, chatWidget.getY() + 25);
            gg.drawString("Alchs/h: " + (int)((float)alchCount / hoursElapsed), chatWidget.getX() + 150, chatWidget.getY() + 25);
            gg.drawString("GP: " + String.format("%.2f", (float)totalLootValue / 1000000.0F) + "M", chatWidget.getX() + 20, chatWidget.getY() + 55);
            gg.drawString("GP/h: " + String.format("%.2f", (float)totalLootValue / 1000000.0F / hoursElapsed) + "M", chatWidget.getX() + 150, chatWidget.getY() + 55);
            int hours = (int)(elapsedTime / 3600L);
            int minutes = (int)(elapsedTime % 3600L / 60L);
            int seconds = (int)(elapsedTime % 60L);
            gg.drawString(String.format("%02d:%02d:%02d", hours, minutes, seconds), chatWidget.getX() + 425, chatWidget.getY() + 25);
        }
    }

    public void onLevelUp(ExperienceEvent event) {
        this.sleep(5000L);
        List<Integer> levels = Arrays.asList(60, 70, 80, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99);
        if (Settings.getDiscordWebhookUrl() != null && Settings.getDiscordWebhookUrl().length() != 0 && event.getSkill().equals(Skill.MAGIC) && levels.contains(Skills.getRealLevel(event.getSkill()))) {
            DiscordWebhook webhook = new DiscordWebhook(Settings.getDiscordWebhookUrl());
            webhook.setUsername("Pyfa Alcher");
            DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
            embedObject.setColor(Color.BLUE);
            embedObject.setTitle("Magic Level");
            embedObject.setDescription(String.valueOf(Skills.getRealLevel(event.getSkill())));
            webhook.addEmbed(embedObject);

            try {
                webhook.execute();
            } catch (IOException var6) {
                throw new RuntimeException(var6);
            }
        }
    }

    public void onGained(ExperienceEvent event) {
        if (event.getSkill().equals(Skill.MAGIC)) {
            ++alchCount;
        }
    }

    static {
        currentState = WatScript.State.BUYING;
        profileName = "";
        chatboxCoverColour = new Color(0, 0, 0, 200);
        alchingBranch = new Branch() {
            public boolean isValid() {
                return WatScript.currentState.equals(WatScript.State.ALCHING);
            }
        };
        buyingBranch = new Branch() {
            public boolean isValid() {
                return WatScript.currentState.equals(WatScript.State.BUYING);
            }
        };
        mulingBranch = new Branch() {
            public boolean isValid() {
                return WatScript.currentState.equals(WatScript.State.MULING);
            }
        };
        launchedGUI = false;
        totalLootValue = 0;
        activityLastLogged = 0L;
    }

    public static enum State {
        ALCHING,
        MULING,
        BUYING;
    }
}