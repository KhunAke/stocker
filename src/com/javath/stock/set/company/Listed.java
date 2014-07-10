package com.javath.stock.set.company;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.hibernate.Session;

import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.mapping.SetMarket;
import com.javath.mapping.SetMarketHome;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;

public class Listed extends Instance implements Runnable{
	
	private final static Assign assign;
	private final static String page_listed;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"stock" + Assign.File_Separator +
				"set.properties";
		assign = Assign.getInstance(Listed.class, default_Properties);
		page_listed = assign.getProperty("page_listed_th",
				"http://www.set.or.th/listedcompany/static/listedCompanies_%1$s.xls");
	}
	
	private Cookie cookie;
	
	private Listed() {
		cookie = new Cookie();
	}
	
	public String getURI(String locale) {
		return String.format(page_listed, Locale.forLanguageTag(locale));
	}
	private Response getWebPage(String locale) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			browser.address(getURI(locale));
			return browser.get();
		} finally {
			Assign.returnObject(browser);
		}
	}
	@Override
	public void run() {
		Response response = getWebPage("en-US");
		HtmlParser parser = new HtmlParser(response.getContent());
		TextNode text = new TextNode(parser.parse());
		boolean flag_data = false;
		Date update = null;
		for (int index = 0; index < text.length(); index++) {
			if (text.getString(index, 0).equals("5")) {
				flag_data = true;
				update = DateTime.format("'As of 'dd MMM yyyy", text.getString(index, 1));
			} else if ((flag_data) && (text.getString(index, 0).equals("3"))) {
				String[] data = text.getStringArray(index);
				System.out.printf("%d: " ,id(new SetMarket(data[6])));
				System.out.printf("%s, %s, %s, %s, %s, %s, %s%n", 
						data[2], //Symbol
						data[4], //Company
						data[6], //Market
						data[8], //Industry
						data[10],//Sector
						data[20],//Website
						DateTime.date(update));
			}
		}
	}
	private short id(SetMarket instance) {
		short result = 0;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetMarketHome home = (SetMarketHome) 
				Assign.borrowObject(SetMarketHome.class);
		try {
			session.beginTransaction();
			List<SetMarket> lists = home.findByExample(instance);
			if (lists.size() == 0) {
				home.persist(instance);
				result = instance.getId();
			} else
				result = lists.get(0).getId();
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	
	public static void main(String[] args) {
		Listed listed = new Listed();
		System.out.println(listed.getURI("en-US"));
		listed.run();
	}

}
