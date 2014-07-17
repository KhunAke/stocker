package com.javath.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.naming.InitialContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.hibernate.SessionFactory;

import com.javath.logger.LOG;

public class Assign extends Instance {
	
	private static final Properties system;
	private static final Map<File,Assign> instances;
	//
	public static final Locale th_TH;
	public static final Locale en_US;
	//
	public static final String File_Separator;
	public static final String Line_Separator;
	public static final String Path_Separator;
	//
	private static final File assign_file;
	//
	public static final String directory;
	public static final String home;
	public static final String temp;
	public static final String etc;
	public static final String var;
	public static final String log;
	//
	public static final String application;
	public static boolean debug = true;
	//
	public static final String charset;
	//
	private static final Preferences preferences;
	private static final AES aes;
	// key is classname
	private static final Map<String, ObjectPool<Object>> map_pool;
	//
	private static final Options options;
	
	static {
		system = System.getProperties();
		instances = new HashMap<File, Assign>();
		//
		th_TH = Locale.forLanguageTag("th-TH");
		en_US = Locale.US;
		//
		File_Separator = system.getProperty("file.separator");
		Line_Separator = system.getProperty("line.separator");
		Path_Separator = system.getProperty("path.separator");
		//
		String default_properties = system.getProperty("user.dir") + File_Separator +
				"etc" + File_Separator +
				"util" + File_Separator +
				"Assign.properties";
		assign_file = new File(system.getProperty("com.javath.util.Assign", default_properties));
		// Loading file properties
		Properties properties = new Properties();
		try {
			FileInputStream file_properties = new FileInputStream(assign_file);
			properties.load(file_properties);
			file_properties.close();
		} catch (FileNotFoundException e) {
			LOG.WARNING(e);
		} catch (IOException e) {
			LOG.WARNING(e);
		} finally {
			// System path
			directory = properties.getProperty("directory",
					system.getProperty("user.dir"));
			home = properties.getProperty("home",
					system.getProperty("user.home"));
			temp = properties.getProperty("temp",
					system.getProperty("java.io.tmpdir"));
			// Custom path
			String path_etc = directory + File_Separator + "etc";
			etc = properties.getProperty("etc", path_etc);
			String path_var = directory + File_Separator + "var";
			var = properties.getProperty("var", path_var);
			String path_log = var + File_Separator + "log";
			log = properties.getProperty("log", path_log);
			//
			charset = properties.getProperty("charset",
					system.getProperty("file.encoding"));
			//
			initVM_argument(properties);
			application = properties.getProperty("application", 
					system.getProperty("application", "java"));
			try {
				debug = getBooleanProperty(properties, "debug");
			} catch (Exception e) {
				LOG.CONFIG(new ObjectException(e,
						"Type mismatch: \"%s\" in \"%s\"", "debug", assign_file.getAbsolutePath()));
			} 
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
	        map_pool = new HashMap<String, ObjectPool<Object>>();
	        instances.put(assign_file, new Assign(assign_file, properties));
	        
	        options = buildOptions();
		}
	}
	public static void initVM_argument(Properties properties) {
		String vm_argument = null; // variable temporary 
		vm_argument = getProperty(properties, "java.naming.factory.initial");
		if (vm_argument == null)
			system.setProperty("java.naming.factory.initial", 
					"com.javath.util.ContextFactory");
		vm_argument = getProperty(properties, "java.util.logging.config.file");
		if (vm_argument == null)
			system.setProperty("java.util.logging.config.file", 
					etc + File_Separator + "logging.properties");
	}
	private static String getProperty(Properties properties, String key) {
		return properties.getProperty(key, system.getProperty(key));
	}
	private static String getProperty(Properties properties, String key, String default_value) {
		return properties.getProperty(key, system.getProperty(key, default_value));
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
		throw new ObjectException("For input string: \"%s\"", value);
	}
	private static boolean getBooleanProperty(Properties properties, String key, boolean default_value) {
		String value = properties.getProperty(key, 
				system.getProperty(key, default_value?"1":"0"));
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
		throw new ObjectException("For input string: \"%s\"", value);
	}
	
	public static Object forConstructor(String classname, String... arguments) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(classname);
			if (arguments.length == 0)
				return clazz.newInstance();
			else
				return forConstructor(clazz, arguments);
		} catch (ClassNotFoundException e) {
			return forMethod(classname.substring(0, classname.lastIndexOf('.')), 
					classname.substring(classname.lastIndexOf('.') + 1), arguments);
		} catch (InstantiationException e) {
			return forMethod(classname, "getInstance");
		} catch (IllegalAccessException e) {
			return forMethod(classname, "getInstance");
		}
	}
	private static Object forConstructor(Class<?> classname, String... arguments) {
		boolean array = false;
		Class<?>[] type_arguments = null;
		Class<?>[] types = null;
		Constructor<?> constructor = null;
		while (true) {
			if (array) {
				types = new Class<?>[1];
				types[0] = String[].class;
			} else {
				types = new Class<?>[arguments.length];
				for (int index = 0; index < arguments.length; index++) {
					Object argument = arguments[index];
					types[index] = argument.getClass();
				}
				type_arguments = types;
			}
			try {
				constructor = classname.getConstructor(types);
				break;
			} catch (NoSuchMethodException e) {
				if (!array) {
					array = true;
					continue;
				} else
					return forMethod(classname, "getInstance", type_arguments, arguments);
			} catch (SecurityException e) {
				LOG.SEVERE(e);
			}
			break;
		}
		try {
			if (array)
				return constructor.newInstance((Object) arguments);
			else 
				return constructor.newInstance((Object[]) arguments);
		} catch (InstantiationException e) {
			LOG.SEVERE(e);
		} catch (IllegalAccessException e) {
			LOG.SEVERE(e);
		} catch (IllegalArgumentException e) {
			LOG.SEVERE(e);
		} catch (InvocationTargetException e) {
			LOG.SEVERE(e);
		}
		return null;
	}
	public static Object forMethod(String classname, String name, String... arguments) {
		try {
			Class<?> clazz = Class.forName(classname);
			if (arguments.length == 0)
				return forMethod(clazz, name, new Class<?>[] {});
			else {
				Class<?>[] types = new Class<?>[arguments.length];
				for (int index = 0; index < arguments.length; index++) {
					Object argument = arguments[index];
					types[index] = argument.getClass();
				}
				return forMethod(clazz, name, types, arguments);
			}		
		} catch (ClassNotFoundException e) {
			LOG.CONFIG(
					new ObjectException(e, String.format("Classloader \"%1$s\" and \"%1$s.%2$s\" not found", classname, name)));
		}
		return null;
	}
	private static Object forMethod(Class<?> classname, String name, Class<?>[] types, String... arguments) {
		Method method = null;
		boolean array = false;
		while (true) {
			if (array) {
				types = new Class<?>[1];
				types[0] = String[].class;
			}
			try {
				if (arguments.length == 0)
					method = classname.getMethod(name);
				else 
					method = classname.getMethod(name, types);
			} catch (NoSuchMethodException e) {
				if ((arguments.length != 0) && !(array)) {
					array = true;
					continue;
				} 
				LOG.SEVERE(e);
			} catch (SecurityException e) {
				LOG.SEVERE(e);
			}
			break;
		}
		try {
			if (array)
				return method.invoke(null, (Object) arguments);
			else 
				return method.invoke(null, (Object[]) arguments);
		} catch (IllegalAccessException e) {
			LOG.SEVERE(e);
		} catch (IllegalArgumentException e) {
			LOG.SEVERE(e);
		} catch (InvocationTargetException e) {
			LOG.SEVERE(e);
		}
		return null;
	}
	
	public static Object borrowObject(Class<?> classname) {
		return borrowObject(classname.getCanonicalName());
	}
	public static Object borrowObject(String classname) {
		ObjectPool<Object> pool = map_pool.get(classname);
		try {
			if (pool == null) {
				pool = initialObjectPool(classname);
				map_pool.put(classname, pool);
			}
			return pool.borrowObject();
		} catch (NoSuchElementException e) {
			LOG.SEVERE(e);
		} catch (IllegalStateException e) {
			LOG.SEVERE(e);
		} catch (Exception e) {
			LOG.SEVERE(e);
		}
		return null;
	}
	public static void returnObject(Object object) {
		String classname = null;
		try {
			classname = object.getClass().getCanonicalName();
		} catch (NullPointerException e) {
			return;
		}
		ObjectPool<Object> pool = map_pool.get(classname);
		try {
			if (pool == null) {
				pool = initialObjectPool(classname);
				map_pool.put(classname, pool);
			}
			pool.returnObject(object);
		} catch (Exception e) {
			LOG.SEVERE(e);
		}
		
	}
	private final static ObjectPool<Object> initialObjectPool(String classname) {
		Assign assign = Assign.getInstance();
		/**/
		Config config = new GenericObjectPool.Config();
		config.lifo = assign.getBooleanProperty("config_lifo", true);
		config.maxActive = (int) assign.getLongProperty("config_maxActive", 8);
	    config.testOnBorrow = assign.getBooleanProperty("config_testOnBorrow", false);
	    config.testOnReturn = assign.getBooleanProperty("config_testOnReturn", false);
	    config.testWhileIdle = assign.getBooleanProperty("config_testWhileIdle", false);
		config.maxIdle = (int) assign.getLongProperty("config_maxIdle", 8);
	    config.minIdle = (int) assign.getLongProperty("config_minIdle", 0);
	    config.maxWait = assign.getLongProperty("config_maxWait", -1);
	    config.numTestsPerEvictionRun =
	    		(int) assign.getLongProperty("config_numTestsPerEvictionRun", 3);
	    config.timeBetweenEvictionRunsMillis = 
	    		assign.getLongProperty("config_timeBetweenEvictionRunsMillis", -1);
	    config.minEvictableIdleTimeMillis = 
	    		assign.getLongProperty("config_minEvictableIdleTimeMillis", 1800000);
	    /**/
		return new GenericObjectPool<Object>(
				new PoolableObjectFactory<Object>() {
					private String classname;
					@Override
					public Object makeObject() 
							throws Exception {
						return getInstance();
					}
					@Override
					public void activateObject(Object object) 
							throws Exception {}
					@Override
					public void passivateObject(Object object) 
							throws Exception {}
					@Override
					public boolean validateObject(Object object) {
						return true;
					}
					@Override
					public void destroyObject(Object object) 
							throws Exception {}
					public PoolableObjectFactory<Object> setClassname(String classname) {
						this.classname = classname;
						return this;
					}
					private Object getInstance() {
						return Assign.forConstructor(classname);
					}
				}.setClassname(classname), config) {
					@Override
					public Object borrowObject() throws Exception {
						Object result = super.borrowObject();
						if (ObjectPoolable.class.isAssignableFrom(result.getClass()))
							((ObjectPoolable) result).initialObject();
						return result;
					}
		};
	}
	
 	public static SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			LOG.SEVERE(e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}
	
	public static String md5(String message) {
		try {
			byte[] message_bytes = message.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(message_bytes);
			return Base64.encodeBase64String(digest);
		} catch (UnsupportedEncodingException e) {
			throw new ObjectException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		}
	}
	public static String randomMD5() {
		char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder string_builder = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 22; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    string_builder.append(c);
		}
		return string_builder.toString() + "==";
	}
	private static String encrypt(String message) {
		return aes.encrypt(message);
	}
	private static String decrypt(String message) {
		return aes.decrypt(message);
	}
	public static String hex(long data) {
		return Long.toHexString(data);
	}
	public static long hex(String data) {
		try {
			char[] char_buffer = data.toCharArray();
			if ((char_buffer.length % 2) != 0) {
				char[] temp_buffer = new char[char_buffer.length + 1];
				System.arraycopy(char_buffer, 0, temp_buffer, 1, char_buffer.length);
				temp_buffer[0] = '0';
				char_buffer = temp_buffer;
			}
			byte[] buffer = Hex.decodeHex(char_buffer);
			long result = 0;
			for (int index = 0; index < buffer.length; index++) {
				result = result << 8;
				result += ((int) buffer[index] & 0x00FF);
			}
			return result;
		} catch (DecoderException e) {
			throw new ObjectException(e);
		}
		
	}
	
	private File file;
	private Properties properties;
	
	private Assign(File file, Properties properties) {
		setFile(file);
		setProperties(properties);
	}
	private Assign(File file) {
		Properties properties = new Properties();
		try {
			FileInputStream file_properties = new FileInputStream(file);
			properties.load(file_properties);
			file_properties.close();
		} catch (FileNotFoundException e) {
			CONFIG(e);
		} catch (IOException e) {
			CONFIG(e);
		} finally {
			setFile(file);
			setProperties(properties);
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
	public static Assign getInstance(Class<?> classname, String default_Properties) {
		return getInstance(new File(system.getProperty(classname.getCanonicalName(), default_Properties)));
	}
	public static Assign getInstance(String file_Properties) {
		return getInstance(new File(file_Properties));
	}
	public static Assign getInstance() {
		return getInstance(assign_file);
	}
	
	private void setFile(File file) {
		this.file = file;
	}
	private void setProperties(Properties properties) {
		this.properties = properties;
	}
	public String getProperty(String key) {
		return getProperty(properties, key, "");
	}
	public String getProperty(String key, String default_value) {
		return getProperty(properties, key, default_value);
	}
	public boolean getBooleanProperty(String key) {
		try {
			return getBooleanProperty(properties, key, false);
		} catch (Exception e) {
			LOG.CONFIG(new ObjectException(e,
					"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
			return false;
		}
	}
	public boolean getBooleanProperty(String key, boolean default_value) {
		try {
			return getBooleanProperty(properties, key, default_value);
		} catch (Exception e) {
			LOG.CONFIG(new ObjectException(e,
					"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
			return false;
		}
	}
	public long getLongProperty(String key) {
		try {
			return Long.valueOf(getProperty(properties, key, "0"));
		} catch (Exception e) {
			LOG.CONFIG(new ObjectException(e,
					"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
			return 0;
		}
	}
	public long getLongProperty(String key, long default_value) {
		try {
			return Long.valueOf(getProperty(properties, key, String.valueOf(default_value)));
		} catch (Exception e) {
			LOG.CONFIG(new ObjectException(e,
					"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
			return 0;
		}
	}
	public double getDoubleProperty(String key) {
		try {
			return Double.valueOf(getProperty(properties, key, "0.0"));
		} catch (Exception e) {
			LOG.CONFIG(new ObjectException(e,
					"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
			return 0.0;
		}
	}
	public double getDoubleProperty(String key, double default_value) {
		try {
			return Double.valueOf(getProperty(properties, key, String.valueOf(default_value)));
		} catch (Exception e) {
			LOG.CONFIG(new ObjectException(e,
					"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
			return 0.0;
		}
	}
	public String getSecureProperty(String key) {
		String value = getProperty(key);
		if ((value == null) || value.isEmpty())
			return "";
		else
			return decrypt(value);
	}
	public String getConfigPathProperty(String key) {
		return getReferencePathProperty(etc, key);
	}
	public String getConfigPathProperty(String key, String default_value) {
		return getReferencePathProperty(etc, key, default_value);
	}
	public String getReferencePathProperty(String reference, String key) {
		String path = getProperty(key);
		try {
			if (path.indexOf(File_Separator) == -1)
				path = reference + File_Separator + path;
		} catch (NullPointerException e) {
			return null;
		}
		return path;
	}
	public String getReferencePathProperty(String reference, String key, String default_value) {
		String path = getProperty(key, default_value);
		if (path.indexOf(File_Separator) == -1)
			path = reference + File_Separator + path;
		return path;
	}
	
	public void warning(Throwable throwable, String key) {
		LOG.CONFIG(new ObjectException(throwable,
				"Type mismatch: \"%s\" in \"%s\"", key, file.getAbsolutePath()));
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
