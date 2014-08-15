package com.javath.settrade;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.javath.html.FormFilter;
import com.javath.html.HtmlParser;
import com.javath.html.InputFilter;
import com.javath.html.ParamFilter;
import com.javath.http.Cookie;
import com.javath.http.Form;
import com.javath.http.Browser;
import com.javath.http.Response;
import com.javath.logger.LOG;
import com.javath.set.Broker;
import com.javath.settrade.flash.BrokerStreaming;
import com.javath.util.Assign;
import com.javath.util.ObjectException;

public class Click2Win extends BrokerStreaming {
	
	private final static Assign assign;
	private final static String[] login_request;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"broker" + Assign.File_Separator +
				"settrade.properties";
		assign = Assign.getInstance(Click2Win.class, default_Properties);
		// static login_request
		int step = (int) assign.getLongProperty("login_request.length");
		login_request = new String[step];
		for (int index = 0; index < step; index++) {
			login_request[index] = 
					assign.getProperty(String.format("login_request[%d]",index));
		}
		// static put brokers
		int size = (int) assign.getLongProperty("brokers.size");
		for (int index = 0; index < size; index++) {
			String username = 
					assign.getProperty(String.format("brokers[%d].username",index));
			String password = 
					assign.getSecureProperty(String.format("brokers[%d].password",index));
			try {
				getBroker(username, password);
			} catch (ObjectException e) {
				LOG.SEVERE(e);
			}
		}
	}

	private String username;
	private String password;
	
	public synchronized static Broker getBroker(String username, String password) {
		Broker result = Broker.getInstance(Click2Win.class, username);
		if (result == null) {
			result = new Click2Win(username, password);
			Broker.putBroker(result, username);
			return result;
		} else if (result.checkPassword(password)) {
			return result;
		} else {
			return Broker.dummy;
		}
	}
	
	private Click2Win(String username, String password) {
		this.username = username;
		this.password = password;
		cookie = new Cookie();
		initial();
	}
	
	private void initial() {
		INFO("Initial \"%s[username=%s]\"", getClassName(), username);
		authentication(cookie);
		loadFlashVars();
	}
	
	public boolean checkPassword(String password) {
		return password.equals(this.password);
	}
	
	protected CookieStore login(Browser browser) {
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
			//
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
	private Form buildForm(Node node) {
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
	
	protected void loadFlashVars() {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			HtmlParser parser = new HtmlParser(null);
			browser.address("https://click2win.settrade.com/realtime/streaming4/flash/Streaming4Screen.jsp");
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
						browser.setCookie(authentication(cookie));
						loadFlashVars();
						return;
						/**/
					}
					/**/
				}		
				throw new ObjectException(String.format("%s \"%s\"", 
						response.getStatusCode(), response.getReasonPhrase()));
			}
			ParamFilter param_filter = new ParamFilter(parser.parse());
			param_filter.filter();
			// 
			NamedNodeMap attributes = param_filter.name("FlashVars").getAttributes();
			for (int index = 0; index < attributes.getLength(); index++) {
				if (attributes.item(index).getNodeName().equals("value"))
					setFlashVars(attributes.item(index).getNodeValue().split("[&]"));
			}
		} finally {
			Assign.returnObject(browser);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
