package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import main.Main;

public class DataHandler
{
	static final String FILE_LOC = "";
	
	//get a list of all the quotes a certain user has said (that have been approved)
	public static List<ApprovedQuote> getQuotesByUser(String userid)
	{
		List<ApprovedQuote> userquotes = new ArrayList<ApprovedQuote>();
		for(ApprovedQuote quote : Main.checkedquotes)
		{
			if(quote.getId().equals(userid))
				userquotes.add(quote);
		}
		
		return userquotes;
	}
	
	//get a quote from a user, and the number is for example, the fourth quote that user has said
	public static ApprovedQuote getUserQuoteByIndex(int userindex, String userid)
	{
		for(ApprovedQuote quote : Main.checkedquotes)
		{
			if(quote.getId().equals(userid) && quote.getUserIndex() == userindex)
				return quote;
		}
		
		return null;
	}
	
	//find an unapprovedquote in the system from its quote url
	public static UnapprovedQuote getUnapprovedQuoteByUrl(String url)
	{
		for(UnapprovedQuote quote : Main.uncheckedquotes)
		{
			if(quote.getQuote().equals(url))
				return quote;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void readData() throws FileNotFoundException, IOException, ParseException
	{
		JSONParser jsp = new JSONParser();
		JSONObject file_obj = (JSONObject) jsp.parse(new BufferedReader(new FileReader(new File(FILE_LOC))));
		
		//add unapproved quotes
		JSONArray unapproved = (JSONArray) file_obj.get("unapproved");
		unapproved.forEach((quote) ->
		{
			JSONObject quote_obj = (JSONObject) quote;
			Main.uncheckedquotes.add(new UnapprovedQuote((String) quote_obj.get("id"), (String) quote_obj.get("quote")));
		});
		
		//add approved quotes
		JSONArray approved = (JSONArray) file_obj.get("approved");
		approved.forEach((quote) ->
		{
			JSONObject quote_obj = (JSONObject) quote;
			Main.checkedquotes.add(new ApprovedQuote((String) quote_obj.get("id"),
					(String) quote_obj.get("quote"),
					(long) quote_obj.get("quoteid"),
					(int) quote_obj.get("userindex")));
		});
		
		//add currentquoteid
		ApprovedQuote.currentquoteid = (long) file_obj.get("currentquoteid");
	}
	
	@SuppressWarnings("unchecked")
	public static void writeData() throws IOException
	{
		JSONObject file_obj = new JSONObject();
		JSONArray approved_arr = new JSONArray();
		JSONArray unapproved_arr = new JSONArray();
		
		//write unapproved quotes
		for(UnapprovedQuote quote : Main.uncheckedquotes)
		{
			JSONObject quote_obj = new JSONObject();
			quote_obj.put("id", quote.getId());
			quote_obj.put("quote", quote.getQuote());
			
			unapproved_arr.add(quote_obj);
		}
		
		//write approved quotes
		for(ApprovedQuote quote : Main.checkedquotes)
		{
			JSONObject quote_obj = new JSONObject();
			quote_obj.put("id", quote.getId());
			quote_obj.put("quote", quote.getQuote());
			quote_obj.put("quoteid", quote.getQuoteId());
			quote_obj.put("userindex", quote.getUserIndex());
			
			approved_arr.add(quote_obj);
		}
		
		//save all the info
		file_obj.put("currentquoteid", ApprovedQuote.currentquoteid);
		file_obj.put("unapproved", unapproved_arr);
		file_obj.put("approved", approved_arr);
		
		file_obj.writeJSONString(new PrintWriter(new FileWriter(new File(FILE_LOC))));
	}
}
