import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        // Getting input from user
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the country: ");
        String enteredCountry = scanner.nextLine();
        System.out.println("Enter day: ");
        int dayCount = scanner.nextInt();
        // Handling if wrong type of integer is given
        if (dayCount <= 0 || dayCount > 1000) System.out.println("Please enter an integer in correct format and try again.");

        LocalDate endTime = LocalDate.now();
        LocalDate startTime = LocalDate.now().minusDays(dayCount);

        // Connecting to HTTP by java.net.http.HttpClient
        String apiURL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=text&starttime=" + startTime + "&endtime=" + endTime;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(apiURL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Defining necessary fields
        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> magnitude = new ArrayList<>();
        ArrayList<String> eventLocationName = new ArrayList<>();
        ArrayList<String> country = new ArrayList<>();
        ArrayList<String> location = new ArrayList<>();

        String receivedData = response.body();
        String[] lines = receivedData.split("\n"); // Splitting by line breaks

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            // Handling Error 400: Bad Request
            if (lines[0].equals("Error 400: Bad Request")){
                System.out.println("Error 400: Bad Request : Please try again with a smaller day input.");
                break;
            }
            String[] fields = line.split("\\|"); // Splitting by pipes and filling fields
            time.add(fields[1]);
            magnitude.add(fields[10]);
            // Handling exception occured by missing country information
            try {
                eventLocationName.add(fields[12]);
            } catch (ArrayIndexOutOfBoundsException e) {
                eventLocationName.add("Not specified");
            }
        }

        for (String s : eventLocationName) {
            String[] temp = s.split(", "); // Splitting by commas to define place and country
            if (temp.length == 2) {
                location.add(temp[0]);
                country.add(temp[1]);
            } else if (temp.length == 3) { // Handling if some places has multiple commas
                location.add(temp[0] + ", " + temp[1]);
                country.add(temp[2]);
            } else {
                location.add("Not specified"); // Handling if only country information is given
                country.add(temp[0]);
            }
        }

        for (int i = 0; i < country.size() - 1; i++) {
            // Printing list of earthquakes
            if (enteredCountry.equalsIgnoreCase(country.get(i))) {
                System.out.println("Country: " + country.get(i) +
                                   ", Place: " + location.get(i) +
                                   ", Magnitude: " + magnitude.get(i) +
                                   ", Date & Time: " + time.get(i));
            // Handling if there is no earthquake data found by desired country
            } else if (!country.get(i).equalsIgnoreCase(enteredCountry)) {
                System.out.println("No Earthquakes were recorded past " + dayCount + " days.");
                break;
            // Handling any other input than a country name
            } else {
                System.out.println("Please try again by entering a country name.");
                break;
            }
        }
    }
}
