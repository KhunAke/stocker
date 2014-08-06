package com.javath.bualuang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import org.hibernate.exception.ConstraintViolationException;

import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.http.Response;
import com.javath.logger.LOG;
import com.javath.mapping.BualuangQuoteDaily;
import com.javath.mapping.BualuangQuoteDailyHome;
import com.javath.mapping.BualuangQuoteDailyId;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorDivideFilter;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorLoader;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.NotificationAdaptor;
import com.javath.util.NotificationEvent.NoteStatus;
import com.javath.util.NotificationListener;
import com.javath.util.NotificationSource;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;
import com.javath.util.TextSpliter;

public class QuoteDaily extends Instance 
		implements NotificationSource, OscillatorLoader, Runnable {
	
	private final static Assign assign;
	private final static String date_board;
	private final static Locale date_locale;
	private final static boolean fixed;
	private final static String[] spliter_headers;
	private final static Map<String,Integer> map_headers;
	private final static int[] spliter_positions;
	private final static String spliter_date_parse;
	private final static String spliter_delimiter;
	private final static String storage_path;
	private final static QuoteDaily instance;
	//
	private static final Options options;

	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"bualuang.properties";
		assign = Assign.getInstance(QuoteDaily.class, default_Properties);
		String default_path = Assign.var + Assign.File_Separator + "bualuang"
				+ Assign.File_Separator + "quotation";
		storage_path = assign.getProperty("storage_path", default_path);
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
		map_headers = new HashMap<String,Integer>();
		spliter_headers = assign.getProperty("spliter_headers","").split(",");
		for (int index = 0; index < spliter_headers.length; index++)
			try {
				map_headers.put(spliter_headers[index].trim(), index);
			} catch (Exception e) {
				LOG.CONFIG(new ObjectException(e,
						"\"spliter_headers\" at order %d", index));
			}
		
		String[] positions = assign.getProperty("spliter_positions","0").split(",");
		spliter_positions = new int[positions.length];
		for (int index = 0; index < positions.length; index++) {
			spliter_positions[index] = Integer.valueOf(positions[index]);
		}
		spliter_date_parse = assign.getProperty("spliter_date_parse","yyMMdd");
		spliter_delimiter = assign.getProperty("spliter_delimiter","\\s");
		instance = new QuoteDaily();
		options = buildOptions();
	}
	
	public static QuoteDaily getInstance() {
		return instance;
	}
	public static Date getLastUpdate() {
		Session session = Assign.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("select max(board.id.date) from BualuangBoardDaily as board");
		Date date = (Date) query.uniqueResult();
		session.getTransaction().commit();
		return date;
	}
	
	private final Cookie cookie;
	private OscillatorDivideFilter oscillator;
	private Date wait_update;
	
	private final NotificationAdaptor note;
	
	private QuoteDaily() {
		cookie = new Cookie();
		note = new NotificationAdaptor(this);
	}
	
	private long setUpdate(Date date) {
		Calendar calendar = DateTime.borrowCalendar();
		try {
			calendar.setTime(date);
			if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
				calendar.add(Calendar.DATE, 3);
			else
				calendar.add(Calendar.DATE, 1);
			wait_update = calendar.getTime();
		} catch (NullPointerException e) {
			wait_update = DateTime.date("2002-07-02");	
		} finally {
			DateTime.returnCalendar(calendar);
		}
		return wait_update.getTime();
	}
	private Response getWebPage(Date date) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		browser.setCookie(cookie.getCookieStore());
		try {
			browser.address(getURI(date));
			return browser.get();
		} finally {
			Assign.returnObject(browser);
		}
	}
	private String getURI(Date date) {
		return String.format(date_locale, date_board, date);
	}
	
	@Override
	public boolean addListener(NotificationListener listener) {
		return note.addListener(listener);
	}
	@Override
	public boolean removeListener(NotificationListener listener) {
		return note.removeListener(listener);
	}
	
	@Override
	public void action(OscillatorEvent event) {
		TaskManager.create(
				String.format("%s[timestamp=%s,update=%s]", 
						this.getClassName(), DateTime.timestamp(event.getTimestamp()), DateTime.date(wait_update)), 
				this);
	}
	@Override
	public void run() {
		Response response = getWebPage(wait_update);
		if (response.getStatusCode() == 200) {
			try {
			response.save(storage_path + Assign.File_Separator + 
					response.getFilename());
					//String.format("%1$td%1$tm%1$tY.txt", wait_update));
			} catch (ObjectException e) {
				//if (e.getCause().getClass().getCanonicalName()
				//		.equals("java.io.FileNotFoundException")) {
				if (e.equalsCause(FileNotFoundException.class)) {
					new File(storage_path).mkdirs();
					response.save(storage_path + Assign.File_Separator + 
							response.getFilename());
							//String.format("%1$td%1$tm%1$tY.txt", wait_update));
				}
			}
			note.notify(NoteStatus.DONE, getURI(wait_update));
			try {
				upload(response.getContent());
			} catch (Exception e) {
				SEVERE(new ObjectException(e, "%s; %s",
						response.getFilename(), e.getMessage()));
				Oscillator source = oscillator.getSource();
				source.removeListener(oscillator);
				e.printStackTrace(System.out);
				return;
			}
			note.notify(NoteStatus.SUCCESS, getURI(wait_update));
			long date = setUpdate(QuoteDaily.getLastUpdate());
			long time = DateTime.time(
					assign.getProperty("schedule", "18:30:00")).getTime();
			//System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(time));
			long datetime = DateTime.merge(date, time).getTime();
			oscillator.setSchedule(datetime);
		} else if (response.getStatusCode() == 404) {
			note.notify(NoteStatus.FAIL, getURI(wait_update));
			WARNING("%s: %d %s", response.getFilename(), 
					response.getStatusCode(), response.getReasonPhrase());
			if (wait_update.compareTo(DateTime.date()) < 0)
				setUpdate(wait_update);
		} else {
			note.notify(NoteStatus.UNKNOW, getURI(wait_update));
			WARNING("%s: %d %s", response.getFilename(), 
					response.getStatusCode(), response.getReasonPhrase());
			Oscillator source = oscillator.getSource();
			source.removeListener(oscillator);
		}
	}
	private static void upload(InputStream input_stream) {
		TextSpliter spliter;
		if (fixed) {
			spliter = new TextSpliter(input_stream, true);
			spliter.setPositions(spliter_positions);
		} else {
			spliter = new TextSpliter(input_stream, false);
			spliter.setDelimiter(spliter_delimiter);
		}
		BualuangQuoteDailyHome home = (BualuangQuoteDailyHome)
					Assign.borrowObject(BualuangQuoteDailyHome.class);
		try {
			//Session session = Assign.getSessionFactory().getCurrentSession();
			while (spliter.ready()) {
				Session session = Assign.getSessionFactory().getCurrentSession();
				session.beginTransaction();
				BualuangQuoteDailyId id = null;
				BualuangQuoteDaily quote = null;;
				try {
					String[] fields = spliter.readLine();
					try {
						id = new BualuangQuoteDailyId(
								fields[map_headers.get("symbol")],
								DateTime.format(spliter_date_parse, fields[map_headers.get("date")]));
						quote = new BualuangQuoteDaily(id,
								Double.valueOf(fields[map_headers.get("open")]),
								Double.valueOf(fields[map_headers.get("high")]),
								Double.valueOf(fields[map_headers.get("low")]),
								Double.valueOf(fields[map_headers.get("close")]),
								Long.valueOf(fields[map_headers.get("volume")]),
								Double.valueOf(fields[map_headers.get("value")]));
					} catch (NumberFormatException e) {
						String symbol = fields[map_headers.get("symbol")];
						if (symbol.equals("BGH-F")) {
							String date =fields[map_headers.get("date")];
							LOG.WARNING(
									new ObjectException(e,"\"%s\",\"$s\"; %s", symbol, date, e.getMessage()));
							session.getTransaction().rollback();
							continue;
						} else {
							session.getTransaction().rollback();
							throw e;
						}
					}
					home.persist(quote);
					session.getTransaction().commit();
				} catch (IOException e) {
					LOG.WARNING(e);
					session.getTransaction().rollback();
				} catch (ConstraintViolationException e) {
					LOG.FINE(new ObjectException(e.getCause(), "%s; %s", 
							e.getMessage(), e.getCause().getMessage()));
					session.getTransaction().rollback();
				} catch (Exception e) {
					session.getTransaction().rollback();
					throw e;
				}
			}
		} catch (IOException e) {
			LOG.WARNING(e);
		} finally {
			Assign.returnObject(home);
		}
	}
	
	/**
	 * Default 
	 * stock.settrade.Market.assign=
	 * stock.settrade.Market.clock=1000 // assign clock = 1s (1000 millisecond)
	 * stock.settrade.Market.try_again=5000 // assign try_again = 5s (1000 millisecond)
	 * stock.settrade.Market.interval_update = 16000; // assign interval_update = 16s (16000 millisecond)
	 * stock.settrade.Market.uri=http://www.settrade.com/C13_MarketSummary.jsp?detail=SET
	 * stock.settrade.Market.incorrect_path=<java.io.tmpdir>
	 */
	@Override
	public void initOscillator() {
		if (oscillator != null)
			return;
		long clock = assign.getLongProperty("clock", 900000); // 15m
		Oscillator source = Oscillator.getInstance(clock);
		long date = setUpdate(QuoteDaily.getLastUpdate());
		long time = DateTime.time(
				assign.getProperty("schedule", "19:30:00")).getTime();
		//System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(time));
		long datetime = DateTime.merge(date, time).getTime();
		//System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(datetime));
		long try_again = (long) Math.ceil(assign.getLongProperty("try_again", clock * 2) / (double) clock);
		if (try_again == 0)
			try_again = 1;
		oscillator = new OscillatorDivideFilter(source, this, try_again, datetime);
	}
	
	public static void main(String[] args) {
		//Date date = DateTime.date("2014-06-20");
		//BoardDaily.getInstance().getWebPage(date);
		CommandLine line = commandline(args);
		// Main operation mode:
		int main_option = 0;
		if (line.hasOption("date"))
			main_option += 1;
		if (line.hasOption("file"))
			main_option += 1;
		if (line.hasOption("schedule"))
			main_option += 1;
		if (line.hasOption("restore"))
			main_option += 1;
		if (line.hasOption("help") || (main_option > 1)) {
			usage();
			return;
		}
		boolean show_name = line.hasOption("show");
		if (line.hasOption("schedule")) {
			QuoteDaily.getInstance().initOscillator();
			Oscillator.startAll();
		} else if (line.hasOption("restore")) {
			File filepath = new File(line.getOptionValue("restore"));
			if (filepath.isFile()) {
				load(filepath);
			} else {
				File[] files = filepath.listFiles();
				Arrays.sort(files, new Comparator<File>() {
				    public int compare(File file_1, File file_2)
				    {
				        return Long.valueOf(file_1.lastModified()).compareTo(file_2.lastModified());
				    } });
				for (int index = 0; index < files.length; index++) {
					load(files[index]);
				}
			}
		} else if (line.hasOption("date")) {
			Response response = QuoteDaily.getInstance()
					.getWebPage(DateTime.date(line.getOptionValue("date")));
			if (response.getStatusCode() == 200)
				print(response.getContent(), show_name);
			else
				response.print();
		} else if (line.hasOption("file")) {
			try {
				//FileInputStream input_Stream = new FileInputStream(line.getOptionValue("file"));
				print(new FileInputStream(line.getOptionValue("file")), show_name);
			} catch (FileNotFoundException e) {
				LOG.SEVERE(e);
			}
		} 
	}
	private static void load(File file) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(file.getName());
		try {
			upload(new FileInputStream(file));
			buffer.append("; SUCCESS");
		} catch (FileNotFoundException e) {
			buffer.append("; FAIL");
			LOG.SEVERE(e);
		}
		System.out.println(buffer.toString());
	}
	private static void print(InputStream input_stream, boolean show_name) {
		TextSpliter spliter;
		if (fixed) {
			spliter = new TextSpliter(input_stream, true);
			spliter.setPositions(spliter_positions);
		} else {
			spliter = new TextSpliter(input_stream, false);
			spliter.setDelimiter(spliter_delimiter);
		}
		try {
			String delimiter = " ";
			if (!spliter_delimiter.equals("\\s"))
				delimiter = spliter_delimiter.substring(0, 1);
			StringBuffer buffer = new StringBuffer();
			while (spliter.ready()) {
				buffer.delete(0, buffer.length());
				try {
					String[] fields = spliter.readLine();
					for (int index = 0; index < fields.length; index++) {
						if (show_name) {
							buffer.append(spliter_headers[index]);
							buffer.append("=\"");
							buffer.append(fields[index]);
							buffer.append("\"");
							buffer.append(delimiter);
						} else {
							buffer.append(fields[index]);
							buffer.append(delimiter);
						}				
					}
					System.out.println(buffer.substring(0, buffer.length() - 1));
				} catch (IOException e) {
					LOG.WARNING(e);
				}
			}
		} catch (IOException e) {
			LOG.WARNING(e);
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
		// Option "--date"
		Option date = OptionBuilder
				.withArgName("yyyy-mm-dd")
				.hasArg()
                .withDescription("extract data from the BLS website on \"date\"")
                .withLongOpt("date")
                .create();
		//options.addOption(date);
		// Option "--file"
		Option file = OptionBuilder
				.hasArg()
				.withArgName("filename")
                .withDescription("extract data from filename")
                .withLongOpt("file")
                .create();
		//options.addOption(file);
		// Option "--help"
		Option help = OptionBuilder
                .withDescription("print this message")
                .withLongOpt("help")
                .create();
		//options.addOption(help);
		// Option "--schedule"
		Option schedule = OptionBuilder
                .withDescription("schedule")
                .withLongOpt("schedule")
                .create();
		//options.addOption(schedule);
		// Option "--file"
		Option restore = OptionBuilder
				.hasArg()
				.withArgName("path")
                .withDescription("restore to Database")
                .withLongOpt("restore")
                .create();
		//options.addOption(file);
		group.addOption(date);
		group.addOption(file);
		group.addOption(help);
		group.addOption(schedule);
		group.addOption(restore);
		group.setRequired(true);
		options.addOptionGroup(group);
		Option show = OptionBuilder
                .withDescription("show variable name")
                .withLongOpt("show")
                .create();
		options.addOption(show);
		return options;
	}
	private static void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("QuoteDaily", options);
	}
	
}
