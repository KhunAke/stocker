package trigger;

import com.javath.logger.LOG;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorDivideFilter;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorLoader;
import com.javath.util.DateTime;

public class TestOscillator implements OscillatorLoader {
	
	public TestOscillator getInstance(String... args) {
		return new TestOscillator(args[0]);
	}
	
	String name;
	
	private TestOscillator(String name) {
		this.name = name;
		System.out.printf("Create %s%n", name);
	}
	
	@Override
	public void action(OscillatorEvent event) {
		System.out.printf("Begin %s: %s%n", name, DateTime.string(event.getTimestamp()));
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			LOG.SEVERE(e);
		}
		System.out.printf("End %s: %s%n", name, DateTime.string(event.getTimestamp()));
	}

	@Override
	public void initOscillator() {
		long clock = 900000;
		Oscillator source = Oscillator.getInstance(clock);
		long date = DateTime.date().getTime();
		long time = DateTime.time("00:00:00").getTime();
		System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(time));
		long datetime = DateTime.merge(date, time).getTime();
		System.out.printf("Schedule: \"%s\"%n", DateTime.timestamp(datetime));
		new OscillatorDivideFilter(source, this, 2, datetime);
	}

}
