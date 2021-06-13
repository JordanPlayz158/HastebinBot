package xyz.jordanplayz158.hastebinbot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.jordanplayz158.utils.FileUtils;
import me.jordanplayz158.utils.Initiate;
import me.jordanplayz158.utils.LoadJson;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.log4j.Logger;
import xyz.jordanplayz158.hastebinbot.listeners.AttachmentListener;
import xyz.jordanplayz158.hastebinbot.listeners.MentionListener;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public class HastebinBot {
    public static void main(String[] args) throws IOException, LoginException {
        final Logger logger = Initiate.log();
        final File configFile = new File("config.json");

        FileUtils.copyFile(configFile);

        JsonObject config = LoadJson.linkedTreeMap(configFile);
        String token = config.get("token").getAsString();

        if(token.length() <= 1) {
            logger.fatal("You have to provide a token in your config file for the bot to boot!");
            System.exit(1);
        }

        // Token and activity is read from and set in the config.json
        JDABuilder jdaBuilder = JDABuilder.createLight(token);

        jdaBuilder.enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES);
        jdaBuilder.addEventListeners(new MentionListener());

        if(config.get("auto").getAsBoolean()) {
            jdaBuilder.addEventListeners(new AttachmentListener());
        }

        JsonObject activity = config.getAsJsonObject("activity");

        jdaBuilder
                .setActivity(Activity.of(Activity.ActivityType.valueOf(activity.get("type").getAsString()), activity.get("name").getAsString()))
                .build();
    }

    public static String sendToHastebin(String message) {
        if(message.length() < 1) {
            return null;
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(message, mediaType);
        Request request = new Request.Builder()
                .url("https://hastebin.com/documents")
                .method("POST", body)
                .addHeader("Content-Type", "text/plain")
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(response.body() == null) {
                return null;
            }

            JsonObject jsonObject = new Gson().fromJson(response.body().string(), JsonObject.class);

            return "https://hastebin.com/" + jsonObject.get("key").getAsString() + "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String sendAttachmentsToHastebin(Message message) {
        StringBuilder result = new StringBuilder();

        message.getAttachments().forEach(attachment -> {
            try {
                File attachmentFile = attachment.downloadToFile().get();

                result.append(attachment.getFileName()).append(": ").append(sendToHastebin(readFileContents(attachmentFile)));

                Files.deleteIfExists(attachmentFile.toPath());
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        });

        return result.toString();
    }

    private static String readFileContents(File file) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultStringBuilder.toString();
    }
}
