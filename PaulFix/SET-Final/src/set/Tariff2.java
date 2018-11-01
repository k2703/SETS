package set;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Tariff
{
	private int type;
	public Tariff(int type)
	{
		this.type = type;
	}
	
	public double getPrice(double usage, String tou) throws Exception
	{
		switch(type)
		{
		case 1:
			return 38.0;
		case 2:
			if(usage < 11)
			{
				return 34.0;
			}
			else if(usage > 11 && usage < 33)
			{
				return 38.0;
			}
			else return 40;
		case 3:
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH", Locale.ENGLISH);
			Date date = df.parse(tou);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			// not weekend
			if (dayOfWeek != 1 && dayOfWeek != 7) {
				int hour = c.get(Calendar.HOUR_OF_DAY);
				// weekday on-peak
				if (hour >= 12 && hour < 18) {
					return 42.0;
				}
				// weekday mid-peak
				if ((hour >= 8 && hour < 12) || (hour >= 18 && hour < 23)) {
					return 36.0;
				}
			}			
			// weekday off-peak or weekend
			return 16.0;
		default:
			throw new Exception("Wrong Tariff Type.");
		}
	}
}
