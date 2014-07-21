package com.javath.set.company;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.javath.bualuang.BoardDaily;
import com.javath.html.HtmlParser;
import com.javath.html.TextNode;
import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.mapping.SetCompany;
import com.javath.mapping.SetCompanyHome;
import com.javath.mapping.SetIndustry;
import com.javath.mapping.SetIndustryHome;
import com.javath.mapping.SetIndustryId;
import com.javath.mapping.SetMarket;
import com.javath.mapping.SetMarketHome;
import com.javath.mapping.SetSector;
import com.javath.mapping.SetSectorHome;
import com.javath.mapping.SetSectorId;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorListener;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.NotificationAdaptor;
import com.javath.util.NotificationEvent;
import com.javath.util.NotificationListener;
import com.javath.util.NotificationSource;
import com.javath.util.TaskManager;
import com.javath.util.NotificationEvent.Status;

public class Listed extends Instance 
		implements NotificationSource, NotificationListener, OscillatorListener, Runnable {
	
	private final static int SYMBOL = 0;
	private final static int MARKET = 1;
	private final static int INDUSTRY = 2;
	private final static int SECTOR = 3;
	private final static int COMPANY = 4;
	private final static int WEBSITE = 5;
	
	private final static Assign assign;
	private final static String page_listed;
	private final static long try_again;
	private final static Listed instance;
	//
	private static final Options options;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"set.properties";
		assign = Assign.getInstance(Listed.class, default_Properties);
		page_listed = assign.getProperty("page_listed",
				"http://www.set.or.th/listedcompany/static/listedCompanies_%1$s.xls");
		//try_again = assign.getLongProperty("try_again", 5000);
		try_again = assign.getLongProperty("try_again", 900000); // 15 minute
		instance = new Listed();
		options = buildOptions();
	}
	
	public static Listed getInstance() {
		return instance;
	}
	
	private Cookie cookie;
	
	private final NotificationAdaptor note;
	
	private Listed() {
		cookie = new Cookie();
		note = new NotificationAdaptor(this);
	}
	
	@Override
	public boolean addListener(NotificationListener listener) {
		return note.addListener(listener);
	}
	@Override
	public boolean removeListener(NotificationListener listener) {
		return note.removeListener(listener);
	}
	
	private void initTryAgain(String locale) {
		note.notify(Status.FAIL, getURI(locale));
		Oscillator oscillator = Oscillator.getInstance(try_again);
		oscillator.addListener(this);
		oscillator.start();
	}
	@Override
	public void action(OscillatorEvent event) {
		Oscillator oscillator = Oscillator.getInstance(try_again);
		oscillator.removeListener(this);
		TaskManager.create(
				String.format("%s[re-try=\"%s\"]", 
						this.getClassName(), DateTime.timestamp(event.getTimestamp())), 
				this);
	}
	@Override
	public void notify(NotificationEvent event) {
		if (event.isClass(BoardDaily.class) 
			&& (event.getStatus() == Status.SUCCESS)) {
			if (getCurrentWeek() != getUpdateWeek())
				TaskManager.create(
						String.format("%s[update]", 
								this.getClassName()), 
						this);
			else
				//note.output("\"Company listed\" The infomation available is current.");
				note.output("\"Company listed\" skip.");
		}
	}
	private int getCurrentWeek() {
		int result = 0;
		Calendar calendar = DateTime.borrowCalendar();
		try {
			result = calendar.get(Calendar.WEEK_OF_YEAR);
		} finally {
			DateTime.returnCalendar(calendar);
		}
		return result;
	}
	private int getUpdateWeek() {
		int result = 0;
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Query query = session.createQuery(
					"select max(weekofyear(company.lastUpdate)) " +
					"from SetCompany as company");
			result = (int) query.uniqueResult();
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
		}
		return result;
	}
	@Override
	public void run() {
		Response response = null;
		String locale = "th-TH";
		try {
			response = getWebPage(locale);
			if (response.getStatusCode() == 200) {
				note.notify(Status.DONE, getURI(locale));
				parser(response, true);
			} else {
				initTryAgain(locale);
				return;
			}
		} catch (Exception e) {
			initTryAgain(locale);
			SEVERE(e);
			return;
		}
		locale = "en-US";
		try {
			response = getWebPage(locale);
			if (response.getStatusCode() == 200) {
				note.notify(Status.DONE, getURI(locale));
				parser(response, false);
			} else {
				initTryAgain(locale);
				return;
			}
		} catch (Exception e) {
			initTryAgain(locale);
			SEVERE(e);
			return;
		}
		note.notify(Status.SUCCESS, getURI(null));
	}
	
	public String getURI(String locale) {
		try {
			return String.format(page_listed, Locale.forLanguageTag(locale));
		} catch (NullPointerException e) {
			return String.format(page_listed, "??_??");
		}
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
	private void parser(Response response, boolean thai) {
		HtmlParser parser = new HtmlParser(response.getContent());
		TextNode text = new TextNode(parser.parse());
		Date update = null;
		Map<String,Integer> header = null;
		for (int index = 0; index < text.length(); index++) {
			if (text.getString(index, 0).equals("5")) {
				if (thai)
					update = DateTime.format(Assign.th_TH, "'ข้อมูล ณ วันที่ 'dd MMM yyyy", text.getString(index, 1));
				else
					update = DateTime.format("'As of 'dd MMM yyyy", text.getString(index, 1));
			} else if ((update != null) && (text.getString(index, 0).equals("3"))) {
				String[] raw = text.getStringArray(index);
				try {
					String[] data = new String[6];
					if (thai) {
						data[SYMBOL] = raw[header.get("หลักทรัพย์")];
						data[MARKET] = raw[header.get("ตลาด")];
						data[INDUSTRY] = raw[header.get("กลุ่มอุตสาหกรรม")];
						data[SECTOR] = raw[header.get("หมวดธุรกิจ")];
						data[COMPANY] = raw[header.get("บริษัท")];
						data[WEBSITE] = raw[header.get("เว๊บไซต์")];
					} else {
						data[SYMBOL] = raw[header.get("Symbol")];
						data[MARKET] = raw[header.get("Market")];
						data[INDUSTRY] = raw[header.get("Industry")];
						data[SECTOR] = raw[header.get("Sector")];
						data[COMPANY] = raw[header.get("Company")];
						data[WEBSITE] = raw[header.get("Website")];
					}
					//System.out.printf("%s, %s, %s, %s, %s, %s%n",
					//		data[SYMBOL], data[MARKET], data[INDUSTRY], data[SECTOR], data[COMPANY], data[WEBSITE]);
					saveOrUpdate(data, update, thai);
				} catch (NullPointerException e) {
					header = new HashMap<String, Integer>();
					for (int order = 0; order < raw.length; order++) {
						//String name = data[order].trim();
						if (!raw[order].equals(""))
							header.put(raw[order], order);
					}
				}
			}
		}
	}
	private void saveOrUpdate(String[] data, Date update, boolean thai) {
		//SetSectorId id = id(data[SYMBOL], data[MARKET], data[INDUSTRY], data[SECTOR] ,thai);
		SetCompany company = null;
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try { 
			SetCompanyHome home = (SetCompanyHome) 
					Assign.borrowObject(SetCompanyHome.class);
			try {
				company = home.findById(data[SYMBOL]);
				SetSectorId id = id(company, data[MARKET], data[INDUSTRY], data[SECTOR] ,thai);
				company.setMarketId(id.getMarketId());
				company.setIndustryId(id.getIndustryId());
				company.setSectorId(id.getSectorId());
				if (thai)
					company.setNameTh(data[COMPANY]);
				else
					company.setNameEn(data[COMPANY]);
				company.setWebsite(data[WEBSITE]);
				company.setLastUpdate(update);
				home.attachDirty(company);
				//System.out.printf("%s, %s, %s, %s%n",
				//		company.getSymbol(), company.getMarketId(), company.getIndustryId(), company.getSectorId());
				session.getTransaction().commit();
			} catch (NullPointerException e) {
				SetSectorId id = id(null, data[MARKET], data[INDUSTRY], data[SECTOR] ,thai);
				if (thai) {
					company = new SetCompany(
							data[SYMBOL],
							id.getMarketId(),
							id.getIndustryId(),
							id.getSectorId(),
							false,
							false,
							false,
							data[COMPANY],
							null,
							data[WEBSITE],
							update);
				} else {
					company = new SetCompany(
							data[SYMBOL],
							id.getMarketId(),
							id.getIndustryId(),
							id.getSectorId(),
							false,
							false,
							false,
							null,
							data[COMPANY],
							data[WEBSITE],
							update);
				}
				home.attachDirty(company);
				//System.out.printf("%s, %s, %s, %s%n",
				//		company.getSymbol(), company.getMarketId(), company.getIndustryId(), company.getSectorId());
				session.getTransaction().commit();
			} finally {
				Assign.returnObject(home);
			}
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		}
	}
	private SetSectorId id (SetCompany company, String market, String industry, String sector, boolean thai) {
		//SetCompany company = findById(symbol);
		short market_id = 0;
		short industry_id = 0;
		short sector_id = 0;
		try {
			market_id = company.getMarketId();
			industry_id = company.getIndustryId();
			sector_id = company.getSectorId();
			SetMarket current_market = findById(market_id);
			if (!current_market.getName()
					.equals(market)) {
				SetMarket update_market = new SetMarket();
				update_market.setName(market);
				market_id = id(update_market);
				WARNING("Symbol=\"%s\" Market change \"%s\" to \"%s\"",
						company.getSymbol(), current_market.getName(), update_market.getName());
			}
			if (thai) {
				SetIndustry current_industry = findById(market_id, industry_id);
				if (current_industry.getNameTh() == null) {
					current_industry.setNameTh(industry);
					update(current_industry);
				} else if (!current_industry.getNameTh().equals(industry)) {
					SetIndustry update_industry = new SetIndustry();
					SetIndustryId update_industry_id = new SetIndustryId();
					update_industry_id.setMarketId(market_id);
					update_industry.setId(update_industry_id);
					update_industry.setNameTh(industry);
					industry_id = id(update_industry);
					WARNING("Symbol=\"%s\" Industry change \"%s\" to \"%s\"",
							company.getSymbol(), current_industry.getNameTh(), update_industry.getNameTh());
				} 
				SetSector current_sector = findById(market_id, industry_id, sector_id);
				if (current_sector.getNameTh() == null) {
					current_sector.setNameTh(sector);
					update(current_sector);
				} else if (!current_sector.getNameTh().equals(sector)) {
					SetSector update_sector = new SetSector();
					SetSectorId update_sector_id = new SetSectorId();
					update_sector_id.setMarketId(market_id);
					update_sector_id.setIndustryId(industry_id);
					update_sector.setId(update_sector_id);
					update_sector.setNameTh(sector);
					sector_id = id(update_sector);
					WARNING("Symbol=\"%s\" Sector change \"%s\" to \"%s\"",
							company.getSymbol(), current_sector.getNameTh(), update_sector.getNameTh());
				}
			} else {
				SetIndustry current_industry = findById(market_id, industry_id);
				if (current_industry.getNameEn() == null) {
					current_industry.setNameEn(industry);
					update(current_industry);
				} else if (!current_industry.getNameEn().equals(industry)) {
					SetIndustry update_industry = new SetIndustry();
					SetIndustryId update_industry_id = new SetIndustryId();
					update_industry_id.setMarketId(market_id);
					update_industry.setId(update_industry_id);
					update_industry.setNameEn(industry);
					industry_id = id(update_industry);
					WARNING("Symbol=\"%s\" Industry change \"%s\" to \"%s\"",
							company.getSymbol(), current_industry.getNameEn(), update_industry.getNameEn());
				} 
				SetSector current_sector = findById(market_id, industry_id, sector_id);
				if (current_sector.getNameEn() == null) {
					current_sector.setNameEn(sector);
					update(current_sector);
				} else if (!current_sector.getNameEn().equals(sector)) {
					SetSector update_sector = new SetSector();
					SetSectorId update_sector_id = new SetSectorId();
					update_sector_id.setMarketId(market_id);
					update_sector_id.setIndustryId(industry_id);
					update_sector.setId(update_sector_id);
					update_sector.setNameEn(sector);
					sector_id = id(update_sector);
					WARNING("Symbol=\"%s\" Sector change \"%s\" to \"%s\"",
							company.getSymbol(), current_sector.getNameEn(), update_sector.getNameEn());
				}
			}
		} catch (NullPointerException e) {
			//
			SetMarket example_market = new SetMarket();
			example_market.setName(market);
			market_id = id(example_market);
			//
			SetIndustry example_industry = new SetIndustry();
			SetIndustryId example_industry_id = new SetIndustryId();
			example_industry_id.setMarketId(market_id);
			example_industry.setId(example_industry_id);
			if (thai)
				example_industry.setNameTh(industry);
			else
				example_industry.setNameEn(industry);
			industry_id = id(example_industry);
			//
			SetSector example_sector = new SetSector();
			SetSectorId example_sector_id = new SetSectorId();
			example_sector_id.setMarketId(market_id);
			example_sector_id.setIndustryId(industry_id);
			example_sector.setId(example_sector_id);
			if (thai)
				example_sector.setNameTh(sector);
			else
				example_sector.setNameEn(sector);
			sector_id = id(example_sector);
		}
		return new SetSectorId(market_id, industry_id, sector_id);
	}
	/**
	private SetCompany findById(String symbol) {
		SetCompany result = null;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetCompanyHome home = (SetCompanyHome) 
				Assign.borrowObject(SetCompanyHome.class);
		try {
			session.beginTransaction();
			result =  home.findById(symbol);
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	/**/
	private SetMarket findById(short market_id) {
		SetMarketHome home = (SetMarketHome) 
				Assign.borrowObject(SetMarketHome.class);
		try {
			return home.findById(market_id);
		} finally {
			Assign.returnObject(home);
		}
	}
	private SetIndustry findById(short market_id, short industry_id) {
		SetIndustryHome home = (SetIndustryHome) 
				Assign.borrowObject(SetIndustryHome.class);
		try {
			return home.findById(new SetIndustryId(market_id, industry_id));
		} finally {
			Assign.returnObject(home);
		}
	}
	private SetSector findById(short market_id, short industry_id, short sector_id) {
		SetSectorHome home = (SetSectorHome) 
				Assign.borrowObject(SetSectorHome.class);
		try {
			return home.findById(new SetSectorId(market_id, industry_id, sector_id));
		} finally {
			Assign.returnObject(home);
		}
	}
	private short id(SetMarket instance) {
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetMarketHome home = (SetMarketHome) 
				Assign.borrowObject(SetMarketHome.class);
		try {
			List<SetMarket> lists = home.findByExample(instance); 
			if (lists.isEmpty()) {
				Query query = session.createQuery(
						"select max(market.marketId) " +
						"from SetMarket as market");
				instance.setMarketId((short) ((short) query.uniqueResult() + 1));
				instance = home.merge(instance);
				return instance.getMarketId();
			} else
				return lists.get(0).getMarketId();
		} catch (NullPointerException e) {
			instance.setMarketId((short) 1);
			instance = home.merge(instance);
			return instance.getMarketId();
		} finally {
			Assign.returnObject(home);
		}
	}
	private short id(SetIndustry instance) {
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetIndustryHome home = (SetIndustryHome) 
				Assign.borrowObject(SetIndustryHome.class);
		try {
			List<SetIndustry> lists = home.findByExample(instance); 
			if (lists.isEmpty()) {
				int market_id = instance.getId().getMarketId();
				Query query = session.createQuery(String.format(
						"select max(industry.id.industryId) " +
						"from SetIndustry as industry " +
						"where industry.id.marketId = '%d'",
						market_id));
				instance.getId().setIndustryId((short) ((short) query.uniqueResult() + 1));
				instance = home.merge(instance);
				return instance.getId().getIndustryId();
			} else
				return lists.get(0).getId().getIndustryId();
		} catch (NullPointerException e) {
			instance.getId().setIndustryId((short) 1);
			instance = home.merge(instance);
			return instance.getId().getIndustryId();
		} finally {
			Assign.returnObject(home);
		}
	}
	private short id(SetSector instance) {
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetSectorHome home = (SetSectorHome) 
				Assign.borrowObject(SetSectorHome.class);
		try {
			List<SetSector> lists = home.findByExample(instance); 
			if (lists.isEmpty()) {
				int market_id = instance.getId().getMarketId();
				int industry_id = instance.getId().getIndustryId();
				Query query = session.createQuery(String.format(
						"select max(sector.id.sectorId) " +
						"from SetSector as sector " +
						"where sector.id.marketId = '%d' " +
						"and sector.id.industryId = '%d' ",
						market_id, industry_id));
				instance.getId().setSectorId((short) ((short) query.uniqueResult() + 1));
				instance = home.merge(instance);
				return instance.getId().getSectorId();
			} else
				return lists.get(0).getId().getSectorId();
		} catch (NullPointerException e) {
			instance.getId().setSectorId((short) 1);
			instance = home.merge(instance);
			return instance.getId().getSectorId();
		} finally {
			Assign.returnObject(home);
		}
	}
	private void update(SetIndustry instance) {
		SetIndustryHome home = (SetIndustryHome) 
				Assign.borrowObject(SetIndustryHome.class);
		try {
			home.attachDirty(instance);
		} finally {
			Assign.returnObject(home);
		}
	}
	private void update(SetSector instance) {
		SetSectorHome home = (SetSectorHome) 
				Assign.borrowObject(SetSectorHome.class);
		try {
			home.attachDirty(instance);
		} finally {
			Assign.returnObject(home);
		}
	}
	
	public static void main(String[] args) {
		CommandLine line = commandline(args);
		int main_option = 0;
		if (line.hasOption("update"))
			main_option += 1;
		if (line.hasOption("help") || (main_option > 1)) {
			usage();
			return;
		}
		Listed listed = Listed.getInstance();
		if (line.hasOption("update")) {
			if (listed.getCurrentWeek() != listed.getUpdateWeek())
				TaskManager.create(
						String.format("%s[update]", 
								listed.getClassName()), 
								listed);
			else {
				//listed.output("\"Company listed\" The infomation available is current.");
				listed.output("\"Company listed\" skip.");
			}
		}
	}
	
	private static CommandLine commandline(String[] args) {
		// CommandLineParser parser = new BasicParser();
		// CommandLineParser parser = new PosixParser();
		try {
			//return new BasicParser().parse(options, args );
			//return new PosixParser().parse(options, args );
			return new GnuParser().parse(options, args );
		} catch (ParseException e) {
			return commandline(new String[] {"--help"});
		}
	}
	@SuppressWarnings("static-access")
	private static Options buildOptions() {
		Options options = new Options();
		OptionGroup group = new OptionGroup();
		/**
		// Option "--date"
		Option date = OptionBuilder
				.withArgName("yyyy-mm-dd")
				.hasArg()
                .withDescription("extract data from the BLS website on \"date\"")
                .withLongOpt("date")
                .create();
		//options.addOption(date);
		/**/
		// Option "--help"
		Option help = OptionBuilder
                .withDescription("print this message")
                .withLongOpt("help")
                .create();
		//options.addOption(help);
		// Option "--update"
		Option update = OptionBuilder
                .withDescription("check week update")
                .withLongOpt("update")
                .create();
		//options.addOption(update);
		group.addOption(help);
		group.addOption(update);
		group.setRequired(true);
		options.addOptionGroup(group);
		return options;
	}
	private static void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Listed", options);
	}

}
