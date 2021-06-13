package xyz.jordanplayz158.hastebinbot;

import com.google.gson.JsonObject;
import me.jordanplayz158.utils.FileUtils;
import me.jordanplayz158.utils.Initiate;
import me.jordanplayz158.utils.LoadJson;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

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
        jdaBuilder.addEventListeners(new ReplyListener());

        JsonObject activity = config.getAsJsonObject("activity");

        jdaBuilder
                .setActivity(Activity.of(Activity.ActivityType.valueOf(activity.get("type").getAsString()), activity.get("name").getAsString()))
                .build();
    }
}
