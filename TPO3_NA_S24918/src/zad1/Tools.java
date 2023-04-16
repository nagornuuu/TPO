/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tools {
    public static Options createOptionsFromYaml(String fileName) throws Exception {
        Yaml yaml = new Yaml();                                                                    // Create a new instance of Yaml
        Map<String, Object> data = yaml.load(new FileInputStream(fileName));                       // Load the data from the YAML file
        Map<String, List<String>> ClientsMap = (Map<String, List<String>>) data.get("clientsMap"); // Get the "clientsMap" value from the data
        return new Options(                                                                        // Create and return a new Options object with the values from the data
                (String) data.get("host"),                              // Get the "host" value from the data
                (int) data.get("port"),                                 // Get the "port" value from the data
                (boolean) data.get("concurMode"),                       // Get the "concurMode" value from the data
                (boolean) data.get("showSendRes"),                      // Get the "showSendRes" value from the data
                new LinkedHashMap<>(ClientsMap)                         // Create a new LinkedHashMap with the "clientsMap" value
        );
    }
}
