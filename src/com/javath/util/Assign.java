package com.javath.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;

import com.javath.logger.LOG;

public class Assign extends Instance {
	
	private static final Properties system;
	private static final Map<File,Assign> instances;
	//
	public static final String FILE_SEPARATOR;
	public static final String LINE_SEPARATOR;
	public static final String PATH_SEPARATOR;
	//
	private static final File file_Properties;
	//
	public static final String application;
	public static final boolean debug;
	//
	public static final String directory;
	public static final String home;
	public static final String temp;
	public static final String etc;
	public static final String var;
	public static final String log;
	//
	public static final String charset;
	//
	private static final Preferences preferences;
	private static final AES aes;
	//
	private static final Options options;
	
	static {
		system = System.getProperties();
		instances = new HashMap<File, Assign>();
		//
		FILE_SEPARATOR = system.getProperty("file.separator");
		LINE_SEPARATOR = system.getProperty("line.separator");
		PATH_SEPARATOR = system.getProperty("path.separator");
		//
		String default_Properties = system.getProperty("user.dir") + FILE_SEPARATOR +
				"etc" + FILE_SEPARATOR +
				"util" + FILE_SEPARATOR +
				"Assignment.properties";
		file_Properties = new File(system.getProperty("com.javath.util.Assignment", default_Properties));
		// Loading file properties
		Properties properties = new Properties();
		try {
			FileInputStream file_properties = new FileInputStream(file_Properties);
			properties.load(file_properties);
			file_properties.close();
		} catch (FileNotFoundException e) {
			LOG.WARNING(e);
		} catch (IOException e) {
			LOG.WARNING(e);
		} finally {
			application = properties.getProperty("application", 
					system.getProperty("application", "java"));
			debug = getBooleanProperty(properties, "debug");
			// System path
			directory = properties.getProperty("directory",
					system.getProperty("user.dir"));
			home = properties.getProperty("home",
					system.getProperty("user.home"));
			temp = properties.getProperty("temp",
					system.getProperty("java.io.tmpdir"));
			// Custom path
			String path_etc = directory + FILE_SEPARATOR + "etc";
			etc = properties.getProperty("etc", path_etc);
			String path_var = directory + FILE_SEPARATOR + "var";
			var = properties.getProperty("var", path_var);
			String path_log = var + FILE_SEPARATOR + "log";
			log = properties.getProperty("log", path_log);
			//
			charset = properties.getProperty("charset",
					system.getProperty("file.encoding"));
			//
			String root = properties.getProperty("preferences", 
					system.getProperty("preferences", "user")).toLowerCase();
			if (root.equals("system"))
	        	preferences = Preferences.systemRoot().node(application);
	        else if (root.equals("user"))
	        	preferences = Preferences.userRoot().node(application);
	        else {
	        	LOG.WARNING("Preference nodes are \"user\" preferences or \"system\" preferences");
	        	preferences = Preferences.userRoot().node(application);
	        }
			String shared_key = preferences.get("SharedKey", "");
	        if (shared_key.equals("")) {
	        	shared_key = randomMD5();
	        	preferences.put("SharedKey", shared_key);
	        }
	        //
	        aes = new AES(md5(shared_key));
	        instances.put(file_Properties, new Assign(properties));
	        options = buildOptions();
		}
	}
	private static String getProperty(Properties properties, String key) {
		return properties.getProperty(key, system.getProperty(key));
	}
	private static String getProperty(Properties properties, String key, String defaultValue) {
		return properties.getProperty(key, system.getProperty(key, defaultValue));
	}
	private static boolean getBooleanProperty(Properties properties, String key) {
		String value = properties.getProperty(key, 
				system.getProperty(key, "0"));
		if (    value.equals("1") || 
				value.equalsIgnoreCase("true") || 
				value.equalsIgnoreCase("t") ||
				value.equalsIgnoreCase("on") )
			return true;
		if (	value.equals("0") || 
				value.equalsIgnoreCase("false") || 
				value.equalsIgnoreCase("f") ||
				value.equalsIgnoreCase("off") )
			return false;
		LOG.CONFIG("Type mismatch: cannot convert from \"%s\" to boolean", value);
		return false;
	}
	private static boolean getBooleanProperty(Properties properties, String key, boolean defaultValue) {
		String value = properties.getProperty(key, 
				system.getProperty(key, defaultValue?"1":"0"));
		if (    value.equals("1") || 
				value.equalsIgnoreCase("true") || 
				value.equalsIgnoreCase("t") ||
				value.equalsIgnoreCase("on") )
			return true;
		if (	value.equals("0") || 
				value.equalsIgnoreCase("false") || 
				value.equalsIgnoreCase("f") ||
				value.equalsIgnoreCase("off") )
			return false;
		LOG.CONFIG("Type mismatch: cannot convert from \"%s\" to boolean", value);
		return false;
	}
	
	public static String md5(String message) {
		try {
			byte[] bytesOfMessage = message.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfMessage);
			return Base64.encodeBase64String(digest);
		} catch (UnsupportedEncodingException e) {
			throw new ObjectException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		}
	}
	public static String randomMD5() {
		char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 22; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		return sb.toString() + "==";
	}
	private static String encrypt(String message) {
		return aes.encrypt(message);
	}
	private static String decrypt(String message) {
		return aes.decrypt(message);
	}
	
	private final Properties properties;
	
	private Assign(Properties properties) {
		this.properties = properties;
	}
	private Assign(File file) {
		this(new Properties());
		try {
			FileInputStream file_properties = new FileInputStream(file);
			properties.load(file_properties);
			file_properties.close();
		} catch (FileNotFoundException e) {
			WARNING(e);
		} catch (IOException e) {
			WARNING(e);
		} 
	}
	
	public static Assign getInstance(File file) {
		Assign result = instances.get(file);
		if (result == null) {
			result = new Assign(file);
			instances.put(file, result);
		}
		return result;
	}
	public static Assign getInstance(String classname, String default_Properties) {
		return getInstance(new File(system.getProperty(classname, default_Properties)));
	}
	public static Assign getInstance(String file_Properties) {
		return getInstance(new File(file_Properties));
	}
	public static Assign getInstance() {
		return getInstance(file_Properties);
	}
	
	public String getProperty(String key) {
		return getProperty(properties, key);
	}
	public String getProperty(String key, String defaultValue) {
		return getProperty(properties, key, defaultValue);
	}
	public boolean getBooleanProperty(String key) {
		return getBooleanProperty(properties, key);
	}
	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		return getBooleanProperty(properties, key, defaultValue);
	}
	public long getPropertyLong(String key) {
		return Long.valueOf(getProperty(properties, key));
	}
	public long getPropertyLong(String key, long defaultValue) {
		return Long.valueOf(getProperty(properties, key, String.valueOf(defaultValue)));
	}
	public double getPropertyDouble(String key) {
		return Double.valueOf(getProperty(properties, key));
	}
	public double getPropertyDouble(String key, double defaultValue) {
		return Double.valueOf(getProperty(properties, key, String.valueOf(defaultValue)));
	}
	public String getPropertySecure(String key) {
		String value = getProperty(key);
		if ((value == null) || value.isEmpty())
			return "";
		else
			return decrypt(value);
	}
	public String getPropertyConfigPath(String key) {
		return getPropertyReferencePath(etc, key);
	}
	public String getPropertyConfigPath(String key, String defaultValue) {
		return getPropertyReferencePath(etc, key, defaultValue);
	}
	public String getPropertyReferencePath(String reference, String key) {
		String path = getProperty(key);
		try {
			if (path.indexOf(FILE_SEPARATOR) == -1)
				path = reference + FILE_SEPARATOR + path;
		} catch (NullPointerException e) {
			return null;
		}
		return path;
	}
	public String getPropertyReferencePath(String reference, String key, String defaultValue) {
		String path = getProperty(key, defaultValue);
		if (path.indexOf(FILE_SEPARATOR) == -1)
			path = reference + FILE_SEPARATOR + path;
		return path;
	}
	
	public void printProperty() {
		Set<Object> keys = properties.keySet();
		for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			System.out.printf("%s=%s\n", key, properties.get(key));
		}
	}
	public void printSystem() {
		Set<Object> keys = system.keySet();
		for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			System.out.printf("%s=%s\n", key, system.get(key));
		}
	}
	
	public static void main(String[] args) {
		CommandLine line = commandline(args);
		if (line.hasOption("help")) {
			usage();
			return;
		}
		// Main operation mode:
		int main_option = 0;
		if (line.hasOption("generate"))
			main_option += 1;
		if (line.hasOption("property"))
			main_option += 1;
		if (line.hasOption("help") || (main_option > 1)) {
			usage();
			return;
		}
		if (line.hasOption("generate")) {
			String plaintext = line.getOptionValue("generate");
			System.out.printf("secure=\"%s\"\n", encrypt(plaintext));
		}
		if (line.hasOption("property")) {
			String key = line.getOptionValue("property");
			Assign assign;
			if (line.hasOption("file"))
				assign = Assign.getInstance(line.getOptionValue("file"));
			else
				assign = Assign.getInstance();
			String value = assign.getProperty(key);
			if (value == null)
				System.out.printf("%s=\n",key);
			else
				System.out.printf("%s=%s\n", key, assign.getProperty(key));
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
		// Option "--generate"
		Option generate = OptionBuilder
				.withArgName("plaintext")
				.hasArg()
                .withDescription("generate secure text from plain text")
                .withLongOpt("generate")
                .create();
		//options.addOption(generate);
		Option property = OptionBuilder
				.hasArg()
				.withArgName("key")
                .withDescription("show value of \"key\" property")
                .withLongOpt("property")
                .create();
		//options.addOption(file);
		Option file = OptionBuilder
				.hasArg()
				.withArgName("filename")
                .withDescription("file property")
                .withLongOpt("file")
                .create();
		// Option "--help"
		Option help = OptionBuilder
                .withDescription("print this message")
                .withLongOpt("help")
                .create();
		group.addOption(generate);
		group.addOption(property);
		group.addOption(help);
		group.setRequired(true);
		//options.addOption(help);
		options.addOptionGroup(group);
		options.addOption(file);
		return options;
	}
	private static void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Assign", options);
	}
	
}
