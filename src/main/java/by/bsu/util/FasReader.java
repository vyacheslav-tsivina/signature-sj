package by.bsu.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to read input data from standard input files
 */
public class FasReader {

    public static Map<Integer, String> readList(Path filePath) throws IOException {
        List<String> raw =  Files.readAllLines(filePath);
        Map<Integer, String> result = new HashMap<>();
        int i = 0;
        StringBuilder seq = new StringBuilder();
        for (int j = 0; j < raw.size(); j++) {
            String str = raw.get(j);
            if (str.startsWith(">") && seq.length() > 0 || str.length() == 0){
                result.put(i, seq.toString());
                i++;
                seq.setLength(0);
            }else if (!str.startsWith(">")) {
                seq.append(str);
                //workaround for for cases when file doesn't cantain last empty string
                if (j+1 == raw.size()){
                    result.put(i, seq.toString());
                }
            }
        }
        return result;
    }

    public static Map<Integer, String> readList(String filePath) throws IOException {
        return readList(Paths.get(filePath));
    }
}
