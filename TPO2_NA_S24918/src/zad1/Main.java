/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

public class Main {
  public static void main(String[] args) {
    Service s = new Service("Poland");
    String weatherJson = s.getWeather("Warsaw");
    Double rate1 = s.getRateFor("USD");
    Double rate2 = s.getNBPRate();
    System.out.print(weatherJson);
    GUI gui = new GUI();
  }
}
