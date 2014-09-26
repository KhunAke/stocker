package com.javath.settrade;

import java.util.List;

import org.apache.http.client.CookieStore;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.html.InputFilter;
import com.javath.http.Browser;
import com.javath.http.Form;
import com.javath.set.Broker;
import com.javath.settrade.flash.BrokerStreaming;
import com.javath.util.Assign;

public class Click2Win extends BrokerStreaming {
	
	private static final int EXTEND_ID;
	private final static Assign assign;
	private final static String[] login_request;
	private final static String streaming_page;
	
	static {
		String classname = Assign.classname();
		EXTEND_ID = getExtendId(classname);
		String default_Properties = Assign.etc + Assign.File_Separator +
				"streaming" + Assign.File_Separator +
				"click2win.properties";
		assign = Assign.getInstance(classname, default_Properties);
		int length = (int) assign.getLongProperty("login_request.length", 0);
		login_request = new String[length];
		for (int index = 0; index < length; index++) {
			login_request[index] = 
					assign.getProperty(String.format("login_request[%d]",index));
		}
		streaming_page = assign.getProperty("streaming_page",
				"https://click2win.settrade.com/realtime/streaming4/flash/Streaming4Screen.jsp");
	}
	
	public static Click2Win getInstance(String username, String password) {
		Broker broker = Broker.getBroker(username, EXTEND_ID);
		try {
			if (broker.checkPassword(password))
				return (Click2Win) broker;
			else
				return new Click2Win(username, password);
		} catch (NullPointerException e) {
			return new Click2Win(username, password);
		}
	}
	
	private Click2Win(String username, String password) {
		initBroker(username, password);
	}
	
	protected int getExtendId() {
		return EXTEND_ID;
	}
	public void login() {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			login(browser);
			loadFlashVars(browser, streaming_page);
		} finally {
			Assign.returnObject(browser);
		}
	}
	public CookieStore login(Browser browser) {
		return login(browser, login_request);
	}
	protected Form buildForm(Node node) {
		//HtmlParser.print(node);
		Form form = new Form();
		if (node == null)
			return form;
		InputFilter inputFilter = new InputFilter(node);
		List<Node> inputs = inputFilter.filter();
		//inputFilter.print();
		
		for (int index = 0; index < inputs.size(); index++) {
			Node input = inputs.get(index);
			NamedNodeMap attributes = input.getAttributes();
			String name = null;
			String value = null;
			for (int item = 0; item < attributes.getLength(); item++) {
				Node attribute = attributes.item(item);
				if (attribute.getNodeName().equals("name"))
					name = attribute.getNodeValue();
				else if (attribute.getNodeName().equals("value")) 
					value = attribute.getNodeValue();
			}
			try {
				if (name.equals("txtLogin"))
					form.add(name, this.username);
				else if (name.equals("txtPassword")) {
					form.add(name, this.password);
				//} else if (name.equals("imageField")) {
				//	Random random = new Random();
				//	form.add("imageField.x", String.valueOf(random.nextInt(56)));
				//	form.add("imageField.y", String.valueOf(random.nextInt(13)));
				} else
					form.add(name, value);
			} catch (java.lang.NullPointerException e) {
				// variable name is null
				// logger.warning(message(HtmlParser.node(input)));
			}
		}
		return form;
	}
	protected void loadFlashVars() {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			loadFlashVars(browser, streaming_page);
		} finally {
			Assign.returnObject(browser);
		}
	}
	
	public long buy(String symbol, double price, long volume) {
		return 0;
	}
	public long sell(String symbol, double price, long volume) {
		return 0;
	}
	public boolean cancel(String symbol, long order_no) {
		return false;
	}

}
