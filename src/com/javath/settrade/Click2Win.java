package com.javath.settrade;

import com.javath.set.Broker;
import com.javath.settrade.flash.BrokerStreaming;
import com.javath.util.Assign;

public class Click2Win extends BrokerStreaming {
	
	private static final int EXTEND_ID;
	
	static {
		EXTEND_ID = getExtendId(Assign.classname());
	}
	
	public static Click2Win getInstance(String username, String password) {
		Broker broker = Broker.getInstance(username, EXTEND_ID);
		if (broker.checkPassword(password))
			;
		return null;
	}
	
	private Click2Win(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	

}
