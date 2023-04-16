/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class Service {

    private final String country;

    public Service(String country) {
        this.country = country;
    }

    public String getWeather(String city) {
        StringBuilder sb = new StringBuilder();              // tworzenie obiektu StringBuilder
        try {
            String apiKey = "6f2b315f20abaac3bd6378f7f6122d72";           // klucz do API
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric"; // adres API
            URL url = new URL(apiUrl);                                                               // tworzenie obiektu URL
            URLConnection connection = url.openConnection();                                         // otwarcie połączenia
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),               // odczytanie danych
                    StandardCharsets.UTF_8));                                                                       // zwrócenie ich jako String
            String inputLine;
            while ((inputLine = in.readLine()) != null) {                                            // odczytanie danych
                sb.append(inputLine);                                                                // dodanie danych do obiektu StringBuilder
            }
            in.close();                                                                              // zamknięcie połączenia
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();                                                                        // zwrócenie danych jako String
    }

    public Double getRateFor(String currencyCode) {
        StringBuilder response = new StringBuilder();                                                // tworzenie obiektu StringBuilder
        try {
            String url = "https://api.exchangerate.host/latest?base=" + country.toUpperCase() + "&symbols=" + currencyCode; // adres API
            URL obj = new URL(url);                                                             // tworzenie obiektu URL
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();                   // otwarcie połączenia
            con.setRequestMethod("GET");                                                        // ustawienie metody żądania
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject objJson;                                                            // tworzenie obiektu JSONObject
            objJson = new JSONObject(response.toString());                                 // przypisanie danych do obiektu
            return objJson.getJSONObject("rates").getDouble(currencyCode);                 // zwrócenie danych
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Double getNBPRate() {
        return 0.0;
    }

}