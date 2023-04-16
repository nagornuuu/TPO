package zad1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.embed.swing.JFXPanel;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class GUI {
    JLabel jTmp, jMiasto, jMin_temp, jMax_temp, jPressure, jLon, jLat, jHumidity, rateLabel, textLabel2, NBP, jCity, jCountry;

    public GUI(){
        jTmp = new JLabel();
        jMiasto = new JLabel();
        jMin_temp = new JLabel();
        jMax_temp = new JLabel();
        jPressure = new JLabel();
        jLon = new JLabel();
        jLat = new JLabel();
        jHumidity = new JLabel();
        rateLabel = new JLabel();
        textLabel2 = new JLabel();
        NBP = new JLabel();
        jCity = new JLabel();
        jCountry = new JLabel();
        runGui();
    }
    public void runGui(){
        JFXPanel jfxpanel = new JFXPanel();

        JFrame frame = new JFrame("Web client");
        frame.setLayout(new GridLayout(1,2));

        JPanel left = new JPanel();
        left.setLayout(new GridLayout(4,1));

        Border etched = BorderFactory.createEtchedBorder();

        JPanel weatherPanel = new JPanel();
        JPanel weather = new JPanel();
        JPanel exchangePanel = new JPanel();
        JPanel nbp = new JPanel();

        weatherPanel.setBorder(BorderFactory.createTitledBorder(etched, "Choose Country and City"));
        JTextField countryField = new JTextField(10);
        JTextField cityField = new JTextField(10);
        jCountry.setText("Country: ");
        weatherPanel.add(jCountry);
        weatherPanel.add(countryField);
        weatherPanel.add(jCity);
        jCity.setText("City: ");
        weatherPanel.add(cityField);
        JButton button1 = new JButton("Enter");
        weatherPanel.add(button1);
        button1.addActionListener(e -> {
            String country = countryField.getText();
            String city = cityField.getText();
            if (country.isEmpty() || city.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter country and city first!");
                return;
            } try {
                    Service service = new Service(country);
                    String weatherJson = service.getWeather(city);
                    JsonParser jsonParser = new JsonParser();
                    JsonElement jsonElement = jsonParser.parse(weatherJson);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonObject coord = jsonObject.get("coord").getAsJsonObject();
                    double lon = coord.get("lon").getAsDouble();
                    double lat = coord.get("lat").getAsDouble();
                    String miasto = jsonObject.get("name").getAsString();
                    JsonObject main = jsonObject.get("main").getAsJsonObject();
                    double temp = main.get("temp").getAsDouble();
                    double temp_min = main.get("temp_min").getAsDouble();
                    double temp_max = main.get("temp_max").getAsDouble();
                    double pressure = main.get("pressure").getAsDouble();
                    double humidity = main.get("humidity").getAsDouble();

                    jMiasto.setText("City: " + miasto);
                    jLon.setText("Longitude: " + lon);
                    jLat.setText("Latitude: " + lat);
                    jTmp.setText("Temperature: " + temp + "°C");
                    jMin_temp.setText("Min. temp.: " + temp_min + "°C");
                    jMax_temp.setText("Max. temp.: " + temp_max + "°C");
                    jPressure.setText("Pressure: " + pressure + " hPa");
                    jHumidity.setText("Humidity: " + humidity + " %");
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(null, "Check the given data!");
                }
            });

        weather.setBorder(BorderFactory.createTitledBorder(etched, "Weather"));
        weather.setLayout(new GridLayout(0,3));
        JComponent[] components = { jTmp, jMiasto, jMin_temp, jMax_temp, jPressure, jLon, jLat, jHumidity };
        for (JComponent component : components) {
            weather.add(component);
        }

        nbp.add(NBP);
        exchangePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Exchange"));

        JTextField currency = new JTextField(10);
        exchangePanel.add(textLabel2);
        textLabel2.setText("Enter the currency code. (e.g. USD)");
        exchangePanel.add(currency);
        JButton exchangeButton = new JButton("Enter");
        exchangePanel.add(exchangeButton);
        exchangeButton.addActionListener(e -> {
            String country = countryField.getText();
            String curr = currency.getText();
            if (country.isEmpty() || curr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter country and currency first!");
            } else {
                try {
                    Service service = new Service(countryField.getText());
                    double tmp = service.getRateFor(currency.getText());
                    rateLabel.setText("1.0 USD = " + tmp + " " + currency.getText());
                    NBP.setText("NBP zloty exchange rate against the currency of the selected country: " + tmp + " PLN");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Check the given data!");
                }
            }
        });
        exchangePanel.add(rateLabel);
        nbp.setBorder(BorderFactory.createTitledBorder(etched, "NBP"));

        JPanel[] panels = { weatherPanel, weather, exchangePanel, nbp};
        for (JPanel panel : panels) {
            left.add(panel);
        }

        frame.add(left);
        frame.add(jfxpanel);
        frame.setSize(1100,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
