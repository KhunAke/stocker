package com.javath.stock.bualuang;

import java.util.Date;
import java.util.Locale;

import com.javath.http.Browser;
import com.javath.http.Response;
import com.javath.http.State;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.Service;

public class BoardDaily extends Instance {
	
	private final static Assign assign;
	private final static String date_board;
	private final static Locale date_locale;
	private final static BoardDaily instance;

	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"stock" + Assign.File_Separator +
				"bualuang.properties";
		assign = Assign.getInstance(Service.class.getCanonicalName(), default_Properties);
		date_board = assign.getProperty("date_board",
				"http://realtime.bualuang.co.th/myeasy/realtime/quotation/txt/%1$td%1$tm%1$tY.txt");
		date_locale = Locale.forLanguageTag(assign.getProperty("date_locale","en-US"));
		instance = new BoardDaily();
	}
	
	public static BoardDaily getInstance() {
		return instance;
	}
	
	private final State state;
	
	private BoardDaily() {
		state = State.borrowObject();
	}
	
	private void getWebPage(Date date) {
		Browser browser = Browser.borrowObject(state.getCookieStore());
		try {
			browser.address(getURI(date));
			Response response = browser.get();
			response.print();
		} finally {
			Browser.returnObject(browser);
		}
	}
	
	private String getURI(Date date) {
		return String.format(date_locale, date_board, date);
	}
	
	public static void main(String[] args) {
		Date date = DateTime.date("2014-06-15");
		BoardDaily.getInstance().getWebPage(date);
	}
	
	@Override
	protected void finalize() throws Throwable {
		State.returnObject(state);
		super.finalize();
	}
	
}
