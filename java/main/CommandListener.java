package main;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.DataHandler;
import utils.Server;

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
		String[] args = msg.split(" ");
		
		if(args[0].equalsIgnoreCase(Main.PREFIX + "quote"))
		{
			if(args.length == 1)
				return;
			
			//^quote [ping/id]
			if(args.length == 2 && e.getMessage().getAttachments().size() == 0)
			{
				try
				{
					String id;
					if(e.getMessage().getMentionedUsers().size() > 0 && args[1].matches("<@!\\d{18}>"))
						id = e.getMessage().getMentionedUsers().get(0).getId();
					else if(args[1].matches("\\d{18}") && e.getGuild().getMemberById(args[1]) != null)
						id = args[1];
					else
					{
						e.getChannel().sendMessage("Could not find user").queue();
						return;
					}
					
					int size = utils.DataHandler.getResultSize("select quote from Wilbur_approvedquotes where userid = \"" + id + "\"");
					//i.e. if a user is in the database
					if(size != 0)
					{
						int quoteno = r.nextInt(size) + 1;
						String quote = utils.DataHandler.getQuoteByIndex(quoteno, id);
						
						EmbedBuilder eb = new EmbedBuilder()
								.setTitle("Quote #" + quoteno + " by " + e.getGuild().getMemberById(id).getEffectiveName())
							.setImage(quote)
							.setColor(Server.EMBED_COL_INT);
						
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
			else if(args.length >= 3 && args[2].matches("\\d+"))
			{
				//^quote [ping/id] [number]
				String id;
				if(e.getMessage().getMentionedUsers().size() > 0 && args[1].matches("<@!\\d+>"))
					id = e.getMessage().getMentionedUsers().get(0).getId();
				else if(args[1].matches("\\d{18}") && e.getGuild().getMemberById(args[1]) != null)
						id = args[1];
				else
				{
					e.getChannel().sendMessage("Could not find user").queue();
					return;
				}
				
				try
				{
					int size = utils.DataHandler.getResultSize("select quote from Wilbur_approvedquotes where userid = \"" + id + "\"");
					if(size != 0)
					{
						if(!(Integer.parseInt(args[2]) > 0 && Integer.parseInt(args[2]) <= size))
						{
							e.getChannel().sendMessage("Quote out of range!").queue();
							return;
						}
						
						String quote = utils.DataHandler.getQuoteByIndex(Integer.parseInt(args[2]), id);
						EmbedBuilder eb = new EmbedBuilder()
							.setTitle("Quote #" + args[2] + " by " + e.getGuild().getMemberById(id).getEffectiveName())
							.setImage(quote)
							.setColor(Server.EMBED_COL_INT);
						
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
				//^quote [ping] [url]
				
				String id;
				if(e.getMessage().getMentionedUsers().size() > 0 && args[1].matches("<@\\!\\d{18}>"))
					id = e.getMessage().getMentionedUsers().get(0).getId();
				else if(args[1].matches("\\d{18}") && e.getGuild().getMemberById(args[1]) != null)
						id = args[1];
				else
				{
					e.getChannel().sendMessage("Could not find user").queue();
					return;
				}
				
				if(id.equals(e.getAuthor().getId()))
				{
					e.getChannel().sendMessage("You can't quote yourself!").queue();
					return;
				}
								
				for(int i = 2; i < args.length; i++)
				{
					//img url regex
					if(args[i].matches("http(s?):\\/\\/.*(\\.png|\\.jpeg|\\.jpg|\\.JPG|\\.PNG)"))
					{						
						try
						{
							Statement s = con.createStatement();
							if(utils.Server.isStaff(e.getMember()))
							{
								s.executeUpdate("insert into Wilbur_approvedquotes (userid, quote, userindex) values (\""
										+ id + "\", \"" + args[i] + "\", " + (DataHandler.getResultSize("select * from Wilbur_approvedquotes where userid = \"" + id + "\"") + 1)
										+ ")");
								
								Server.sendQuote(id, args[i]);
								e.getChannel().sendMessage("Quote posted!").queue();
							}
							else
							{
								s.executeUpdate("insert into Wilbur_pendingquotes (userid, quote) values (\""
										+ id + "\", \"" + args[i] + "\")");
								
								utils.Server.sendApproval(id, args[i], e.getAuthor());
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
										s.executeUpdate("insert into Wilbur_approvedquotes (userid, quote, userindex) values (\""
												+ id + "\", \"" + imgurl + "\"," 
												+ (DataHandler.getResultSize("select * from Wilbur_approvedquotes where userid = \"" + id + "\"") + 1) + ")");
										
										Server.sendQuote(id, imgurl);
										e.getChannel().sendMessage("Quote posted!").queue();
									}
									else
									{
										s.executeUpdate("insert into Wilbur_pendingquotes (userid, quote) values (\""
												+ id + "\", \"" + imgurl + "\")");
										
										utils.Server.sendApproval(id, imgurl, e.getAuthor());
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
		
		if(args[0].equalsIgnoreCase(Main.PREFIX + "quoteshelp"))
		{
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("**Quotes Help:**")
					.setColor(Server.EMBED_COL_INT)
					.setDescription("**" + Main.PREFIX + "quote [ping the person being quoted] [image/image url]: **"
							+ "submits a quote by the pinged user- images can be attached instead of using"
							+ " image urls, and you can submit multiple quotes for a user at a time by attaching"
							+ " more images or adding more image urls to the end of the command")
					
						.appendDescription("\n\n**" + Main.PREFIX + "quote [ping/id]: **"
							+ "get a random quote from the specified person")
						
						.appendDescription("\n\n**" + Main.PREFIX + "quote [ping/id] [number]: **"
							+ "get a specific quote from the specified person")
						
						.appendDescription("\n\n**" + Main.PREFIX + "quoteshelp: **"
							+ "displays this message")
						
						.appendDescription("\n\n**" + Main.PREFIX + "disable quotes: **"
							+ "(staff only) disables the quotes feature");
					e.getChannel().sendMessage(eb.build()).queue();
					
					return;
		}
		
		if(args[0].equalsIgnoreCase(Main.PREFIX + "disable") && args[1].equalsIgnoreCase("quotes") && utils.Server.isStaff(e.getMember()))
		{
			try
			{
				con.close();
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
			e.getChannel().sendMessage("*quotes feature disabled-- ask al if you want it back up*").complete();
			Main.jda.shutdownNow();
			System.exit(0);
		}
	}
}
