package com.javath.settrade.flash;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.html.FormFilter;
import com.javath.html.HtmlParser;
import com.javath.html.ParamFilter;
import com.javath.http.Browser;
import com.javath.http.Form;
import com.javath.http.Response;
import com.javath.set.Broker;
import com.javath.util.Assign;
import com.javath.util.ObjectException;

public abstract class BrokerStreaming extends Broker {
	
	protected String account_no;
	protected String account_type;
	protected String pin = "000000";
	 
	private Map<String,String> flashVars;
	
	protected CookieStore login(Browser browser, String[] login_request) {
		Response response = null;
		Form form = null;
		HtmlParser parser = new HtmlParser(null);
		FormFilter formFilter = new FormFilter(null);
		// Login process
		for (int step = 0; step < login_request.length; step++) {
			browser.address(login_request[step]);
			if (form == null) {
				response = browser.get();
			} else {
				browser.body(form);
				response = browser.post();
			}
			// 
			String message = String.format("Login step-%d/%d: %d \"%s\"",
					step, login_request.length - 1, response.getStatusCode(), response.getReasonPhrase());
			switch (response.getStatusCode()) {
			case 200:
				FINE(message);
				break;
			case 302:
				WARNING(message);
				Header[] headers = response.getLocations();
				if (headers.length == 1) {
					try {
						String fail_message = getParameter(headers[0].getValue(),"txtFailMsg").replace('+', ' ');
						throw new ObjectException(String.format("username=\"%s\", %s", username, fail_message));
					} catch (NullPointerException e) {} // find "txtFailMsg" not found in Header "Location:"
					//if (fail_message.equals("Login+attempt+exceeds+quota+and+account+is+locked.+Please+contact+your+broker."))
					//	;
				} else {
					throw new ObjectException("\"Location:\" has %d headers", headers.length);
				}
				break;
			default:
				SEVERE(message);
				return browser.getCookie();
			}
			parser.setInputStream(response.getContent());
			formFilter.setNode(parser.parse()).filter();
			if ((step + 1) == login_request.length) {
				INFO("Authentication success");
			} else {
				if (step == 0)
					form = buildForm(formFilter.action(login_request[step + 1]));
				else
					form = formFilter.actionForm(login_request[step + 1]);
			}
		}
		return browser.getCookie();
	}
	private String getParameter(String uri, String name) {
		int begin = uri.indexOf(name + "=");
		if (begin != -1) {
			begin = begin + name.length() + 1;
			int end = uri.indexOf('&',begin);
			if (end == -1)
				return uri.substring(begin);
			else
				return uri.substring(begin, end);
		} else
			return null;
	}
	protected abstract Form buildForm(Node node);
	protected abstract void loadFlashVars();
	protected void loadFlashVars(Browser browser, String url) {
		HtmlParser parser = new HtmlParser(null);
		browser.address(url);
		//browser.setCookie(cookie.getResource());
		Response response = browser.get();
		parser.setInputStream(response.getContent());
		if (response.getStatusCode() != 200) {
			if (response.getStatusCode() == 404) {
				//** Function search "txtMsg" in URI 
				String uri = browser.getRequestLine().getUri();
				int begin = uri.indexOf("txtMsg=");
				int end = uri.indexOf('&',begin);
				if (end == -1)
					uri = uri.substring(begin + 7);
				else
					uri = uri.substring(begin + 7, end);
				if (uri.equals("Settrade+Cookies+can%27t+be+found")) {
					WARNING("Settrade Cookies can't be found. Please call method \"authentication()\" before");
					//** Login Process
					authentication(browser, cookie);
					loadFlashVars(browser, url);
					return;
					/**/
				}
				/**/
			}		
			throw new ObjectException(String.format("%s \"%s\"", 
					response.getStatusCode(), response.getReasonPhrase()));
		}
		ParamFilter paramFilter = new ParamFilter(parser.parse());
		paramFilter.filter();
		// 
		NamedNodeMap attributes = paramFilter.name("FlashVars").getAttributes();
		for (int index = 0; index < attributes.getLength(); index++) {
			if (attributes.item(index).getNodeName().equals("value"))
				setFlashVars(attributes.item(index).getNodeValue().split("[&]"));
		}		
	}
	protected void setFlashVars(String[] flashVars) {
		this.flashVars = new HashMap<String,String>();
		for (int index = 0; index < flashVars.length; index++) {
			String[] vars = flashVars[index].split("[=]");
			if (vars.length == 1)
				this.flashVars.put(vars[0],"");
			else
				this.flashVars.put(vars[0],vars[1]);
		}
		account_no = getAccountNo();
		account_type = getAccountType();
	}
	public String getFlashVar(String name) {
		try {
			return flashVars.get(name);
		} catch (NullPointerException e) {
			WARNING("flashVars is %s. Please call method \"loadFlashVars()\" before", e.getMessage());
			loadFlashVars();
			return getFlashVar(name);
		}
	}
	public void printFlashVars() {
		try {
			for (Iterator<String> iterator = flashVars.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				System.out.printf("%s=%s%n",key,flashVars.get(key));
			}
		} catch (NullPointerException e) {
			WARNING("flashVars is %s. Please call method \"loadFlashVars()\" before", e.getMessage());
			loadFlashVars();
			printFlashVars();
		}
	}
	// Information flashVars in name = "fvAccountInfoList"
	private String getAccountNo() {
		DataProvider data = new DataProvider(); 
		data.read(getFlashVar("fvAccountInfoList"));
		try {
			return data.get(0, 0, 2);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	private String getAccountType() {
		DataProvider data = new DataProvider(); 
		data.read(getFlashVar("fvAccountInfoList"));
		try {
			return data.get(2, 0, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	protected String fvHttpLink(String fvHost, String fvPath) {
		return hyperlink("http://", getFlashVar(fvHost), getFlashVar(fvPath)); 
	}
	protected String fvHttpsLink(String fvHost, String fvPath) {
		return hyperlink("https://", getFlashVar(fvHost), getFlashVar(fvPath)); 
	}
	protected String hyperlink(String hostname, String path) {
		return hostname + path; 
	}
	protected String hyperlink(String scheme, String hostname, String path) {
		return scheme + hostname + path; 
	}
	
	public long synctime() {
		return synctime("fvPrimaryHost");
	}
	protected long synctime(String fvHostName) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		long time = new Date().getTime();
		try{ 
			//browser.address("https://pushctw1.settrade.com/realtime/streaming4/synctime.jsp");
			browser.address(fvHttpsLink(fvHostName, "fvSyncTimeServlet"));
			Response response = browser.get();
			if (response.getStatusCode() != 200) {
				SEVERE("HTTP Status %s \"%s\"",response.getStatusCode(), response.getReasonPhrase());
				return 0;
			}
			DataProvider dataProvider = new DataProvider().read(response.getContent());
			long result = Long.valueOf(dataProvider.get(0,0,2)) - time ;
			INFO("Adjust time Settrade server offset %.3f sec", result/1000.0d);
			//logger.finest(message.toString());
			return result;
		} finally {
			Assign.returnObject(browser);
		}
	}
	
	protected DataProvider seos(Form form) {
		// browser httpContext
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			//browser.address("https://click2win.settrade.com/daytradeflex/streamingSeos.jsp");
			browser.address(hyperlink(getFlashVar("fvITPHost"), "/daytradeflex/streamingSeos.jsp"));
			browser.body(form);
			Response response = browser.post();
			
			DataProvider dataProvider = null;
			try {
				dataProvider = new DataProvider().read(response.getContent());
			} catch (ObjectException e) {
				if (e.getMessage().equals("Unauthorized Access.")) {
					WARNING(e.getMessage());
					authentication(browser, cookie);
					dataProvider = seos(form);
				} else
					throw e;
			}
			return dataProvider;
		} finally {
			Assign.returnObject(browser);
		}
	}
	protected DataProvider seos(String service) {
		Form form = new Form();
		form.add("Service", service);
		//form.add("txtAccountNo", "");
		form.add("txtAccountNo", account_no);
		form.add("NewMode", "Pull");
		//form.add("txtAccountType", "");
		form.add("txtAccountType", account_type);
		return seos(form);
	}
	public DataProvider orderStatus() {
		return seos("OrderStatus");
	}
	public DataProvider accountInfo() {
		return seos("AccountInfo");
	}
	public DataProvider portfolio() {
		return seos("Portfolio");
	}
	protected DataProvider placeOrder(String symbol, String order, double price, long volue) {
		Form form = new Form();
		//FormEntity formEntity = new FormEntity(null);
		form.add("txtTerminalType", "streaming");
		form.add("type", "place");
		form.add("positionType", "");
		form.add("txtClientType", "");
		form.add("txtNvdr", "");
		form.add("txtBorS", order);
		form.add("txtPublishVol", "");
		form.add("txtSymbol", symbol);
		//form.add("txtAccountNo", ""); // AccountNo
		form.add("txtAccountNo", account_no); // AccountNo
		form.add("txtQty", String.valueOf(volue));
		form.add("Service", "PlaceOrder");
		form.add("txtPrice", String.valueOf(price));
		form.add("txtCondition", "DAY");
		form.add("txtPIN_new", pin); // Pin
		form.add("confirmedWarn", "");
		form.add("txtOrderNo", "");
		form.add("txtPriceType", "limit");
		return seos(form);
	}
	public DataProvider buyOrder(String symbol, double price, long volume) {
		return placeOrder(symbol, "B", price, volume);
	}
	public DataProvider sellOrder(String symbol, double price, long volume) {
		return placeOrder(symbol, "S", price, volume);
	}
	public DataProvider cancelOrder(String symbol, String orderNo) {
		Form form = new Form();
		form.add("txtCancelSymbol", symbol);
		form.add("type", "cancel");
		form.add("Service", "PlaceOrder");
		form.add("txtTerminalType", "streaming");
		form.add("positionType", "");
		form.add("txtBorS", "");
		form.add("txtNewATOATC", "");
		form.add("extOrderNo", null);
		form.add("txtClientType", "");
		//form.add("txtAccountNo", getAccountNo()); // AccountNo
		form.add("txtAccountNo", account_no); // AccountNo
		form.add("txtPrice", "");
		form.add("txtNvdr", "");
		form.add("txtPIN_new", pin); // Pin
		form.add("txtQty", "");
		form.add("txtSymbol", "");
		form.add("txtOrderNo", orderNo);
		return seos(form);
	}
	protected DataProvider dataProvider(Form form) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			browser.address(fvHttpsLink("fvPrimaryHost","fvDataStrServlet"));
			browser.body(form);
			Response response = browser.post();
			DataProvider dataProvider = new DataProvider().read(response.getContent());
			return dataProvider;
		} finally {
			Assign.returnObject(browser);
		}
	}

}
