package utils;

public class UnapprovedQuote
{
	private String id, quote;
	
	public UnapprovedQuote(String id, String quote)
	{
		this.id = id;
		this.quote = quote;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getQuote()
	{
		return this.quote;
	}
}
