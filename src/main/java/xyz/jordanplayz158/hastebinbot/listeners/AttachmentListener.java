package xyz.jordanplayz158.hastebinbot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.jordanplayz158.hastebinbot.HastebinBot;

public class AttachmentListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();
        Message message = event.getMessage();

        if (!user.isBot() && message.getAttachments().size() > 0) {
            event.getChannel().sendMessage(HastebinBot.sendAttachmentsToHastebin(message)).mention(user).queue();
        }
    }
}
