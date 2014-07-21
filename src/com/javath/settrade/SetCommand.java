package com.javath.settrade;

import java.util.ArrayList;

import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.util.Assign;
import com.javath.util.Instance;
import com.javath.util.NotificationAdaptor;
import com.mysql.jdbc.Buffer;

public class SetCommand extends Instance implements Runnable {
	
	private final Cookie cookie;
	private final NotificationAdaptor note;
	
	public SetCommand() {
		cookie = new Cookie();
		note = new NotificationAdaptor(this);
	}
	
	private Response getWebPage() {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			browser.address(getURI());
			return browser.get();
		} finally {
			Assign.returnObject(browser);
		}
	}
	private String getURI() {
		return "http://www.settrade.com/C13_MarketSummarySET.jsp?command=SET50";
	}

	@Override
	public void run() {
		Response response = getWebPage();
		response.printHeaders();
		parser(response);
		
	}
	
	private String[] parser(Response response) {
		ArrayList<String> result = new ArrayList<String>();
		HtmlParser parser = new HtmlParser(response.getContent(), response.getCharset());
		TextNode text = new TextNode(parser.parse());
		text.print();
		boolean header = false;
		boolean seek = false;
		for (int index = 0; index < text.length(); index++) {
			if (header) {
				
			} else
				if (seek)
					if (text.getString(index, 0).equals("6"))
						;
					
				else if (text.getString(index, 0).equals("5"))
					seek = true;
		}
		return result.toArray(new String[] {});
	}

	public static void main(String[] args) {
		SetCommand command = new SetCommand();
		command.run();
	}
}
