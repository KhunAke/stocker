package util;

import java.util.Calendar;

import com.javath.util.DateTime;

public class TestDateTime {

	public static void main(String[] args) {
		Calendar calendar1 = DateTime.borrowCalendar();
		Calendar calendar2 = DateTime.borrowCalendar();
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		DateTime.returnCalendar(calendar2);
		DateTime.returnCalendar(calendar1);
	}

}
