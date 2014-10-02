package settrade;

import com.javath.settrade.Click2Win;
import com.javath.settrade.Market;
import com.javath.settrade.MarketStatusScreen;
import com.javath.settrade.flash.DataProvider;

public class TestClick2Win {

	public static void main(String[] args) {
		Market market = Market.getInstance();
		market.initOscillator();
		MarketStatusScreen.getInstance();
		//Click2Win client = Click2Win.getInstance("khunake", "kill4ake");
		/**
		DataProvider data;
		//data = client.buyOrder("PTT", 360, 100);
		//System.out.println(data);
		data = client.accountInfo();
		System.out.println(data);
		data = client.portfolio();
		System.out.println(data);
		data = client.orderStatus();
		System.out.println(data);
		//Click2Win client = Click2Win.getInstance("username", "password");
		/**/
		//client.save();
	}

}
