import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class Main {
    private static final String FILE_IN = "src/busy_day.in";
    private static final String FILE_IN2 = "src/redundancy.in";
    private static final String FILE_IN3 = "src/mother_of_all_warehouses.in";
    private static final String FILE_OUT = "src/test.out";
    private static int rows;
    private static int cols;
    private static int dronesCount;
    private static int turns;
    private static int maxLoad;
    private static int productTypes;
    private static int[] productWeigths;
    private static int warehouseCount;
    private static ArrayList<WareHouse> warehouses;
    private static int orderCount;
    private static ArrayList<Order> orders;
    private static ArrayList<Drone> drones = new ArrayList<>();
    private static ArrayList<String> commands = new ArrayList<>();

    public static void main(String[] args) {
        readFile();

        for (int i = 0; i < turns; i++) {
            // One tick
            for (int d = 0; d < dronesCount; d++) {
                // drones number d
                Drone drone = drones.get(d);
                if (drone.busy > 0) {
                    // This turn, this drone is busy~
                    drone.busy--;
                } else {
                    // TODO find something for the drone.
                }
            }
        }

        writeFile();
    }

    private static void readFile() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(FILE_IN));

            String line;

            line = br.readLine();
            String[] ints = line.split(" ");
            rows = Integer.parseInt(ints[0]);
            cols = Integer.parseInt(ints[1]);
            dronesCount = Integer.parseInt(ints[2]);
            turns = Integer.parseInt(ints[3]);
            maxLoad = Integer.parseInt(ints[4]);

            productTypes = Integer.parseInt(br.readLine());

            productWeigths = new int[productTypes];
            ints = br.readLine().split(" ");
            for (int i = 0; i < productTypes; i++) {
                productWeigths[i] = Integer.parseInt(ints[i]);
            }

            warehouseCount = Integer.parseInt(br.readLine());
            warehouses = new ArrayList<>();
            for (int i = 0; i < warehouseCount; i++) {
                ints = br.readLine().split(" ");
                WareHouse w = new WareHouse();
                w.x = Integer.parseInt(ints[0]);
                w.y = Integer.parseInt(ints[1]);

                ints = br.readLine().split(" ");
                for (int j = 0; j < productTypes; j++) {
                    int n = Integer.parseInt(ints[j]);
                    for (int k = 0; k < n; k++) w.products.add(new Product(j, productWeigths[j]));
                }

                warehouses.add(w);
            }

            orderCount = Integer.parseInt(br.readLine());

            orders = new ArrayList<>();
            for (int i = 0; i < orderCount; i++) {
                Order order = new Order();
                ints = br.readLine().split(" ");
                order.x = Integer.parseInt(ints[0]);
                order.y = Integer.parseInt(ints[1]);

                int n = Integer.parseInt(br.readLine());
                ints = br.readLine().split(" ");
                for (int j = 0; j < n; j++) {
                    int type = Integer.parseInt(ints[j]);
                    order.products.add(new Product(type, productWeigths[type]));
                }

                orders.add(order);
            }

            for (int i = 0; i < dronesCount; i++) {
                drones.add(new Drone(warehouses.get(0).x, warehouses.get(0).y));
            }

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
            writer.println(commands.size());

            for (String command : commands) {
                writer.println(command);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("error writing file " + e.getMessage());
        }
    }


    private static class Product {
        int weight;
        int type;

        public Product(int type, int weigth) {
            this.weight = weigth;
            this.type = type;
        }
    }

    private static class Order {
        List<Product> products = new ArrayList<>();
        int x, y;
    }

    private static class WareHouse {
        List<Product> products = new ArrayList<>();
        int x, y;
    }

    private static class Drone {
        List<Product> products = new ArrayList<>();
        int x, y, busy;

        public Drone(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static int distanceBetween(int xa, int ya, int xb, int yb) {
        return (int) ceil(sqrt((pow(abs((double)(xa - xb)), 2) + pow(abs((double)(ya - yb)), 2))));
    }
}
