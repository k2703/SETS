package set;

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
			return 0.0;
		default:
			throw new Exception("Wrong Tariff Type.");
		}
	}
}
