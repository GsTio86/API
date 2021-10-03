package com.envyful.api.discord;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 *
 * Class for handling simple Discord webhooks
 * Originally from:
 * <a>https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb</a>
 *
 */
public class DiscordWebHook {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private List<EmbedObject> embeds = new ArrayList<>();

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebHook(String url) {
        this.url = url;
    }

    /**
     *
     * Sets the text content of the message
     *
     * @param content The text content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     *
     * Sets the username of the sender
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * Sets the URL of the avatar for the sending user
     *
     * @param avatarUrl the avatar url
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     *
     * Sets if the message should be read as TTS to anyone in a voice channel reading the given text channel
     *
     * @param tts if it should be TTS
     */
    public void setTts(boolean tts) {
        this.tts = tts;
    }

    /**
     *
     * Adds an embed to the message being sent
     *
     * @param embed The embed being added
     */
    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    /**
     *
     * Gets a new instance of the Builder class
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     * Clones current web hook to a new object
     *
     * @return new webhook
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DiscordWebHook clone() {
        DiscordWebHook webHook = new DiscordWebHook(this.url);
        webHook.setUsername(this.username);
        webHook.setAvatarUrl(this.avatarUrl);
        webHook.setTts(this.tts);

        for (EmbedObject embed : this.embeds) {
            webHook.addEmbed(embed);
        }

        return webHook;
    }

    /**
     *
     * Executes the message and sends it to the web hook URL
     *
     * @throws IOException Exception when something is incorrect or goes wrong
     */
    public void execute() throws IOException {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        JSONObject json = new JSONObject();

        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);

        if (!this.embeds.isEmpty()) {
            List<JSONObject> embedObjects = new ArrayList<>();

            for (EmbedObject embed : this.embeds) {
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

                if (footer != null) {
                    JSONObject jsonFooter = new JSONObject();

                    jsonFooter.put("text", footer.getText());
                    jsonFooter.put("icon_url", footer.getIconUrl());
                    jsonEmbed.put("footer", jsonFooter);
                }

                if (image != null) {
                    JSONObject jsonImage = new JSONObject();

                    jsonImage.put("url", image.getUrl());
                    jsonEmbed.put("image", jsonImage);
                }

                if (thumbnail != null) {
                    JSONObject jsonThumbnail = new JSONObject();

                    jsonThumbnail.put("url", thumbnail.getUrl());
                    jsonEmbed.put("thumbnail", jsonThumbnail);
                }

                if (author != null) {
                    JSONObject jsonAuthor = new JSONObject();

                    jsonAuthor.put("name", author.getName());
                    jsonAuthor.put("url", author.getUrl());
                    jsonAuthor.put("icon_url", author.getIconUrl());
                    jsonEmbed.put("author", jsonAuthor);
                }

                List<JSONObject> jsonFields = new ArrayList<>();
                for (EmbedObject.Field field : fields) {
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
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes());
        stream.flush();
        stream.close();

        connection.getInputStream().close(); //I'm not sure why but it doesn't work without getting the InputStream
        connection.disconnect();
    }

    /**
     *
     * Static builder class for the Web hook
     *
     */
    public static class Builder {

        private String url;
        private String content;
        private String username;
        private String avatarUrl;
        private boolean tts;
        private List<EmbedObject> embeds = new ArrayList<>();

        Builder() {}

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder tts(boolean tts) {
            this.tts = tts;
            return this;
        }

        public Builder embeds(EmbedObject... embeds) {
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }

        public DiscordWebHook build() {
            DiscordWebHook webHook = new DiscordWebHook(this.url);
            webHook.setContent(this.content);
            webHook.setUsername(this.username);
            webHook.setAvatarUrl(this.avatarUrl);
            webHook.setTts(this.tts);

            for (EmbedObject embed : this.embeds) {
                webHook.addEmbed(embed);
            }

            return webHook;
        }
    }

    private class JSONObject {

        private final HashMap<String, Object> map = new HashMap<>();

        void put(String key, Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            builder.append("{");

            int i = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                Object val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof String) {
                    builder.append(quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val.toString());
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);
                    for (int j = 0; j < len; j++) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder.toString();
        }

        private String quote(String string) {
            return "\"" + string + "\"";
        }
    }
}