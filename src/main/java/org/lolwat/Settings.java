package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.utilities.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class Settings {
   public static JFrame settingsGUI;
   public static String discordWebhookUrl;
   public static boolean f2p;
   public static boolean mulingEnabled = false;
   public static boolean takingGoldFromMuleEnabled = false;
   public static String profileToLoad = "";
   static JComboBox profileName;
   static JPanel generalSettings;
   static JPanel mulePanel;
   private static String configFolder;
   private static boolean visible = true;
   private static HashMap<String, Component> components;

   public Settings() {
      components = new HashMap();
      configFolder = createFolder();
   }

   public Settings(String profileName) {
      components = new HashMap();
      configFolder = createFolder();
      visible = false;
      Logger.log("Should load " + profileName);
      profileToLoad = profileName;
   }

   public static void createGUI() {
      settingsGUI = new JFrame();
      settingsGUI.setTitle("Pyfa Alcher");
      settingsGUI.setDefaultCloseOperation(2);
      settingsGUI.setLocationRelativeTo(Client.getCanvas());
      settingsGUI.setPreferredSize(new Dimension(500, 400));
      settingsGUI.getContentPane().setLayout(new BorderLayout());
      generalSettings = new JPanel();
      mulePanel = new JPanel();
      JTabbedPane tabbedPane = new JTabbedPane(1);
      tabbedPane.addTab("General", generalSettings);
      tabbedPane.addTab("Mule", mulePanel);
      generalSettings.setLayout(new GridLayout(0, 2));
      mulePanel.setLayout(new GridLayout(0, 2));
      JLabel discordWebhookUrlLabel = new JLabel("Discord Webhook URL", 0);
      generalSettings.add(discordWebhookUrlLabel);
      JTextField discordWebhookUrlTextField = new JTextField();
      discordWebhookUrlTextField.setToolTipText("DISCORD_WEBHOOK_URL");
      generalSettings.add(discordWebhookUrlTextField);
      components.put("general.DISCORD_WEBHOOK_URL", discordWebhookUrlTextField);
      JLabel mulingLabel = new JLabel("Muling options", 0);
      mulePanel.add(mulingLabel);
      JCheckBox mulingEnabledCheckbox = new JCheckBox("Enable Muling", false);
      mulingEnabledCheckbox.setToolTipText("ENABLE_MULING");
      mulePanel.add(mulingEnabledCheckbox);
      components.put("mule.ENABLE_MULING", mulingEnabledCheckbox);
      JLabel muleAmountToKeepLabel = new JLabel("Amount of gold to keep", 0);
      mulePanel.add(muleAmountToKeepLabel);
      JSpinner muleAmountToKeepSpinner = new JSpinner();
      muleAmountToKeepSpinner.setToolTipText("MULE_AMOUNT_TO_KEEP");
      muleAmountToKeepSpinner.setValue(3000000);
      mulePanel.add(muleAmountToKeepSpinner);
      components.put("mule.MULE_AMOUNT_TO_KEEP", muleAmountToKeepSpinner);
      JLabel muleAmountToBeginLabel = new JLabel("Amount of gold to begin muling", 0);
      mulePanel.add(muleAmountToBeginLabel);
      JSpinner muleAmountToBeginSpinner = new JSpinner();
      muleAmountToBeginSpinner.setToolTipText("MULE_AMOUNT_TO_BEGIN");
      muleAmountToBeginSpinner.setValue(10000000);
      mulePanel.add(muleAmountToBeginSpinner);
      components.put("mule.MULE_AMOUNT_TO_BEGIN", muleAmountToBeginSpinner);
      JLabel mulePortLabel = new JLabel("Mule Port", 0);
      mulePanel.add(mulePortLabel);
      JSpinner mulePortSpinner = new JSpinner();
      mulePortSpinner.setToolTipText("MULE_PORT");
      mulePortSpinner.setValue(50000);
      mulePanel.add(mulePortSpinner);
      components.put("mule.MULE_PORT", mulePortSpinner);
      settingsGUI.getContentPane().add(tabbedPane, "Center");
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout());
      JButton button = new JButton();
      button.setText("Start");
      button.addActionListener((l) -> {
         startScript();
         settingsGUI.dispose();
      });
      buttonPanel.add(button);
      JButton load = new JButton("Load Profile");
      load.addActionListener((e) -> {
         loadProfile(profileName.getSelectedItem().toString());
      });
      buttonPanel.add(load);
      JButton save = new JButton("Save Profile");
      save.addActionListener((e) -> {
         saveProfile();
      });
      buttonPanel.add(save);
      profileName = new JComboBox(availableProfiles().stream().toArray());
      profileName.setEditable(true);
      buttonPanel.add(profileName);
      settingsGUI.getContentPane().add(buttonPanel, "South");
      settingsGUI.pack();
      settingsGUI.setVisible(visible);
      if (!visible) {
         Logger.log("Loading " + profileToLoad);
         loadProfile(profileToLoad);
         Logger.log("Starting " + profileToLoad);
         startScript();
      }

   }

   private static void saveProfile() {
      String profileName = Settings.profileName.getSelectedItem().toString();
      Logger.log("Saving " + profileName);

      try {
         OutputStream output = new FileOutputStream(configFolder + profileName + ".properties");
         Throwable var2 = null;

         try {
            Properties prop = new Properties();
            Component[] generalSettingsComponents = generalSettings.getComponents();
            Component[] mulePanelComponents = generalSettingsComponents;
            int var6 = generalSettingsComponents.length;

            int var7;
            for(var7 = 0; var7 < var6; ++var7) {
               Component component = mulePanelComponents[var7];
               if (component instanceof JComboBox) {
                  if (((JComboBox)component).getToolTipText().equals("SELECTED_WORLD")) {
                     prop.setProperty("general." + ((JComboBox)component).getToolTipText(), ((JComboBox)component).getSelectedItem().toString());
                  }

                  if (((JComboBox)component).getToolTipText().equals("COMBAT_STYLE")) {
                     prop.setProperty("general." + ((JComboBox)component).getToolTipText(), ((JComboBox)component).getSelectedItem().toString());
                  }
               }

               if (component instanceof JTextField) {
                  prop.setProperty("general." + ((JTextField)component).getToolTipText(), ((JTextField)component).getText());
               }

               if (component instanceof JSpinner) {
                  prop.setProperty("general." + ((JSpinner)component).getToolTipText(), String.valueOf(((JSpinner)component).getValue()));
               }

               if (component instanceof JCheckBox) {
                  prop.setProperty("general." + ((JCheckBox)component).getToolTipText(), Boolean.toString(((JCheckBox)component).isSelected()));
               }
            }

            mulePanelComponents = mulePanel.getComponents();
            Component[] var21 = mulePanelComponents;
            var7 = mulePanelComponents.length;

            for(int var22 = 0; var22 < var7; ++var22) {
               Component component = var21[var22];
               if (component instanceof JCheckBox) {
                  prop.setProperty("mule." + ((JCheckBox)component).getToolTipText(), Boolean.toString(((JCheckBox)component).isSelected()));
               }

               if (component instanceof JSpinner) {
                  prop.setProperty("mule." + ((JSpinner)component).getToolTipText(), String.valueOf(((JSpinner)component).getValue()));
               }
            }

            prop.store(output, (String)null);
            Logger.log("Saved profile");
         } catch (Throwable var18) {
            var2 = var18;
            throw var18;
         } finally {
            if (output != null) {
               if (var2 != null) {
                  try {
                     output.close();
                  } catch (Throwable var17) {
                     var2.addSuppressed(var17);
                  }
               } else {
                  output.close();
               }
            }

         }

      } catch (IOException var20) {
         throw new RuntimeException(var20);
      }
   }

   private static void loadProfile(String profileName) {
      Logger.log("Loading " + profileName);

      try {
         InputStream input = new FileInputStream(configFolder + profileName + ".properties");
         Throwable var2 = null;

         try {
            Properties prop = new Properties();
            prop.load(input);
            Iterator var4 = prop.stringPropertyNames().iterator();

            while(var4.hasNext()) {
               String property = (String)var4.next();
               Component component = (Component)components.get(property);
               if (property.startsWith("general.")) {
                  if (component instanceof JComboBox) {
                  }

                  if (component instanceof JTextField) {
                     ((JTextField)component).setText(prop.getProperty(property));
                  }

                  if (component instanceof JSpinner) {
                     ((JSpinner)component).setValue(Integer.parseInt(prop.getProperty(property)));
                  }

                  if (component instanceof JCheckBox) {
                     ((JCheckBox)component).setSelected(Boolean.parseBoolean(prop.getProperty(property)));
                  }
               }

               if (property.startsWith("mule.")) {
                  if (component instanceof JTextField) {
                     ((JTextField)component).setText(prop.getProperty(property));
                  }

                  if (component instanceof JSpinner) {
                     ((JSpinner)component).setValue(Integer.parseInt(prop.getProperty(property)));
                  }

                  if (component instanceof JCheckBox) {
                     ((JCheckBox)component).setSelected(Boolean.parseBoolean(prop.getProperty(property)));
                  }
               }
            }
         } catch (Throwable var15) {
            var2 = var15;
            throw var15;
         } finally {
            if (input != null) {
               if (var2 != null) {
                  try {
                     input.close();
                  } catch (Throwable var14) {
                     var2.addSuppressed(var14);
                  }
               } else {
                  input.close();
               }
            }

         }
      } catch (Exception var17) {
         Logger.log(var17);
      }

   }

   private static void startScript() {
      Logger.log("Starting script");

      try {
         Component[] generalSettingsComponents = generalSettings.getComponents();
         Component[] mulePanelComponents = generalSettingsComponents;
         int var2 = generalSettingsComponents.length;

         int var3;
         for(var3 = 0; var3 < var2; ++var3) {
            Component component = mulePanelComponents[var3];
            if (component instanceof JTextField && ((JTextField)component).getToolTipText().equals("DISCORD_WEBHOOK_URL")) {
               setDiscordWebhookUrl(((JTextField)component).getText());
            }

            if (component instanceof JCheckBox) {
            }
         }

         mulePanelComponents = mulePanel.getComponents();
         Component[] var7 = mulePanelComponents;
         var3 = mulePanelComponents.length;

         for(int var8 = 0; var8 < var3; ++var8) {
            Component component = var7[var8];
            if (component instanceof JCheckBox && ((JCheckBox)component).getToolTipText().equals("ENABLE_MULING")) {
               setMulingEnabled(((JCheckBox)component).isSelected());
            }

            if (component instanceof JSpinner) {
               if (((JSpinner)component).getToolTipText().equals("MULE_AMOUNT_TO_BEGIN")) {
                  MuleUtil.setGoldToMuleAt((Integer)((JSpinner)component).getValue());
               }

               if (((JSpinner)component).getToolTipText().equals("MULE_PORT")) {
                  MuleUtil.setMulePort((Integer)((JSpinner)component).getValue());
               }

               if (((JSpinner)component).getToolTipText().equals("MULE_AMOUNT_TO_KEEP")) {
                  MuleUtil.setGoldToKeep((Integer)((JSpinner)component).getValue());
               }
            }
         }
      } catch (Exception var6) {
         Logger.log(var6);
      }

   }

   private static String createFolder() {
      Properties properties = System.getProperties();
      String scriptsFolder = properties.getProperty("scripts.path");
      File folder = new File(scriptsFolder + "/Pyfa/Alcher");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      return scriptsFolder + "/Pyfa/Alcher/";
   }

   private static List<String> availableProfiles() {
      File folder = new File(createFolder());
      File[] listOfFiles = folder.listFiles();
      List<String> availableProfiles = new ArrayList();

      for(int i = 0; i < listOfFiles.length; ++i) {
         if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".properties")) {
            availableProfiles.add(listOfFiles[i].getName().split("\\.")[0]);
         }
      }

      return availableProfiles;
   }

   public static JFrame getSettingsGUI() {
      return settingsGUI;
   }

   public static void setSettingsGUI(JFrame settingsGUI) {
      Settings.settingsGUI = settingsGUI;
   }

   public static String getDiscordWebhookUrl() {
      return discordWebhookUrl;
   }

   public static void setDiscordWebhookUrl(String discordWebhookUrl) {
      Settings.discordWebhookUrl = discordWebhookUrl;
   }

   public static boolean isF2p() {
      return f2p;
   }

   public static void setF2p(boolean f2p) {
      Settings.f2p = f2p;
   }

   public static boolean isMulingEnabled() {
      return mulingEnabled;
   }

   public static void setMulingEnabled(boolean mulingEnabled) {
      Settings.mulingEnabled = mulingEnabled;
   }

   public static boolean isTakingGoldFromMuleEnabled() {
      return takingGoldFromMuleEnabled;
   }

   public static void setTakingGoldFromMuleEnabled(boolean takingGoldFromMuleEnabled) {
      Settings.takingGoldFromMuleEnabled = takingGoldFromMuleEnabled;
   }
}
