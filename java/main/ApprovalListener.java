package main;
import java.sql.Connection;
import java.sql.SQLException;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.DataHandler;
import utils.Server;

public class ApprovalListener extends ListenerAdapter
{
	private Connection con;
	
	public ApprovalListener()
	{
		con = utils.DataHandler.getConnection();
	}
	
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e)
	{
		if(e.getChannel().getId().equals(utils.Server.approval_channel_id))
		{
			//bot ignores all non-staff and itself
			if(!utils.Server.isStaff(e.getMember()) || e.getMember().getUser().equals(e.getJDA().getSelfUser()))
				return;
			
			e.getChannel().retrieveMessageById(e.getMessageId()).queue(msg ->
			{
				//if a reaction is added to a message in the RIGHT channel but it isn't a reaction to the bot's message
				if(!msg.getAuthor().equals(e.getJDA().getSelfUser()) || !msg.getEmbeds().get(0).getFooter().getText().equals("quotes"))
					return;
				
				//only interested in the right reactions
				if(!e.getReactionEmote().getName().equals(utils.Server.approvalEmoteUnicode) && !e.getReactionEmote().getName().equals(utils.Server.rejectEmoteUnicode))
					return;
				
				//every embed log should have an image with a url, and a user id
				MessageEmbed em = msg.getEmbeds().get(0);
				String userID = null;
				String imgurl = null;
				String quoter = null;
				for(Field field : em.getFields())
				{
					if(field.getName().equals("User id:"))
						userID = field.getValue();
					else if(field.getName().equals("Quote:"))
						imgurl = field.getValue();
					else if(field.getName().equals("Quoted by:"))
						quoter = field.getValue();
				}
				
				try
				{
					//always remove from the pending quotes list (quote urls assumed to be unique, and if not, this deletes duplicates anyway)
					con.createStatement().executeUpdate("delete from Wilbur_pendingquotes where quote = \""
							+ imgurl + "\"");
					
					//approved
					if(e.getReactionEmote().getName().equals(utils.Server.approvalEmoteUnicode))
					{
						con.createStatement().executeUpdate("insert into Wilbur_approvedquotes (userid, quote, userindex)"
								+ "values (" + userID + ", \"" + imgurl + "\", " 
								+ (DataHandler.getResultSize("select * from Wilbur_approvedquotes where userid = " + userID) + 1) + ")");
						
						Server.sendQuote(userID, imgurl);
						e.getChannel().sendMessage("Quote approved!").queue();
						Server.sendLog(userID, imgurl, quoter, e.getMember().getId(), true);
					}
					else
					{
						e.getChannel().sendMessage("Quote rejected").queue();
						Server.sendLog(userID, imgurl, quoter, e.getMember().getId(), true);
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
				
				msg.delete().queue();
			});
		}
	}
}
