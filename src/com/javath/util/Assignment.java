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

public final class Assignment extends Instance {
	
	private static final Properties system;
	private static final Preferences preferences;
	private static final Map<File,Assignment> instances;
	private static final File file_config;
	private static final Options options;
	//
	public static final String application;
	public static final boolean debug;
	private static final AES aes;
	
	static {
		system = System.getProperties();
		instances = new HashMap<File, Assignment>();
		String default_config = system.getProperty("user.dir") + system.getProperty("file.separator") +
				"etc" + system.getProperty("file.separator") +
				"configuration.properties";
		file_config = new File(system.getProperty("configuration", default_config));
		options = buildOptions();
		
		// Loading file properties
		Properties properties = new Properties();
		try {
			FileInputStream file_properties = new FileInputStream(file_config);
			properties.load(file_properties);
			file_properties.close();
		} catch (FileNotFoundException e) {
			LOG.WARNING(e);
		} catch (IOException e) {
			LOG.WARNING(e);
		} finally {
			application = properties.getProperty("application", 
					system.getProperty("application", "java"));
			debug = getLoggingDebug(properties);
			
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
	        
	        aes = new AES(md5(shared_key));
		}
	}
	private static boolean getLoggingDebug(Properties properties) {
		String debug = properties.getProperty("debug", 
				system.getProperty("debug", "false"));
		if (debug.equals("1") || debug.equalsIgnoreCase("true"))
			return true;
		if (debug.equals("0") || debug.equalsIgnoreCase("false"))
			return false;
		LOG.CONFIG("Unknow debug is \"%s\".", debug);
		return false;
	}
	
	public static Assignment getInstance() {
		return getInstance(file_config);
	}
	public static Assignment getInstance(String file) {
		if ((file == null) || (file == ""))
			return getInstance();
		else
			return getInstance(new File(file));
	}
	public static Assignment getInstance(File file) {
		Assignment result = instances.get(file);
		if (result == null) {
			result = new Assignment(file);
			instances.put(file, result);
		}
		return result;
	}
	
	private final Properties properties;
	
	public final String FILE_SEPARATOR;
	public final String LINE_SEPARATOR;
	public final String PATH_SEPARATOR;
	
	public final String directory;
	public final String home;
	public final String temp;
	public final String etc;
	public final String var;
	public final String log;
	
	public final String charset;
	
	private Assignment(File file) {
		properties = new Properties();
		try {
			FileInputStream file_properties = new FileInputStream(file);
			properties.load(file_properties);
			file_properties.close();
		} catch (FileNotFoundException e) {
			WARNING(e);
		} catch (IOException e) {
			WARNING(e);
		} finally {
			// Separator
			FILE_SEPARATOR = getProperty("file.separator");
			LINE_SEPARATOR = getProperty("line.separator");
			PATH_SEPARATOR = getProperty("path.separator");
			// System path
			directory = getProperty("user.dir");
			home = getProperty("user.home");
			temp = getProperty("java.io.tmpdir");
			// Custom path
			String path_etc = directory + FILE_SEPARATOR + "etc";
			etc = getProperty("user.etc", path_etc);
			String path_var = directory + FILE_SEPARATOR + "var";
			var = getProperty("user.var", path_var);
			String path_log = var + FILE_SEPARATOR + "log";
			log = getProperty("user.log", path_log);
			
			charset = getProperty("file.encoding");
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key, system.getProperty(key));
	}
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, system.getProperty(key, defaultValue));
	}
	public boolean getPropertyBoolean(String key) {
		String value = getProperty(key);
		if (value.equals("1") || value.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}
	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		String value = getProperty(key, defaultValue?"1":"0");
		if (value.equals("1") || value.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}
	public long getPropertyLong(String key) {
		return Long.valueOf(getProperty(key));
	}
	public long getPropertyLong(String key, long defaultValue) {
		return Long.valueOf(getProperty(key, String.valueOf(defaultValue)));
	}
	public double getPropertyDouble(String key) {
		return Double.valueOf(getProperty(key));
	}
	public double getPropertyDouble(String key, double defaultValue) {
		return Double.valueOf(getProperty(key, String.valueOf(defaultValue)));
	}
	public String getPropertySecure(String key) {
		String value = getProperty(key);
		if ((value == null) || value.isEmpty())
			return "";
		else
			return decrypt(value);
	}
	public String getPropertyConfigPath(String key) {
		return getPropertyConfigPath(etc, key);
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
	
	public static Preferences getPreferenceNode(String node) {
		return preferences.node(node);
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
			Assignment assign;
			if (line.hasOption("file"))
				assign = Assignment.getInstance(line.getOptionValue("file"));
			else
				assign = Assignment.getInstance();
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
		formatter.printHelp("Assignment", options);
	}
	
}
