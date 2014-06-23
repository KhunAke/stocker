package com.javath.stock.bualuang;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import com.javath.http.Browser;
import com.javath.http.Response;
import com.javath.http.State;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.Service;
import com.javath.util.TextSpliter;

public class BoardDaily extends Instance {
	
	private final static Assign assign;
	private final static String date_board;
	private final static Locale date_locale;
	private final static boolean fixed;
	private final static String[] spliter_headers;
	private final static int[] spliter_positions;
	private final static String spliter_delimiter;
	private final static BoardDaily instance;

	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"stock" + Assign.File_Separator +
				"bualuang.properties";
		assign = Assign.getInstance(Service.class.getCanonicalName(), default_Properties);
		date_board = assign.getProperty("date_board",
				"http://realtime.bualuang.co.th/myeasy/realtime/quotation/txt/%1$td%1$tm%1$tY.txt");
		date_locale = Locale.forLanguageTag(assign.getProperty("date_locale","en-US"));
		String spliter_mode = assign.getProperty("spliter", "fixed").toLowerCase();
		if (spliter_mode.equals("fixed")) {
			fixed = true;
		} else if (spliter_mode.equals("delimited"))
			fixed = false;
		else {
			assign.warning(new ObjectException("For input string: \"%s\"", spliter_mode), "spliter");
			fixed = true;
		}
		spliter_headers = assign.getProperty("spliter_headers","").split(",");
		String[] positions = assign.getProperty("spliter_positions","").split(",");
		spliter_positions = new int[positions.length];
		for (int index = 0; index < positions.length; index++) {
			spliter_positions[index] = Integer.valueOf(positions[index]);
		}
		spliter_delimiter = assign.getProperty("spliter_delimiter","\\s");
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
			TextSpliter spliter;
			if (fixed) {
				spliter = new TextSpliter(response.getContent(), true);
				spliter.setPositions(spliter_positions);
			} else {
				spliter = new TextSpliter(response.getContent(), false);
				spliter.setDelimiter(spliter_delimiter);
			}
			try {
				while (spliter.ready()) {
					try {
						String[] fields = spliter.readLine();
						for (int index = 0; index < fields.length; index++) {
							System.out.printf("%s=\"%s\",", spliter_headers[index], fields[index]);
						}
						System.out.println();
					} catch (IOException e) {
						WARNING(e);
					}
				}
			} catch (IOException e) {
				WARNING(e);
			}
		} finally {
			Browser.returnObject(browser);
		}
	}
	private String getURI(Date date) {
		return String.format(date_locale, date_board, date);
	}
	
	public static void main(String[] args) {
		Date date = DateTime.date("2014-06-20");
		BoardDaily.getInstance().getWebPage(date);
	}
	
	@Override
	protected void finalize() throws Throwable {
		State.returnObject(state);
		super.finalize();
	}
	
}
