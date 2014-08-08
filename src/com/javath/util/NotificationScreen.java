package com.javath.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import com.javath.logger.LOG;
import com.javath.trigger.OscillatorLoader;
import com.javath.util.NotificationEvent.NoteStatus;

public class NotificationScreen implements NotificationListener{
	
	private final static Assign assign;
	private static NotificationScreen instance;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"util" + Assign.File_Separator +
				"notification.properties";
		assign = Assign.getInstance(NotificationScreen.class, default_Properties);
		instance = new NotificationScreen();
	}
	
	public static NotificationScreen getInstance() {
		return instance;
	}
	
	private NotificationScreen() {
		loader();
	}
	
	public void loader() {
		String loader_path = Assign.etc + Assign.File_Separator + "NotificationLoader";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new FileReader(assign.getProperty("NotificationLoader", loader_path)));
			while (reader.ready()) {
				String line = reader.readLine();
				String classname = null;
				String method_name = null;
				String[] arguments = null;
				String pattern_not_arguments = "^\\w+(.\\w+)*\\(\\s*\\)$";
				String pattern_arguments = "^\\w+(.\\w+)*\\(\\s*\\w*\\s*(\\s*,\\s*\\w*)*\\)$";
				if (Pattern.matches(pattern_not_arguments, line)) {
					classname = line.substring(0,line.indexOf('('));
					method_name = "getInstance";
				} else if (Pattern.matches(pattern_arguments, line)) {
					classname = line.substring(0,line.indexOf('('));
					method_name = "getInstance";
					arguments = line.substring(line.indexOf('(') + 1, line.indexOf(')'))
							.replaceAll("\\s", "").split(",");
				}
				Class<?> clazz = null;
				NotificationSource object = null;
				try {
					clazz = Class.forName(classname);
					if (NotificationSource.class.isAssignableFrom(clazz)) {
						if (arguments == null)
							object = (NotificationSource) Assign.forConstructor(classname);
						else
							object = (NotificationSource) Assign.forConstructor(classname, arguments);
					}
				} catch (ClassNotFoundException e) {
					method_name = classname.substring(classname.lastIndexOf('.') + 1);
					classname = classname.substring(0, classname.lastIndexOf('.'));
					try {
						clazz = Class.forName(classname);
						if (OscillatorLoader.class.isAssignableFrom(clazz)) {
							if (arguments == null)
								object = (NotificationSource) Assign.forMethod(classname, method_name);
							else
								object = (NotificationSource) Assign.forMethod(classname, method_name, arguments);
						}
					} catch (ClassNotFoundException ex) {
						LOG.CONFIG(new ObjectException(e, String.format(
								"Classloader \"%1$s\" and \"%1$s.%2$s\" not found", classname, method_name)));
						continue;
					}
				}
				if (object != null) {
					object.addListener(this);
					LOG.INFO("\"%s\" has Listening.",  line.replaceAll("\\s", ""));
				} 
			}
			reader.close();
		} catch (FileNotFoundException e) {
			LOG.CONFIG(e);
		} catch (IOException e) {
			LOG.CONFIG(e);
		} 
	}
	
	@Override
	public void notify(NotificationEvent event) {
		if (event.getStatus().equals(NoteStatus.NOTICE))
			System.out.printf("%s: %s%n", DateTime.timestamp(new Date()), event.getMessage());
		else
			System.out.printf("%s: %s%n", DateTime.timestamp(new Date()), event);
	}

}
