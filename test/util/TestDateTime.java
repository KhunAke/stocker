package util;

import java.util.Calendar;

import com.javath.util.DateTime;

public class TestDateTime {

	public static void main(String[] args) {
		Calendar calendar1 = DateTime.borrowCalendar();
		System.out.println(DateTime.timestamp(calendar1));
		calendar1.clear();
		System.out.println(DateTime.timestamp(calendar1));
		DateTime.returnCalendar(calendar1);
		Calendar calendar2 = DateTime.borrowCalendar();
		System.out.println(DateTime.timestamp(calendar2));
		calendar2.clear();
		System.out.println(DateTime.timestamp(calendar2));
		DateTime.returnCalendar(calendar2);
	}

}
