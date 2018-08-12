package test;

import java.time.ZonedDateTime;

public class TestDate {
	
	public static void main(String[] args) {
		ZonedDateTime zdt;
		long millis;
		zdt = ZonedDateTime.parse("2000-01-01T00:00:00.000+00:00[UTC]"); 
		System.out.println(zdt); 
		millis = zdt.toInstant().toEpochMilli();
		System.out.println(zdt + " - " + millis); 
		zdt = ZonedDateTime.parse("2099-12-31T23:59:59.999+00:00[UTC]"); 
		System.out.println(zdt); 
		millis = zdt.toInstant().toEpochMilli();
		System.out.println(zdt + " - " + millis);
		zdt = ZonedDateTime.parse("2018-08-12T15:09+00:00[UTC]"); 
		System.out.println(zdt); 
		millis = zdt.toInstant().toEpochMilli();
		System.out.println(zdt + " - " + millis + " - " + System.currentTimeMillis());
		
	}
}
