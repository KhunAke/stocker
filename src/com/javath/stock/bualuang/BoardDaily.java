package com.javath.stock.bualuang;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.javath.http.Browser;
import com.javath.http.Response;
import com.javath.http.State;
import com.javath.logger.LOG;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorDivideFilter;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorSchedule;
import com.javath.trigger.OscillatorScheduleFilter;
import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.Service;
import com.javath.util.TextSpliter;

public class BoardDaily extends Instance implements OscillatorSchedule {
	
	private final static Assign assign;
	private final static String date_board;
	private final static Locale date_locale;
	private final static boolean fixed;
	private final static String[] spliter_headers;
	private final static int[] spliter_positions;
	private final static String spliter_delimiter;
	private final static BoardDaily instance;
	//
	private static final Options options;

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
		options = buildOptions();
	}
	
	public static BoardDaily getInstance() {
		return instance;
	}
	
	private final State state;
	private OscillatorDivideFilter schedule;
	
	private BoardDaily() {
		state = State.borrowObject();
	}
	
	private Response getWebPage(Date date) {
		Browser browser = Browser.borrowObject(state.getCookieStore());
		try {
			browser.address(getURI(date));
			return browser.get();
		} finally {
			Browser.returnObject(browser);
		}
	}
	private String getURI(Date date) {
		return String.format(date_locale, date_board, date);
	}
	
	@Override
	public void action(OscillatorEvent event) {
		System.out.println(DateTime.string(event.getTimestamp()));
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
	public boolean setSchedule() {
		// TODO Auto-generated method stub
		long clock = assign.getLongProperty("clock", 900000);
		Oscillator oscillator = Oscillator.getInstance(clock);
		long date = DateTime.date().getTime();
		long time = DateTime.time(
				assign.getProperty("schedule", "18:30:00")).getTime();
		schedule = new OscillatorDivideFilter(oscillator, this, 2, date + time);
		oscillator.start();
		return false;
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
		if (line.hasOption("help") || (main_option > 1)) {
			usage();
			return;
		}
		boolean show_name = line.hasOption("show");
		if (line.hasOption("schedule")) {
			BoardDaily.getInstance().setSchedule();
		} else if (line.hasOption("date")) {
			Response response = BoardDaily.getInstance()
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
							buffer.append("\",");
						} else {
							buffer.append(fields[index]);
							buffer.append(",");
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
		//options.addOption(file);
		// Option "--file"
		Option file = OptionBuilder
				.hasArg()
				.withArgName("filename")
                .withDescription("extract data from filename")
                .withLongOpt("file")
                .create();
		//options.addOption(file);
		// Option "--file"
		Option help = OptionBuilder
                .withDescription("print this message")
                .withLongOpt("help")
                .create();
		//options.addOption(file);
		// Option "--help"
		Option schedule = OptionBuilder
                .withDescription("schedule")
                .withLongOpt("schedule")
                .create();
		//options.addOption(file);
		group.addOption(date);
		group.addOption(file);
		group.addOption(help);
		group.addOption(schedule);
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
		formatter.printHelp("BoardDaily", options);
	}
	
	@Override
	protected void finalize() throws Throwable {
		State.returnObject(state);
		super.finalize();
	}

}
