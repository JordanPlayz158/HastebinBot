package xyz.jordanplayz158.hastebinbot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.jordanplayz158.hastebinbot.HastebinBot;

import java.util.Objects;

public class MentionListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();

        if(!user.isBot()) {
            Message message = event.getMessage();
            Message referencedMessage = message.getReferencedMessage();

            if(message.getMentionedUsers().contains(event.getJDA().getSelfUser())) {
                StringBuilder result = new StringBuilder();

                if (referencedMessage != null) {
                    result
                            .append(Objects.requireNonNullElse(HastebinBot.sendToHastebin(referencedMessage.getContentStripped()), ""))
                            .append(HastebinBot.sendAttachmentsToHastebin(referencedMessage));
                } else {
                    result
                            .append(HastebinBot.sendToHastebin(message.getContentStripped()))
                            .append(HastebinBot.sendAttachmentsToHastebin(message));
                }

                event.getChannel().sendMessage(result).mention(user).queue();
            }
        }
    }
}
