package mapping;

import java.util.Date;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.bualuang.BoardDaily;
import com.javath.mapping.BualuangBoardDaily;
import com.javath.mapping.BualuangBoardDailyHome;
import com.javath.mapping.BualuangBoardDailyId;
import com.javath.trigger.Oscillator;
import com.javath.util.NotificationEvent;
import com.javath.util.NotificationListener;

public class TestBualuangBoardDaily implements NotificationListener {

	public static void main(String[] args) {
		/**
		Properties system = System.getProperties();
		system.setProperty("java.naming.factory.initial", 
				"com.javath.util.ContextFactory");
		//		"org.hibernate.engine.jndi.internal.JndiService");
		
		BualuangBoardDailyHome home = new BualuangBoardDailyHome();
		BualuangBoardDailyId id = new BualuangBoardDailyId("PPT", new Date());
		BualuangBoardDaily bualuang_board_daily = new BualuangBoardDaily(id);
		SessionFactory session_factory;
		try {
			session_factory = (SessionFactory) new InitialContext()
				.lookup("SessionFactory");
			Session session = session_factory.getCurrentSession();
			session.beginTransaction();
			home.persist(bualuang_board_daily);
			session.getTransaction().commit();
			
			//session_factory.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**/
		BoardDaily board = BoardDaily.getInstance();
		board.initOscillator();
		TestBualuangBoardDaily test = new TestBualuangBoardDaily();
		board.addListener(test);
		Oscillator.startAll();
	}

	@Override
	public void notify(NotificationEvent event) {
		System.out.println(event);
	}

}
