import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    static String[] inputs = new String[4];
    static ArrayList<String> time = new ArrayList<>();
    static ArrayList<String> magnitude = new ArrayList<>();
    static ArrayList<String> eventLocationName = new ArrayList<>();
    static ArrayList<String> country = new ArrayList<>();
    static ArrayList<String> location = new ArrayList<>();
    static String receivedData = "";


    public static void main(String[] args) throws IOException, InterruptedException {
        gettingInput();
        HTTPConnection();
        fillFields();
        getCountryLocation();
        earthquakeInfo();
    }

    public static void gettingInput() {
        // Getting input from user
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the country: ");
            String enteredCountry = scanner.nextLine();
            System.out.println("Enter day: ");
            int dayCount = scanner.nextInt();
            if (dayCount < 0 || dayCount > 100) {
                System.out.println("Please enter an integer between range of 0-100 and try again.");
                System.exit(0);
            }
            LocalDate endTime = LocalDate.now();
            LocalDate startTime = LocalDate.now().minusDays(dayCount);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            inputs[0] = enteredCountry;
            inputs[1] = Integer.toString(dayCount);
            inputs[2] = endTime.format(formatter);
            inputs[3] = startTime.format(formatter);
        } catch (InputMismatchException e) {
            System.out.println("Please enter a country for country field and an integer for day field and try again.");
            System.exit(0);
        }
    }

    public static void HTTPConnection() throws IOException, InterruptedException {
        // Connecting to HTTP by java.net.http.HttpClient
        String apiURL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=text&starttime=" + inputs[3] + "&endtime=" + inputs[2];
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(apiURL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            receivedData = response.body();
            if (status != 200) {
            System.out.println("An error occured, please try again. Error code: " + status);
            System.exit(0);
        }
    }


    public static void fillFields(){
        String[] lines = receivedData.split("\n"); // Splitting by line breaks
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] fields = line.split("\\|"); // Splitting by pipes and filling fields
            time.add(fields[1]);
            magnitude.add(fields[10]);
            // Handling exception occured by missing country information
            if (fields.length == 13) {
                eventLocationName.add(fields[12]);
            } else {
                eventLocationName.add("Not specified");
            }
        }
    }

    public static void getCountryLocation() {
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
    }

    public static void earthquakeInfo() {
        for (int i = 0; i < country.size() - 1; i++) {
            // Printing list of earthquakes
            if (inputs[0].equalsIgnoreCase(country.get(i))) {
                System.out.println("Country: " + country.get(i) +
                        ", Place: " + location.get(i) +
                        ", Magnitude: " + magnitude.get(i) +
                        ", Date & Time: " + time.get(i));
                // Handling if there is no earthquake data found by desired country
            } else if (!country.get(i).equalsIgnoreCase(inputs[0])) {
                i++;
            } else {
                // Handling any other input than a country name
                System.out.println("Please try again by entering a country name.");
                break;
            }
        }
    }
}
