package main;
import java.io.IOException;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.ApprovedQuote;
import utils.DataHandler;
import utils.Server;
import utils.UnapprovedQuote;

public class ApprovalListener extends ListenerAdapter
{	
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e)
	{
		if(!e.getGuild().getId().equals(Server.SERVER_ID)) //only wilbur's server
			return;
		
		if(e.getChannel().getId().equals(Server.JUDGEMENT_CHANNEL_ID))
		{
			//bot ignores all non-staff
			if(!utils.Server.isStaff(e.getMember()))
				return;
			
			e.getChannel().retrieveMessageById(e.getMessageId()).queue( (msg) ->
			{
				//if a reaction is added to a message in the RIGHT channel but it isn't a reaction to the bot's message
				if(!msg.getAuthor().equals(e.getJDA().getSelfUser()))
					return;
				
				//only interested in the right reactions
				if(!e.getReactionEmote().getName().equals(Server.APPROVAL_EMOTE_UNICODE) && !e.getReactionEmote().getName().equals(Server.REJECT_EMOTE_UNICODE))
					return;
				
				//every embed log should have an image with a url, and a user id
				MessageEmbed em = msg.getEmbeds().get(0);
				//formatted like this in utils.Server.sendLog() (may change in the future)
				String imgurl = em.getDescription().split("\n")[1];
				
				//always remove from the pending quotes list
				UnapprovedQuote quote = Main.uncheckedquotes.remove(Main.uncheckedquotes.indexOf(DataHandler.getUnapprovedQuoteByUrl(imgurl)));
				String userID = quote.getId();
				
				//approved
				if(e.getReactionEmote().getName().equals(Server.APPROVAL_EMOTE_UNICODE))
				{
					Main.checkedquotes.add(new ApprovedQuote(quote));
					
					//save in storage after updating
					try
					{
						DataHandler.writeData();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					
					EmbedBuilder quotePost = new EmbedBuilder()
						.setTitle("New Quote!")
						.setColor(Server.EMBED_COL_INT)
						.setDescription("Check out this quote of "
							+ e.getGuild().getMemberById(userID).getEffectiveName() + "!")
						.setImage(imgurl);
					Main.jda.getTextChannelById(Server.QUOTE_CHANNEL_ID).sendMessage(quotePost.build()).queue();
					
					e.getChannel().sendMessage("Quote approved!").queue();
					
					EmbedBuilder log = new EmbedBuilder()
							.setTitle("Quote approved")
							.setColor(Server.EMBED_COL_INT)
							.addField("From: ", e.getGuild().getMemberById(quote.getId()).getUser().getAsTag(), true)
							.addField("Approved by: ", e.getUser().getAsTag(), true)
							.setImage(quote.getQuote())
							.setTimestamp(Instant.now());
					
					e.getGuild().getTextChannelById(Server.LOG_CHANNEL_ID).sendMessage(log.build()).queue();
					
					msg.delete().queue();
				}
				
				//rejected
				if(e.getReactionEmote().getName().equals(Server.REJECT_EMOTE_UNICODE))
				{
					e.getChannel().sendMessage("Quote rejected").queue();
					
					EmbedBuilder log = new EmbedBuilder()
							.setTitle("Quote rejected")
							.setColor(Server.EMBED_COL_INT)
							.addField("From: ", e.getGuild().getMemberById(quote.getId()).getUser().getAsTag(), true)
							.addField("Rejected by: ", e.getUser().getAsTag(), true)
							.setImage(quote.getQuote())
							.setTimestamp(Instant.now());
					
					e.getGuild().getTextChannelById(Server.LOG_CHANNEL_ID).sendMessage(log.build()).queue();
					
					msg.delete().queue();
				}
			});
		}
	}
}
