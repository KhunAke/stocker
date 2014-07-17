package com.javath.stock.set.company;

import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

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
import com.javath.trigger.MulticastEvent;
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
import com.javath.util.NotificationEvent.Status;

public class Listed extends Instance implements NotificationSource, OscillatorListener, Runnable {
	
	private final static int SYMBOL = 0;
	private final static int MARKET = 1;
	private final static int INDUSTRY = 2;
	private final static int SECTOR = 3;
	private final static int COMPANY = 4;
	private final static int WEBSITE = 5;
	
	private final static Assign assign;
	private final static String page_listed;
	private final static long try_again;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"stock" + Assign.File_Separator +
				"set.properties";
		assign = Assign.getInstance(Listed.class, default_Properties);
		page_listed = assign.getProperty("page_listed",
				"http://www.set.or.th/listedcompany/static/listedCompanies_%1$s.xls");
		try_again = assign.getLongProperty("try_again", 5000);
		//try_again = assign.getLongProperty("try_again", 900000); // 15 minute
	}
	
	private Cookie cookie;
	private final NotificationAdaptor note;
	
	private Listed() {
		cookie = new Cookie();
		note = new NotificationAdaptor();
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
		this.run();
	}
	@Override
	public void run() {
		Response response = null;
		String locale = "en-US";
		try {
			response = getWebPage(locale);
			if (response.getStatusCode() == 200)
				note.notify(Status.DONE, getURI(locale));
			else {
				initTryAgain(locale);
				return;
			}
			parser(response, false);
		} catch (Exception e) {
			initTryAgain(locale);
			SEVERE(e);
			return;
		}
		locale = "th-TH";
		try {
			response = getWebPage(locale);
			if (response.getStatusCode() == 200)
				note.notify(Status.DONE, getURI(locale));
			else {
				initTryAgain(locale);
				return;
			}
			parser(response, true);
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
				company.setIndustryId(id.getMarketId());
				company.setSectorId(id.getMarketId());
				if (thai)
					company.setNameTh(data[COMPANY]);
				else
					company.setNameEn(data[COMPANY]);
				company.setWebsite(data[WEBSITE]);
				company.setLastUpdate(update);
			} catch (NullPointerException e) {
				SetSectorId id = id(null, data[MARKET], data[INDUSTRY], data[SECTOR] ,thai);
				if (thai) {
					company = new SetCompany(
							data[SYMBOL],
							id.getMarketId(),
							id.getIndustryId(),
							id.getSectorId(),
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
							null,
							data[COMPANY],
							data[WEBSITE],
							update);
				}
				home.attachDirty(company);
			} finally {
				Assign.returnObject(home);
			}
			session.getTransaction().commit();
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
			if (!findById(market_id).getName()
					.equals(market)) {
				SetMarket example_market = new SetMarket();
				example_market.setName(market);
				market_id = id(example_market);
			}
			if (thai) {
				SetIndustry set_industry = findById(market_id, industry_id);
				if (set_industry.getNameTh() == null) {
					set_industry.setNameTh(industry);
					update(set_industry);
				} else if (!set_industry.getNameTh().equals(industry)) {
					SetIndustry example_industry = new SetIndustry();
					SetIndustryId example_industry_id = new SetIndustryId();
					example_industry_id.setMarketId(market_id);
					example_industry.setId(example_industry_id);
					example_industry.setNameTh(industry);
					industry_id = id(example_industry);
				} 
				SetSector set_sector = findById(market_id, industry_id, sector_id);
				if (set_sector.getNameTh() == null) {
					set_sector.setNameTh(industry);
					update(set_sector);
				} else if (!set_sector.getNameTh().equals(sector)) {
					SetSector example_sector = new SetSector();
					SetSectorId example_sector_id = new SetSectorId();
					example_sector_id.setMarketId(market_id);
					example_sector_id.setIndustryId(industry_id);
					example_sector.setId(example_sector_id);
					example_sector.setNameTh(sector);
					sector_id = id(example_sector);
				}
			} else {
				SetIndustry set_industry = findById(market_id, industry_id);
				if (set_industry.getNameEn() == null) {
					set_industry.setNameEn(industry);
					update(set_industry);
				} else if (!set_industry.getNameEn().equals(industry)) {
					SetIndustry example_industry = new SetIndustry();
					SetIndustryId example_industry_id = new SetIndustryId();
					example_industry_id.setMarketId(market_id);
					example_industry.setId(example_industry_id);
					example_industry.setNameEn(industry);
					industry_id = id(example_industry);
				} 
				SetSector set_sector = findById(market_id, industry_id, sector_id);
				if (set_sector.getNameEn() == null) {
					set_sector.setNameEn(industry);
					update(set_sector);
				} else if (!set_sector.getNameEn().equals(sector)) {
					SetSector example_sector = new SetSector();
					SetSectorId example_sector_id = new SetSectorId();
					example_sector_id.setMarketId(market_id);
					example_sector_id.setIndustryId(industry_id);
					example_sector.setId(example_sector_id);
					example_sector.setNameEn(sector);
					sector_id = id(example_sector);
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
		Listed listed = new Listed();
		listed.run();
	}


}
