package main;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.ApprovedQuote;
import utils.DataHandler;
import utils.Server;
import utils.UnapprovedQuote;

public class CommandListener extends ListenerAdapter
{
	private Random r;
	
	public CommandListener()
	{
		r = new Random();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] args = msg.split(" ");
		String userID = e.getAuthor().getId();
		
		if(!e.getGuild().getId().equals(Server.SERVER_ID)) //only wilbur's server
			return;
		
		if(args[0].equalsIgnoreCase(Main.PREFIX + "quote"))
		{
			//^quote [ping/name]
			if(args.length == 2 && e.getMessage().getAttachments().size() == 0)
			{
				String id;
				if(e.getMessage().getMentionedUsers().size() > 0 && args[1].matches("<@!\\d+>"))
					id = e.getMessage().getMentionedUsers().get(0).getId();
				else
				{
					Member m = utils.Server.getMemberByName(args[1]);
					if(m == null)
					{
						e.getChannel().sendMessage("Found no users by " + args[1]).queue();
						return;
					}
					id = m.getId();
				}
				
				//i.e. if a user is in the database
				List<ApprovedQuote> quotesbyuser = DataHandler.getQuotesByUser(id);
				if(quotesbyuser.size() > 0) //change to a list of quotes for a particular user
				{
					int quoteno = r.nextInt(quotesbyuser.size()) + 1;
					ApprovedQuote quote = DataHandler.getUserQuoteByIndex(quoteno, id);
					
					EmbedBuilder eb = new EmbedBuilder()
						.setTitle("Quote #" + quoteno + " by " + e.getGuild().getMemberById(id).getEffectiveName())
						.setColor(Server.EMBED_COL_INT)
						.setImage(quote.getQuote());
					e.getChannel().sendMessage(eb.build()).queue();
				}
				else
				{
					e.getChannel().sendMessage("No quotes found for " 
							+ e.getGuild().getMemberById(id).getEffectiveName() + "!").queue();
				}
			}
			else if(args.length >= 3 && args[2].matches("\\d+"))
			{
				//^quote [ping/user] [number]
				String id;
				if(e.getMessage().getMentionedUsers().size() > 0 && args[1].matches("<@!\\d+>"))
					id = e.getMessage().getMentionedUsers().get(0).getId();
				else
				{
					Member m = utils.Server.getMemberByName(args[1]);
					if(m == null)
					{
						e.getChannel().sendMessage("Found no users by " + args[1]).queue();
						return;
					}
					id = m.getId();
				}
				
				List<ApprovedQuote> quotesbyuser = DataHandler.getQuotesByUser(id);
				if(quotesbyuser.size() > 0)
				{
					if(!(args[2].matches("\\d+") && Integer.parseInt(args[2]) > 0 && Integer.parseInt(args[2]) <= quotesbyuser.size()))
					{
						e.getChannel().sendMessage("Quote out of range!").queue();
						return;
					}
					
					ApprovedQuote quote = DataHandler.getUserQuoteByIndex(Integer.parseInt(args[2]), id);
					EmbedBuilder eb = new EmbedBuilder()
							.setTitle("Quote #" + args[2] + " by " + e.getGuild().getMemberById(id).getEffectiveName())
							.setColor(Server.EMBED_COL_INT)
							.setImage(quote.getQuote());
					e.getChannel().sendMessage(eb.build()).queue();
				}
				else
				{
					e.getChannel().sendMessage("No quotes found for " 
							+ e.getGuild().getMemberById(id).getEffectiveName() + "!").queue();
				}
			}
			else
			{
				if(!(e.getMessage().getMentionedMembers().size() == 1 && args[1].matches("<@!\\d+>")))
				{
					e.getChannel().sendMessage("No user specified!").queue();
					return;
				}
				
				//^quote [ping] [url]		
				String id = e.getMessage().getMentionedUsers().get(0).getId();
				
				for(int i = 2; i < args.length; i++)
				{
					//img url regex
					if(args[i].matches("http(s?):\\/\\/.*(\\.png|\\.jpeg|\\.jpg|\\.JPG|\\.PNG)"))
					{
						if(utils.Server.isStaff(e.getMember()))
						{
							Main.checkedquotes.add(new ApprovedQuote(new UnapprovedQuote(id, args[i])));
							
							EmbedBuilder quotePost = new EmbedBuilder()
								.setTitle("New Quote!")
								.setColor(Server.EMBED_COL_INT)
								.setDescription("Check out this quote of "
									+ e.getGuild().getMemberById(id).getEffectiveName() + "!")
								.setImage(args[i]);
							
							Main.jda.getTextChannelById(Server.QUOTE_CHANNEL_ID).sendMessage(quotePost.build()).queue();
							e.getChannel().sendMessage("Quote posted!").queue();
						}
						else
						{
							Main.uncheckedquotes.add(new UnapprovedQuote(id, args[i]));
							utils.Server.sendLog(userID, args[i]);
							e.getChannel().sendMessage("Your quote has been submitted!").queue();
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
								String imgurl = a.getUrl();
								if(utils.Server.isStaff(e.getMember()))
								{
									Main.checkedquotes.add(new ApprovedQuote(DataHandler.getUnapprovedQuoteByUrl(imgurl)));
									EmbedBuilder quotePost = new EmbedBuilder()
										.setTitle("New Quote!")
										.setColor(Server.EMBED_COL_INT)
										.setDescription("Check out this quote of "
											+ e.getGuild().getMemberById(id).getEffectiveName() + "!")
										.setImage(imgurl);
									
									Main.jda.getTextChannelById(Server.QUOTE_CHANNEL_ID).sendMessage(quotePost.build()).queue();
									e.getChannel().sendMessage("Quote posted!").queue();
								}
								else
								{
									Main.uncheckedquotes.add(new UnapprovedQuote(id, imgurl));
									utils.Server.sendLog(userID, imgurl);
									e.getChannel().sendMessage("Your quote has been submitted!").queue();
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
			EmbedBuilder eb = new EmbedBuilder()
			.setTitle("**Quotes Help:**")
			.setColor(Server.EMBED_COL_INT)
			.setDescription("**" + Main.PREFIX + "quote [ping the person being quoted] [image/image url]: **"
					+ "submits a quote by the pinged user- images can be attached instead of using"
					+ "image urls, and you can submit multiple quotes for a user at a time by attaching"
					+ "more images or adding more image urls to the end of the command")
			
				.appendDescription("\n\n**" + Main.PREFIX + "quote [ping someone or type the first part of a person's name]: **"
					+ "get a random quote from the specified person")
				
				.appendDescription("\n\n**" + Main.PREFIX + "quote [ping someone or type the first part of a person's name] [number]: **"
					+ "get a specific quote from the specified person")
				
				.appendDescription("\n\n**" + Main.PREFIX + "quoteshelp: **"
					+ "displays this message")
				
				.appendDescription("\n\n**" + Main.PREFIX + "disablequotes: **"
					+ "(staff only) disables the quotes feature");
			e.getChannel().sendMessage(eb.build()).queue();
			
			return;
		}
		
		if(utils.Server.isStaff(e.getMember()) && args[0].equalsIgnoreCase(Main.PREFIX + "disable") && args[1].equalsIgnoreCase("quotes"))
		{
			try
			{
				DataHandler.writeData();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			e.getChannel().sendMessage("*quotes feature disabled-- ask al if you want it back up*").complete();
			Main.jda.shutdown();
			System.exit(0);
		}
	}
}
