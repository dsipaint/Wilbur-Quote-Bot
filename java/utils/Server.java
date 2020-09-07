package utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Server
{
	public static final String APPROVAL_EMOTE_STR = "white_check_mark", REJECT_EMOTE_STR = "x";
	public static final String APPROVAL_EMOTE_UNICODE = "\u2705", REJECT_EMOTE_UNICODE = "\u274C";
	public static final String LOG_CHANNEL_ID = "625612507188559872", QUOTE_CHANNEL_ID = "599720754690392094";
	public static final String SERVER_ID = "565623426501443584";
	
	public static boolean isStaff(Member m)
	{
		for(Role r : m.getRoles())
		{
			//admin: 602889336748507164  mod: 565626094917648386
			if(r.getId().equals("602889336748507164") || r.getId().equals("565626094917648386"))
				return true;
		}
		return false;
	}
	
	
	public static void sendLog(String userID, String url)
	{
		/*
		 * This method sends a log to a channel so that quotes can be approved.
		 * This method assumes the message is a legitimate submission syntax-wise
		 */
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("New Quote Submission:");
		eb.setAuthor(main.Main.jda.getUserById(userID).getAsTag());
		eb.setDescription(userID + "\n" + url);
		eb.setImage(url);
		
		//send to log channel
		main.Main.jda.getTextChannelById(LOG_CHANNEL_ID).sendMessage(eb.build()).queue((log) ->
		{
			//emote: white_check_mark
			log.addReaction(APPROVAL_EMOTE_UNICODE).queue();
			//emote: x
			log.addReaction(REJECT_EMOTE_UNICODE).queue();
		});
	}
	
	public static Member getMemberByName(String name)
	{
		Guild guild = main.Main.jda.getGuildById(SERVER_ID);
		for(Member m : guild.getMembers())
		{
			if(m.getUser().isBot())
				continue;
			if(m.getNickname() != null && m.getNickname().startsWith(name))
				return m;
			if(m.getUser().getName().startsWith(name))
				return m;
		}
		
		return null;
	}
}
