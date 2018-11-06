package set;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import au.com.bytecode.opencsv.CSVReader;

public class postURL {

	private double result[] = new double[2];
	private double previous= 0;
	private int hour = 0;
	private String filename = "test.csv";
	String tou;

	// Method of the class that returns the filename to itself
	public postURL(String filename) {
		this.filename = filename;
	}

	// Method that returns hour as an int
	public int getHour() {
		return hour;
	}

	// Method to increment each hour
	private void incrHour() {
		hour++;
	}
	
	private int getHeaderLocation(String[] headers, String columnName)
	{
		return Arrays.asList(headers).indexOf(columnName);
	}

	// Method for retrieving a URL for the data and outputting the predicted value
	public double[] UseService(String column) {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			CSVReader reader = new CSVReader(new FileReader(filename));
			List<String[]> lines = new ArrayList<>();
			lines = reader.readAll(); 
			int i;
			for(i = 0; i < lines.size(); i++) if(lines.get(0)[i].equals(column)) break;
			String actual = lines.get(getHour()+1)[getHeaderLocation(lines.get(0), column)];
			tou = lines.get(getHour()+1)[getHeaderLocation(lines.get(0), "UNIX_TS")];
			//System.out.println(actual);
			result[0] = Double.parseDouble(actual);
			//System.out.println(result[0]);
			String url = "http://115.146.92.150:5000/lstmforecast/" + column + "/predict";
			HttpPost postRequest = new HttpPost(url);
			StringEntity input;
			// NOTE : Upon startup the string entity must be an empty field due to a
			// pre-trained model and afterwards the observation field may contain values
			if (getHour() == 0) {
				input = new StringEntity("{\"observation\":\"\"}");
				previous = result[0];
			} else {
				//input = new StringEntity("{\"observation\":\"59.51666667\"}");
				input = new StringEntity("{\"observation\":\""+previous+ "\"}");
				previous = result[0];
			}
			input.setContentType("application/json");
			postRequest.setEntity(input);
			HttpResponse response = httpClient.execute(postRequest);
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			//System.out.println("Output from Server: \n");
			while ((output = br.readLine()) != null) {
			    
				String str = output.replaceAll("[^\\d.]", "");
				result[1] = Double.parseDouble( str);
				//System.out.println(str);
			}
			incrHour();
			reader.close();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return result;
	}
	
	public String UseDate()
	{
		return tou;
	}

	/*public static void main(String args[])
	{
		postURL a = new postURL("test.csv");
		double trial[] = a.UseService("FGE");
		System.out.println("Actual: " + trial[0] + "  Predicted: "+ trial[1]);
		trial = a.UseService("FGE");
		System.out.println("Actual: " + trial[0] + "  Predicted: "+ trial[1]);
		trial = a.UseService("FGE");
		System.out.println("Actual: " + trial[0] + "  Predicted: "+ trial[1]);
		trial = a.UseService("FGE");
		System.out.println("Actual: " + trial[0] + "  Predicted: "+ trial[1]);
	}*/
}
