package com.javath.stock.set.company;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
		Date update = null;
		Map<String,Integer> header = null;
		for (int index = 0; index < text.length(); index++) {
			if (text.getString(index, 0).equals("5")) {
				update = DateTime.format("'As of 'dd MMM yyyy", text.getString(index, 1));
			} else if ((update != null) && (text.getString(index, 0).equals("3"))) {
				String[] data = text.getStringArray(index);
				try {
					SetSectorId id = id(data[header.get("Symbol")],
							data[header.get("Market")],
							data[header.get("Industry")],
							data[header.get("Sector")]);
					System.out.printf("%s, %s, %s, %s, %s, %s, %s%n", 
							data[header.get("Symbol")], //Symbol
							data[header.get("Company")], //Company
							data[header.get("Market")], //Market
							data[header.get("Industry")], //Industry
							data[header.get("Sector")],//Sector
							data[header.get("Website")],//Website
							DateTime.date(update));
					Session session = Assign.getSessionFactory().getCurrentSession();
					SetCompanyHome home = (SetCompanyHome) 
							Assign.borrowObject(SetCompanyHome.class);
					try {
						session.beginTransaction();
						//
						//
						session.getTransaction().commit();
					} catch (Exception e) {
						session.getTransaction().rollback();
						throw e;
					} finally {
						Assign.returnObject(home);
					}
					
				} catch (NullPointerException e) {
					header = new HashMap<String, Integer>();
					for (int order = 0; order < data.length; order++) {
						//String name = data[order].trim();
						if (!data[order].equals(""))
							header.put(data[order], order);
					}
				}
			}
		}
	}
	private SetSectorId id (String symbol, String market, String industry, String sector) {
		SetCompany company = findById(symbol);
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
			if (!findById(market_id, industry_id).getNameEn()
					.equals(industry)) {
				SetIndustry example_industry = new SetIndustry();
				SetIndustryId example_industry_id = new SetIndustryId();
				example_industry_id.setMarketId(market_id);
				example_industry.setId(example_industry_id);
				example_industry.setNameEn(industry);
				industry_id = id(example_industry);
			}
			if (!findById(market_id, industry_id, sector_id).getNameEn()
					.equals(sector)) {
				SetSector example_sector = new SetSector();
				SetSectorId example_sector_id = new SetSectorId();
				example_sector_id.setMarketId(market_id);
				example_sector_id.setIndustryId(industry_id);
				example_sector.setId(example_sector_id);
				example_sector.setNameEn(sector);
				sector_id = id(example_sector);
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
			example_industry.setNameEn(industry);
			industry_id = id(example_industry);
			//
			SetSector example_sector = new SetSector();
			SetSectorId example_sector_id = new SetSectorId();
			example_sector_id.setMarketId(market_id);
			example_sector_id.setIndustryId(industry_id);
			example_sector.setId(example_sector_id);
			example_sector.setNameEn(sector);
			sector_id = id(example_sector);
		}
		System.out.printf("%d, %d, %d%n", market_id, industry_id, sector_id);
		return new SetSectorId(market_id, industry_id, sector_id);
	}
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
	private SetMarket findById(short market_id) {
		SetMarket result = null;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetMarketHome home = (SetMarketHome) 
				Assign.borrowObject(SetMarketHome.class);
		try {
			session.beginTransaction();
			result =  home.findById(market_id);
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	private SetIndustry findById(short market_id, short industry_id) {
		SetIndustry result = null;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetIndustryHome home = (SetIndustryHome) 
				Assign.borrowObject(SetIndustryHome.class);
		try {
			session.beginTransaction();
			result =  home.findById(new SetIndustryId(market_id, industry_id));
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	private SetSector findById(short market_id, short industry_id, short sector_id) {
		SetSector result = null;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetSectorHome home = (SetSectorHome) 
				Assign.borrowObject(SetSectorHome.class);
		try {
			session.beginTransaction();
			result =  home.findById(new SetSectorId(market_id, industry_id, sector_id));
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	private short id(SetMarket instance) {
		short result = 0;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetMarketHome home = (SetMarketHome) 
				Assign.borrowObject(SetMarketHome.class);
		session.beginTransaction();
		try {
			List<SetMarket> lists = home.findByExample(instance); 
			if (lists.isEmpty()) {
				Query query = session.createQuery(
						"select max(market.marketId) " +
						"from SetMarket as market");
				instance.setMarketId((short) ((short) query.uniqueResult() + 1));
				home.persist(instance);
				result = instance.getMarketId();
				session.getTransaction().commit();
				return result;
			} else {
				session.getTransaction().rollback();
				return lists.get(0).getMarketId();
			}
		} catch (NullPointerException e) {
			instance.setMarketId((short) 1);
			home.persist(instance);
			result = instance.getMarketId();
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	private short id(SetIndustry instance) {
		short result = 0;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetIndustryHome home = (SetIndustryHome) 
				Assign.borrowObject(SetIndustryHome.class);
		session.beginTransaction();
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
				home.persist(instance);
				result = instance.getId().getIndustryId();
				session.getTransaction().commit();
				return result;
			} else {
				session.getTransaction().rollback();
				return lists.get(0).getId().getIndustryId();
			}
		} catch (NullPointerException e) {
			instance.getId().setIndustryId((short) 1);
			home.persist(instance);
			result = instance.getId().getIndustryId();
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			Assign.returnObject(home);
		}
	}
	private short id(SetSector instance) {
		short result = 0;
		Session session = Assign.getSessionFactory().getCurrentSession();
		SetSectorHome home = (SetSectorHome) 
				Assign.borrowObject(SetSectorHome.class);
		session.beginTransaction();
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
				home.persist(instance);
				result = instance.getId().getSectorId();
				session.getTransaction().commit();
				return result;
			} else {
				session.getTransaction().rollback();
				return lists.get(0).getId().getSectorId();
			}
		} catch (NullPointerException e) {
			instance.getId().setSectorId((short) 1);
			home.persist(instance);
			result = instance.getId().getSectorId();
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
