package org.lolwat;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class DiscordWebhook {
   private final String url;
   private final List<EmbedObject> embeds = new ArrayList();
   private String content;
   private String username;
   private String avatarUrl;
   private boolean tts;

   public DiscordWebhook(String url) {
      this.url = url;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public void setAvatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
   }

   public void setTts(boolean tts) {
      this.tts = tts;
   }

   public void addEmbed(EmbedObject embed) {
      this.embeds.add(embed);
   }

   public void execute() throws IOException {
      if (this.content == null && this.embeds.isEmpty()) {
         throw new IllegalArgumentException("Set content or add at least one EmbedObject");
      } else {
         JSONObject json = new JSONObject();
         json.put("content", this.content);
         json.put("username", this.username);
         json.put("avatar_url", this.avatarUrl);
         json.put("tts", this.tts);
         if (!this.embeds.isEmpty()) {
            List<JSONObject> embedObjects = new ArrayList();
            Iterator var3 = this.embeds.iterator();

            while(var3.hasNext()) {
               EmbedObject embed = (EmbedObject)var3.next();
               JSONObject jsonEmbed = new JSONObject();
               jsonEmbed.put("title", embed.getTitle());
               jsonEmbed.put("description", embed.getDescription());
               jsonEmbed.put("url", embed.getUrl());
               if (embed.getColor() != null) {
                  Color color = embed.getColor();
                  int rgb = color.getRed();
                  rgb = (rgb << 8) + color.getGreen();
                  rgb = (rgb << 8) + color.getBlue();
                  jsonEmbed.put("color", rgb);
               }

               EmbedObject.Footer footer = embed.getFooter();
               EmbedObject.Image image = embed.getImage();
               EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
               EmbedObject.Author author = embed.getAuthor();
               List<EmbedObject.Field> fields = embed.getFields();
               JSONObject jsonAuthor;
               if (footer != null) {
                  jsonAuthor = new JSONObject();
                  jsonAuthor.put("text", footer.getText());
                  jsonAuthor.put("icon_url", footer.getIconUrl());
                  jsonEmbed.put("footer", jsonAuthor);
               }

               if (image != null) {
                  jsonAuthor = new JSONObject();
                  jsonAuthor.put("url", image.getUrl());
                  jsonEmbed.put("image", jsonAuthor);
               }

               if (thumbnail != null) {
                  jsonAuthor = new JSONObject();
                  jsonAuthor.put("url", thumbnail.getUrl());
                  jsonEmbed.put("thumbnail", jsonAuthor);
               }

               if (author != null) {
                  jsonAuthor = new JSONObject();
                  jsonAuthor.put("name", author.getName());
                  jsonAuthor.put("url", author.getUrl());
                  jsonAuthor.put("icon_url", author.getIconUrl());
                  jsonEmbed.put("author", jsonAuthor);
               }

               List<JSONObject> jsonFields = new ArrayList();
               Iterator var12 = fields.iterator();

               while(var12.hasNext()) {
                  EmbedObject.Field field = (EmbedObject.Field)var12.next();
                  JSONObject jsonField = new JSONObject();
                  jsonField.put("name", field.getName());
                  jsonField.put("value", field.getValue());
                  jsonField.put("inline", field.isInline());
                  jsonFields.add(jsonField);
               }

               jsonEmbed.put("fields", jsonFields.toArray());
               embedObjects.add(jsonEmbed);
            }

            json.put("embeds", embedObjects.toArray());
         }

         URL url = new URL(this.url);
         HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
         connection.addRequestProperty("Content-Type", "application/json");
         connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
         connection.setDoOutput(true);
         connection.setRequestMethod("POST");
         OutputStream stream = connection.getOutputStream();
         stream.write(json.toString().getBytes());
         stream.flush();
         stream.close();
         connection.getInputStream().close();
         connection.disconnect();
      }
   }

   private class JSONObject {
      private final HashMap<String, Object> map;

      private JSONObject() {
         this.map = new HashMap();
      }

      void put(String key, Object value) {
         if (value != null) {
            this.map.put(key, value);
         }

      }

      public String toString() {
         StringBuilder builder = new StringBuilder();
         Set<Entry<String, Object>> entrySet = this.map.entrySet();
         builder.append("{");
         int i = 0;
         Iterator var4 = entrySet.iterator();

         while(var4.hasNext()) {
            Entry<String, Object> entry = (Entry)var4.next();
            Object val = entry.getValue();
            builder.append(this.quote((String)entry.getKey())).append(":");
            if (val instanceof String) {
               builder.append(this.quote(String.valueOf(val)));
            } else if (val instanceof Integer) {
               builder.append(Integer.valueOf(String.valueOf(val)));
            } else if (val instanceof Boolean) {
               builder.append(val);
            } else if (val instanceof JSONObject) {
               builder.append(val);
            } else if (val.getClass().isArray()) {
               builder.append("[");
               int len = Array.getLength(val);

               for(int j = 0; j < len; ++j) {
                  builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
               }

               builder.append("]");
            }

            ++i;
            builder.append(i == entrySet.size() ? "}" : ",");
         }

         return builder.toString();
      }

      private String quote(String string) {
         return "\"" + string + "\"";
      }

      // $FF: synthetic method
      JSONObject(Object x1) {
         this();
      }
   }

   public static class EmbedObject {
      private final List<Field> fields = new ArrayList();
      private String title;
      private String description;
      private String url;
      private Color color;
      private Footer footer;
      private Thumbnail thumbnail;
      private Image image;
      private Author author;

      public String getTitle() {
         return this.title;
      }

      public EmbedObject setTitle(String title) {
         this.title = title;
         return this;
      }

      public String getDescription() {
         return this.description;
      }

      public EmbedObject setDescription(String description) {
         this.description = description;
         return this;
      }

      public String getUrl() {
         return this.url;
      }

      public EmbedObject setUrl(String url) {
         this.url = url;
         return this;
      }

      public Color getColor() {
         return this.color;
      }

      public EmbedObject setColor(Color color) {
         this.color = color;
         return this;
      }

      public Footer getFooter() {
         return this.footer;
      }

      public Thumbnail getThumbnail() {
         return this.thumbnail;
      }

      public EmbedObject setThumbnail(String url) {
         this.thumbnail = new Thumbnail(url);
         return this;
      }

      public Image getImage() {
         return this.image;
      }

      public EmbedObject setImage(String url) {
         this.image = new Image(url);
         return this;
      }

      public Author getAuthor() {
         return this.author;
      }

      public List<Field> getFields() {
         return this.fields;
      }

      public EmbedObject setFooter(String text, String icon) {
         this.footer = new Footer(text, icon);
         return this;
      }

      public EmbedObject setAuthor(String name, String url, String icon) {
         this.author = new Author(name, url, icon);
         return this;
      }

      public EmbedObject addField(String name, String value, boolean inline) {
         this.fields.add(new Field(name, value, inline));
         return this;
      }

      private class Field {
         private final String name;
         private final String value;
         private final boolean inline;

         private Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
         }

         private String getName() {
            return this.name;
         }

         private String getValue() {
            return this.value;
         }

         private boolean isInline() {
            return this.inline;
         }

         // $FF: synthetic method
         Field(String x1, String x2, boolean x3, Object x4) {
            this(x1, x2, x3);
         }
      }

      private class Author {
         private final String name;
         private final String url;
         private final String iconUrl;

         private Author(String name, String url, String iconUrl) {
            this.name = name;
            this.url = url;
            this.iconUrl = iconUrl;
         }

         private String getName() {
            return this.name;
         }

         private String getUrl() {
            return this.url;
         }

         private String getIconUrl() {
            return this.iconUrl;
         }

         // $FF: synthetic method
         Author(String x1, String x2, String x3, Object x4) {
            this(x1, x2, x3);
         }
      }

      private class Image {
         private final String url;

         private Image(String url) {
            this.url = url;
         }

         private String getUrl() {
            return this.url;
         }

         // $FF: synthetic method
         Image(String x1, Object x2) {
            this(x1);
         }
      }

      private class Thumbnail {
         private final String url;

         private Thumbnail(String url) {
            this.url = url;
         }

         private String getUrl() {
            return this.url;
         }

         // $FF: synthetic method
         Thumbnail(String x1, Object x2) {
            this(x1);
         }
      }

      private class Footer {
         private final String text;
         private final String iconUrl;

         private Footer(String text, String iconUrl) {
            this.text = text;
            this.iconUrl = iconUrl;
         }

         private String getText() {
            return this.text;
         }

         private String getIconUrl() {
            return this.iconUrl;
         }

         // $FF: synthetic method
         Footer(String x1, String x2, Object x3) {
            this(x1, x2);
         }
      }
   }
}
