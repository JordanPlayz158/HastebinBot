package xyz.jordanplayz158.hastebinbot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ReplyListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();

        if(!user.isBot()) {
            Message message = event.getMessage();
            Message referencedMessage = message.getReferencedMessage();

            if(message.getMentionedUsers().contains(event.getJDA().getSelfUser())) {
                StringBuilder result = new StringBuilder();

                if (referencedMessage != null) {
                    result.append(sendToHastebin(referencedMessage.getContentStripped())).append(sendAttachmentsToHastebin(referencedMessage));
                } else {
                    result.append(sendToHastebin(message.getContentStripped())).append(sendAttachmentsToHastebin(message));
                }

                event.getChannel().sendMessage(result).mention(event.getAuthor()).queue();
            }
        }
    }

    public String sendToHastebin(String message) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, message);
        Request request = new Request.Builder()
                .url("https://hastebin.com/documents")
                .method("POST", body)
                .addHeader("Content-Type", "text/plain")
                .build();
        try {
            Response response = client.newCall(request).execute();

            JsonObject jsonObject = new Gson().fromJson(response.body().string(), JsonObject.class);

            return "https://hastebin.com/" + jsonObject.get("key").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendAttachmentsToHastebin(Message message) {
        StringBuilder result = new StringBuilder();

        message.getAttachments().forEach(attachment -> {
            try {
                File attachmentFile = attachment.downloadToFile().get();

                result.append("\n").append(attachment.getFileName()).append(": ").append(sendToHastebin(readFileContents(attachmentFile)));

                Files.deleteIfExists(attachmentFile.toPath());
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        });

        return result.toString();
    }

    public String readFileContents(File file) {
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
