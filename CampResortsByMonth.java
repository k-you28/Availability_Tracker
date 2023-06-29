import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL; 

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.PrintWriter;

public class CampResortsByMonth {
	private static String requestedMonth = "2023-07"; //Format goes YYYY-MM
	private static String reqUrl = "https://www.recreation.gov/api/permits/249991/availability/month?start_date=" + requestedMonth + "-01T00:00:00.000Z";

	public static void main(String[] args) throws IOException, InterruptedException {
		int i = 1; //Tracks the amount of time a POST request is made
		while(true) {
			System.out.println("Post Requests Made: " + i);
			sendPOST();
			//Thread.sleep(21600000);    //Requests every 6 hours
			//Thread.sleep(7200000);     //Requests every 2 hours
			//Thread.sleep(5400000);     //Requests every 1.5 hours
			Thread.sleep(300000);        //Requests every 5 minutes
			i++;

		}

	}

	public static void sendGET() throws IOException {

		URL u = new URL(reqUrl); //Creates a URL Object to use for connection
		HttpURLConnection con = (HttpURLConnection) u.openConnection(); //Creates connection with URL object


		if(con.getResponseCode()==HttpURLConnection.HTTP_OK) { //Connection is successful

			//Reads input for all information received from the request
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();


			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String json = response.toString(); //Holds the request result information in string format

			/*
			 * The following lines all filter the received JSON to the relevant information. Multiple
			 * JSONObject variables are needed due to nested JSONs existing.
			 */

			Object obj=JSONValue.parse(json);  
			JSONObject jsonObject = (JSONObject) obj; 

			JSONObject payload = (JSONObject)jsonObject.get("payload");
			JSONObject available = (JSONObject)payload.get("availability");
			JSONObject num279 = (JSONObject)available.get("279");
			JSONObject quote_type_maps = (JSONObject)num279.get("quota_type_maps");
			JSONObject QuotaUsageByMemberDaily = (JSONObject)quote_type_maps.get("QuotaUsageByMemberDaily");



			//Traverses through each day of the month
			for(int i = 1; i < 32; i++) {

				String date = (i<10) ? (requestedMonth + "-0" + i + "T00:00:00Z") : (requestedMonth + "-" + i + "T00:00:00Z"); //Makes sure date format is correct
				JSONObject d = (JSONObject) QuotaUsageByMemberDaily.get(date);


				if(d==null) break; //If null, the day does not exist and we are finished looping

				//If there is a remaining slot, add it to our relevant String
				if(!d.get("remaining").toString().equals("0")) System.out.println(date.substring(0, 10) + "    Remaining Slots: " + d.get("remaining") + "\n");

			}	

		} else { //Connection failed

			System.out.println("GET REQUEST FAILED. Status Code: " + con.getResponseCode());

		}

	}

	public static void sendPOST() throws IOException {

		URL u = new URL(reqUrl); //Creates a URL Object to use for connection
		HttpURLConnection con = (HttpURLConnection) u.openConnection(); //Creates connection with URL object
		String res = ""; //Holds all relevant information to send to the receiver
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");



		if(con.getResponseCode()==HttpURLConnection.HTTP_OK) { //Connection is successful

			//Reads input for all information received from the request
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();


			//Appends all lines of the JSON into response
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();



			String json = response.toString(); //Holds the request result information in string format


			/*
			 * The following lines all filter the received JSON to the relevant information. Multiple
			 * JSONObject variables are needed due to nested JSONs existing.
			 */

			Object obj=JSONValue.parse(json);  
			JSONObject jsonObject = (JSONObject) obj; 

			JSONObject payload = (JSONObject)jsonObject.get("payload");
			JSONObject available = (JSONObject)payload.get("availability");
			JSONObject num279 = (JSONObject)available.get("279");
			JSONObject quote_type_maps = (JSONObject)num279.get("quota_type_maps");
			JSONObject QuotaUsageByMemberDaily = (JSONObject)quote_type_maps.get("QuotaUsageByMemberDaily");



			//Traverses through each day of the month
			for(int i = 1; i < 32; i++) {

				if(i < 13 || i > 15) continue; //OPTIONAL LINE, DELETE IF YOU WANT ALL DATES OF THE MONTH
				

				String date = (i<10) ? (requestedMonth + "-0" + i + "T00:00:00Z") : (requestedMonth + "-" + i + "T00:00:00Z"); //Makes sure date format is correct
				JSONObject d = (JSONObject) QuotaUsageByMemberDaily.get(date);


				if(d==null) break; //If null, the day does not exist and we are finished looping

				//If there is a remaining slot, add it to our relevant String
				if(!d.get("remaining").toString().equals("0")) res += date.substring(0, 10) + "    Remaining Slots: " + d.get("remaining") + "\n";

			}	




			if(res.length()==0) return; //If not relevant info, don't continue to avoid duplicate information
			String payloadInfo = "name=Kevin+You&email=kevin.you2940%40gmail.com&message=" + res + "&sub=Send+Message";



			//Opens lastReceivedDates.txt file that holds the previously sent information to make sure no
			//duplicate information is sent again to avoid unnecessary action.
			File lastReceivedDatesFile = new File("src/lastReceivedDates.txt");
			FileWriter datesWriter = new FileWriter(lastReceivedDatesFile.getAbsolutePath(), true);
			Scanner fileReader = new Scanner(lastReceivedDatesFile);
			String lastDatesStr = ""; //Holds the previous file contents in String format



			while(fileReader.hasNextLine()) {

				lastDatesStr += fileReader.nextLine() + "\n"; //Appends lastDatesStr

			}


			if (lastDatesStr.equals(res)) { //Checks if the information is a duplicate

				datesWriter.close();
				fileReader.close();
				return;

			} else {

				//Deletes the current files contents and replaces it with the current file contents
				PrintWriter writer = new PrintWriter(lastReceivedDatesFile.getAbsolutePath());
				writer.print("");
				writer.close();		
				datesWriter.write(res);

			}

			datesWriter.close();
			fileReader.close();


			//Opens a connection to a third-party email sender
			URL formspree = new URL("https://formspree.io/f/mgebgkrl"); //CHANGE THIS LINK
			HttpURLConnection form = (HttpURLConnection) formspree.openConnection();

			form.setRequestMethod("POST");
			form.setDoOutput(true); //Allows OutputStream to be sent out
			form.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //IMPORTANT: Information sent should be in URL format



			//Holds all of the information to send out 
			OutputStream o = form.getOutputStream();
			o.write(payloadInfo.getBytes());
			o.flush();
			o.close();
			form.connect(); //Sends the information
			form.getContentLength();

		}

	}
}