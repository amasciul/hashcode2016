import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Alexandre on 11/02/16.
 */
public class Main {
    private static final String FILE_IN = "src/test.in";
    private static final String FILE_OUT = "src/test.out";

    public static void main(String[] args) {
        readFile();
        writeFile();
    }

    private static void readFile() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(FILE_IN));

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("error reading file " + e.getMessage());
        }
    }

    private static void writeFile() {
        try {
            PrintWriter writer = new PrintWriter(FILE_OUT);
            writer.println("stuff");
            writer.close();
        } catch (IOException e) {
            System.out.println("error writing file " + e.getMessage());
        }
    }
}
