//package test;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;
//
//public class Main
//{
//	public static void main(String[] args) throws Exception
//	{
//		CloseableHttpClient httpclient = HttpClients.createDefault();
//		try
//		{
//			/*HttpGet httpGet = new HttpGet("http://115.146.92.150:5000/lstmforecast/FGE/predict");
//			CloseableHttpResponse response1 = httpclient.execute(httpGet);
//			// The underlying HTTP connection is still held by the response object
//			// to allow the response content to be streamed directly from the network
//			// socket.
//			// In order to ensure correct deallocation of system resources
//			// the user MUST call CloseableHttpResponse#close() from a finally clause.
//			// Please note that if response content is not fully consumed the underlying
//			// connection cannot be safely re-used and will be shut down and discarded
//			// by the connection manager.
//			try
//			{
//				System.out.println(response1.getStatusLine());
//				HttpEntity entity1 = response1.getEntity();
//				System.out.println(new JSONObject( EntityUtils.toString(entity1)).getString("tasks").toString());
//				// do something useful with the response body
//				// and ensure it is fully consumed
//				EntityUtils.consume(entity1);
//			} finally
//			{
//				response1.close();
//			}
//			*/
//			HttpPost httpPost = new HttpPost("http://115.146.92.150:5000/lstmforecast/FGE/append");
//			StringEntity s = new StringEntity("\'{\"observation\":\"59.51666667\"}\'");
//			httpPost.setEntity(s);
//			CloseableHttpResponse response2 = httpclient.execute(httpPost);
//			try
//			{
//				System.out.println(response2.getStatusLine());
//				HttpEntity entity2 = response2.getEntity();
//				System.out.println(EntityUtils.toString(entity2) + "F");
//				// do something useful with the response body
//				// and ensure it is fully consumed
//				EntityUtils.consume(entity2);
//			} finally
//			{
//				response2.close();
//			}
//		} 
//		finally
//		{
//			httpclient.close();
//		}
//	}
//}
