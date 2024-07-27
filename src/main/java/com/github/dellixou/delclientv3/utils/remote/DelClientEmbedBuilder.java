package com.github.dellixou.delclientv3.utils.remote;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class DelClientEmbedBuilder {
    
    public static EmbedBuilder simpleEmbed(String content, Color color){
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Remote Controller");

        embed.setDescription(content);

        embed.setColor(color);

        embed.setFooter("DelClient Remote Controller - Version 0.1");
        return embed;
    }
    
}
