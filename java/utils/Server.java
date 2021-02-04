package utils;

import java.time.Instant;

import main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class Server
{
	public static final String approvalEmoteStr = "white_check_mark", rejectEmoteStr = "x";
	public static final String approvalEmoteUnicode = "\u2705", rejectEmoteUnicode = "\u274C";
	public static final String approval_channel_id = "653257117536485387", post_channel_id = "599720754690392094", log_channel_id = "565631919728099338";
	public static final int EMBED_COL_INT = 65280;
	
	//return true if a member has discord mod, admin or is owner
	public static boolean isStaff(Member m)
	{
		try
		{
			//if owner
			if(m.isOwner())
				return true;
		}
		catch(NullPointerException e)
		{
			//no error message reee its pissing me off
		}
		
		//if admin
		if(m.hasPermission(Permission.ADMINISTRATOR))
			return true;
		
		//if discord mod TODO: Make discord mod module for all servers
		switch(m.getGuild().getId())
		{
			case "565623426501443584" : //wilbur's discord
				for(Role r : m.getRoles())
				{
					if(r.getId().equals("565626094917648386")) //wilbur discord mod
						return true;
				}
				break;
				
			case "640254333807755304" : //charlie's server
				for(Role r : m.getRoles())
				{
					if(r.getId().equals("640255355401535499")) //charlie discord mod
						return true;
				}
				break;
		}
		
		return false;
	}
	
	
	public static void sendApproval(String userID, String url, User quoter)
	{
		/*
		 * This method sends a log to a channel so that quotes can be approved.
		 * This method assumes the message is a legitimate submission syntax-wise
		 */
		
		EmbedBuilder eb = new EmbedBuilder()
			.setTitle("New Quote Submission:")
			.setAuthor(main.Main.jda.getUserById(userID).getAsTag())
			.addField("User id:", userID, true)
			.addField("Quote:", url, true)
			.addField("Quote of:", main.Main.jda.getUserById(userID).getAsTag(), true)
			.addField("Quoted by:", quoter.getAsTag(), true)
			.setImage(url)
			.setTimestamp(Instant.now())
			.setColor(EMBED_COL_INT)
			.setFooter("quotes");
		
		//send to approval channel
		main.Main.jda.getTextChannelById(approval_channel_id).sendMessage(eb.build()).queue(log ->
		{
			//emote: white_check_mark
			log.addReaction(approvalEmoteUnicode).queue();
			//emote: x
			log.addReaction(rejectEmoteUnicode).queue();
		});
	}
	
	public static void sendQuote(String userid, String url)
	{
		EmbedBuilder quotePost = new EmbedBuilder()
				.setTitle("New Quote!")
				.setDescription("Check out this quote of "
					+ main.Main.jda.getGuildById("565623426501443584").getMemberById(userid).getEffectiveName() + "!")
				.setImage(url)
				.setColor(Server.EMBED_COL_INT);
			
			Main.jda.getTextChannelById(Server.post_channel_id).sendMessage(quotePost.build()).queue();
	}
	
	public static void sendLog(String userid, String url, String quoter, String approvalid, boolean approved)
	{
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Quote " + (approved ? "Approved" : "Rejected"))
				.setAuthor(main.Main.jda.getUserById(userid).getAsTag())
				.addField("User id: ", userid, true)
				.addField("Quote: ", url, true)
				.addField("Quoted by:", quoter, true)
				.addField((approved ? "Approved " : "Rejected ") + "by: ", main.Main.jda.getUserById(approvalid).getAsTag(), true)
				.setImage(url)
				.setTimestamp(Instant.now())
				.setColor(EMBED_COL_INT);
		
		main.Main.jda.getTextChannelById(log_channel_id).sendMessage(eb.build()).queue();
	}
}
