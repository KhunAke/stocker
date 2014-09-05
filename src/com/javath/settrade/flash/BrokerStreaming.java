package com.javath.settrade.flash;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.javath.http.Browser;
import com.javath.http.Form;
import com.javath.http.Response;
import com.javath.set.Broker;
import com.javath.util.Assign;
import com.javath.util.ObjectException;

public abstract class BrokerStreaming extends Broker implements Runnable {
	
	//protected String accountNo = getAccountNo();
	protected String pin = "000000";
	
	protected double credit;
	protected double cash;
	protected double line;
	
	// service = S4MarketSummary
	private String mode = "Pull";
	// service = S4InstrumentInfo
	private String initiated_flag = "1";
	private String new_inst_info = "";
	private String old_inst_info = "";
	// service = S4InstrumentTicker
	private String sequence_id = ""; //
	private String new_inst_ticker = "";
	private String new_market = "";
	private String old_inst_ticker = "";
	private String old_market = "";
	// service = S4MarketTicker
	private String option_sequence_id_2 = "-1"; //
	private String sequence_id_2 = "-1"; //
	private String new_market_2 = "A";
	private String new_inst_ticker_2 = "_all";
	private String new_sum_2 = "N";
	private String old_market_2 = "";
	private String old_inst_ticker_2 = "";
	private String old_sum_2 = "";
	
	private Map<String,String> flash_vars;

	protected abstract void loadFlashVars();
	protected void setFlashVars(String[] flash_vars) {
		this.flash_vars = new HashMap<String,String>();
		for (int index = 0; index < flash_vars.length; index++) {
			String[] vars = flash_vars[index].split("[=]");
			if (vars.length == 1)
				this.flash_vars.put(vars[0],"");
			else
				this.flash_vars.put(vars[0],vars[1]);
		}
	}
	public String getFlashVar(String name) {
		try {
			return flash_vars.get(name);
		} catch (NullPointerException e) {
			WARNING("flashVars is %s. Please call method \"loadFlashVars()\" before", e.getMessage());
			loadFlashVars();
			return getFlashVar(name);
		}
	}
	public void printFlashVars() {
		try {
			for (Iterator<String> iterator = flash_vars.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				System.out.printf("%s = %s%n",key,flash_vars.get(key));
			}
		} catch (NullPointerException e) {
			WARNING("flash_vars is %s. Please call method \"loadFlashVars()\" before", e.getMessage());
			loadFlashVars();
			printFlashVars();
		}
	}
	// Information flashVars in name = "fvAccountInfoList"
	protected String getAccountNo() {
		DataProvider data = new DataProvider(); 
		data.read(getFlashVar("fvAccountInfoList"));
		try {
			return data.get(0, 0, 2);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	protected String getAccountType() {
		DataProvider data = new DataProvider();
		data.read(getFlashVar("fvAccountInfoList"));
		try {
			return data.get(2, 0, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	protected String fvHttpLink(String fvHost, String fvPath) {
		return hyperLink("http://", getFlashVar(fvHost), getFlashVar(fvPath)); 
	}
	protected String fvHttpsLink(String fvHost, String fvPath) {
		return hyperLink("https://", getFlashVar(fvHost), getFlashVar(fvPath)); 
	}
	protected String hyperLink(String scheme, String hostname, String path) {
		return scheme + hostname + path; 
	}

	public long synctime() {
		return synctime("fvPrimaryHost");
	}
	protected long synctime(String fvHostName) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			long time = new Date().getTime();
			//browser.address("https://pushctw1.settrade.com/realtime/streaming4/synctime.jsp");
			browser.address(fvHttpsLink(fvHostName, "fvSyncTimeServlet"));
			Response response = browser.get();
			if (response.getStatusCode() != 200) {
				SEVERE("HTTP Status %s \"%s\"",response.getStatusCode(), response.getReasonPhrase());
				return 0;
			}
			DataProvider dataProvider = new DataProvider().read(response.getContent());
			long result = Long.valueOf(dataProvider.get(0,0,2)) - time ;
			INFO("Adjust time Settrade server offset %.3f sec",result/1000.0d);
			//logger.finest(message.toString());
			return result;
		} finally {
			Assign.returnObject(browser);
		}
	}
	
	protected String fvITPHostLink(String path) {
		return getFlashVar("fvITPHost") + path;
	}	
	protected DataProvider seos(Form form) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			// browser httpContext
			browser.address("https://click2win.settrade.com/daytradeflex/streamingSeos.jsp");
			//browser.address(fvITPHostLink("/daytradeflex/streamingSeos.jsp"));
			browser.body(form);
			Response response = browser.post();
			
			DataProvider dataProvider = null;
			try {
				dataProvider = new DataProvider().read(response.getContent());
			} catch (ObjectException e) {
				if (e.getMessage().equals("Unauthorized Access.")) {
					WARNING(e.getMessage());
					browser.setCookie(authentication(cookie));
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
		form.add("txtAccountNo", "");
		//form.add("txtAccountNo", getAccountNo());
		form.add("NewMode", "Pull");
		form.add("txtAccountType", "");
		//form.add("txtAccountType", getAccountType());
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
		form.add("txtAccountNo", ""); // AccountNo
		//form.add("txtAccountNo", getAccountNo()); // AccountNo
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
		return placeOrder(symbol,"B",price,volume);
	}
	public DataProvider sellOrder(String symbol, double price, long volume) {
		return placeOrder(symbol,"S",price,volume);
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
		form.add("txtAccountNo", getAccountNo()); // AccountNo
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
	
	public void getPage(String address) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
			try {
			browser.address(address);
			Response response = browser.get();
			//response.print();
			try {
				DataProvider data = new DataProvider();
				data.read(response.getContent());
				System.out.println(data);
			} catch (ObjectException e) {
				if (e.getMessage().equals("Unauthorize Access")) {
					browser.setCookie(authentication(cookie));
					getPage(address);
				}	
			}
		} finally {
			Assign.returnObject(browser);
		}
	}

}