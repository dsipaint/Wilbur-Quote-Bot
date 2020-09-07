package utils;

public class ApprovedQuote extends UnapprovedQuote
{
	private long quoteid;
	private int userindex; //number quote of user i.e. azure's 3rd quote
	
	public static long currentquoteid;
	
	public ApprovedQuote(String id, String quote, long quoteid, int userindex)
	{
		super(id, quote);
		this.quoteid = quoteid;
		this.userindex = userindex;
	}
	
	public ApprovedQuote(UnapprovedQuote quote)
	{
		super(quote.getId(), quote.getQuote());
		this.quoteid = currentquoteid++;
		this.userindex = DataHandler.getQuotesByUser(quote.getId()).size() + 1;
	}
	
	public long getQuoteId()
	{
		return this.quoteid;
	}
	
	public int getUserIndex()
	{
		return this.userindex;
	}
}
