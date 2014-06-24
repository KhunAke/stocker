package util;

import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorListener;
import com.javath.util.DateTime;

public class TestOscillator implements OscillatorListener {
	
	public TestOscillator() {
		Oscillator oscillator = Oscillator.getInstance(1000);
		oscillator.addListener(this);
		oscillator.start();
	}

	@Override
	public void action(OscillatorEvent event) {
		System.out.printf("Event: \"%s\"%n", DateTime.timestamp(event.getTimestamp()));
	}
	
	public static void main(String[] args) {
		new TestOscillator();
	}
	
}
