package main;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter
{
	private Connection con;
	private Random r;
	
	public CommandListener()
	{
		con = utils.DataHandler.getConnection();
		r = new Random();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] arguments = msg.split(" ");
		String userID = e.getAuthor().getId();
		
		if(arguments[0].equalsIgnoreCase(Main.PREFIX + "quote"))
		{
			//^quote [ping/name]
			if(arguments.length == 2 && e.getMessage().getAttachments().size() == 0)
			{
				try
				{
					String id;
					if(e.getMessage().getMentionedUsers().size() > 0 && arguments[1].matches("<@!\\d+>"))
						id = e.getMessage().getMentionedUsers().get(0).getId();
					else
					{
						Member m = utils.Server.getMemberByName(arguments[1]);
						if(m == null)
						{
							e.getChannel().sendMessage("Found no users by " + arguments[1]).queue();
							return;
						}
						id = m.getId();
					}
					
					int size = utils.DataHandler.getResultSize("select quote from Wilbur_approvedquotes where discord_id = \"" + id + "\"");
					//i.e. if a user is in the database
					if(size != 0)
					{
						int quoteno = r.nextInt(size) + 1;
						String quote = utils.DataHandler.getQuoteByIndex(quoteno, id);
						
						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle("Quote #" + quoteno + " by " + e.getGuild().getMemberById(id).getEffectiveName());
						eb.setImage(quote);
						e.getChannel().sendMessage(eb.build()).queue();
					}
					else
					{
						e.getChannel().sendMessage("No quotes found for " 
								+ e.getGuild().getMemberById(id).getEffectiveName() + "!").queue();
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
				
			}
			else if(arguments.length >= 3 && arguments[2].matches("\\d+"))
			{
				//^quote [ping/user] [number]
				String id;
				if(e.getMessage().getMentionedUsers().size() > 0 && arguments[1].matches("<@!\\d+>"))
					id = e.getMessage().getMentionedUsers().get(0).getId();
				else
				{
					Member m = utils.Server.getMemberByName(arguments[1]);
					if(m == null)
					{
						e.getChannel().sendMessage("Found no users by " + arguments[1]).queue();
						return;
					}
					id = m.getId();
				}
				
				try
				{
					int size = utils.DataHandler.getResultSize("select quote from Wilbur_approvedquotes where discord_id = \"" + id + "\"");
					if(size != 0)
					{
						if(!(Integer.parseInt(arguments[2]) > 0 && Integer.parseInt(arguments[2]) <= size))
						{
							e.getChannel().sendMessage("Quote out of range!").queue();
							return;
						}
						
						String quote = utils.DataHandler.getQuoteByIndex(Integer.parseInt(arguments[2]), id);
						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle("Quote #" + arguments[2] + " by " + e.getGuild().getMemberById(id).getEffectiveName());
						eb.setImage(quote);
						e.getChannel().sendMessage(eb.build()).queue();
					}
					else
					{
						e.getChannel().sendMessage("No quotes found for " 
								+ e.getGuild().getMemberById(id).getEffectiveName() + "!").queue();
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
			}
			else
			{
				if(!(e.getMessage().getMentionedMembers().size() == 1 && arguments[1].matches("<@!\\d+>")))
				{
					e.getChannel().sendMessage("No user specified!").queue();
					return;
				}
				
				//^quote [ping] [url]		
				String id = e.getMessage().getMentionedUsers().get(0).getId();
				
				for(int i = 2; i < arguments.length; i++)
				{
					//img url regex
					if(arguments[i].matches("http(s?):\\/\\/.*(\\.png|\\.jpeg|\\.jpg|\\.JPG|\\.PNG)"))
					{
						try
						{
							Statement s = con.createStatement();
							if(utils.Server.isStaff(e.getMember()))
							{
								s.executeUpdate("insert into Wilbur_approvedquotes (discord_id, quote) values (\""
										+ id + "\", \"" + arguments[i] + "\")");
								
								EmbedBuilder quotePost = new EmbedBuilder();
								quotePost.setTitle("New Quote!");
								quotePost.setDescription("Check out this quote of "
										+ e.getGuild().getMemberById(id).getEffectiveName() + "!");
								quotePost.setImage(arguments[i]);
								Main.jda.getTextChannelById("599720754690392094").sendMessage(quotePost.build()).queue();
							}
							else
							{
								s.executeUpdate("insert into Wilbur_pendingquotes (discord_id, quote) values (\""
										+ id + "\", \"" + arguments[i] + "\")");
								
								utils.Server.sendLog(userID, arguments[i]);
								e.getChannel().sendMessage("Your quote has been submitted!").queue();
							}	
						}
						catch(SQLException e1)
						{
							e1.printStackTrace();
						}
					}
					else
						e.getChannel().sendMessage("Invalid image!").queue();
				}
					
					
					//attached images
					if(e.getMessage().getAttachments().size() > 0)
					{
						for(Attachment a : e.getMessage().getAttachments())
						{
							if(a.isImage())
							{
								try
								{
									String imgurl = a.getUrl();
									Statement s = con.createStatement();
									if(utils.Server.isStaff(e.getMember()))
									{
										s.executeUpdate("insert into Wilbur_approvedquotes (discord_id, quote) values (\""
												+ id + "\", \"" + imgurl + "\")");
										
										EmbedBuilder quotePost = new EmbedBuilder();
										quotePost.setTitle("New Quote!");
										quotePost.setDescription("Check out this quote of "
												+ e.getGuild().getMemberById(id).getEffectiveName() + "!");
										quotePost.setImage(imgurl);
										Main.jda.getTextChannelById("599720754690392094").sendMessage(quotePost.build()).queue();
										
									}
									else
									{
										s.executeUpdate("insert into Wilbur_pendingquotes (discord_id, quote) values (\""
												+ id + "\", \"" + imgurl + "\")");
										
										utils.Server.sendLog(userID, imgurl);
										e.getChannel().sendMessage("Your quote has been submitted!").queue();
									}	
								}
								catch(SQLException e1)
								{
									e1.printStackTrace();
								}
							}
							else
								e.getChannel().sendMessage("Invalid image!").queue();
						}
					}
				}
			
			return;
		}
		
		//^quoteshelp
		if(msg.equalsIgnoreCase(Main.PREFIX + "quoteshelp"))
		{
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("**Quotes Help:**");
			eb.setDescription("**" + Main.PREFIX + "quote [ping the person being quoted] [image/image url]: **"
					+ "submits a quote by the pinged user- images can be attached instead of using"
					+ "image urls, and you can submit multiple quotes for a user at a time by attaching"
					+ "more images or adding more image urls to the end of the command");
			eb.appendDescription("\n\n**" + Main.PREFIX + "quote [ping someone or type the first part of a person's name]: **"
					+ "get a random quote from the specified person");
			eb.appendDescription("\n\n**" + Main.PREFIX + "quote [ping someone or type the first part of a person's name] [number]: **"
					+ "get a specific quote from the specified person");
			eb.appendDescription("\n\n**" + Main.PREFIX + "quoteshelp: **"
					+ "displays this message");
			eb.appendDescription("\n\n**" + Main.PREFIX + "disablequotes: **"
					+ "(staff only) disables the quotes feature");
			e.getChannel().sendMessage(eb.build()).queue();
			
			return;
		}
		
		if(msg.equalsIgnoreCase(Main.PREFIX + "disablequotes") && utils.Server.isStaff(e.getMember()))
		{
			e.getChannel().sendMessage("*quotes feature disabled-- ask al if you want it back up*").queue();
			Main.jda.shutdownNow();
			System.exit(0);
		}
	}
}
