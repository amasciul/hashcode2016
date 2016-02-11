import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class Main {
    private static final String BUSY_DAY_IN = "src/busy_day.in";
    private static final String BUSY_DAY_OUT = "src/busy_day.out";
    private static final String BUSY_DAY_MAP = "src/busy_day.map";

    private static final String REDUNDANCY_IN = "src/redundancy.in";
    private static final String REDUNDANCY_OUT = "src/redundancy.out";
    private static final String REDUNDANCY_MAP = "src/redundancy.map";

    private static final String MOTHER_OF_ALL_WAREHOUSES_IN = "src/mother_of_all_warehouses.in";
    private static final String MOTHER_OF_ALL_WAREHOUSES_OUT = "src/mother_of_all_warehouses.out";
    private static final String MOTHER_OF_ALL_WAREHOUSES_MAP = "src/mother_of_all_warehouses.map";

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
        readFile(MOTHER_OF_ALL_WAREHOUSES_IN);

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

        addLoadCommand(drones.get(0), warehouses.get(0), 163, 1);
        addDeliverCommand(drones.get(0), orders.get(1), 163, 1);

        writeFile(MOTHER_OF_ALL_WAREHOUSES_OUT);
    }

    private static void readFile(String name) {
        try {

            BufferedReader br = new BufferedReader(new FileReader(name));

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
                WareHouse w = new WareHouse(i);
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
                Order order = new Order(i);
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
                drones.add(new Drone(warehouses.get(0).x, warehouses.get(0).y, i));
            }

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("error reading file " + e.getMessage());
        }
    }

    private static void addLoadCommand(Drone drone, WareHouse wareHouse, int productType, int productNumber) {
        StringBuilder stringBuilder = new StringBuilder(5);
        stringBuilder.append(drone.id);
        stringBuilder.append(' ');
        stringBuilder.append('L');
        stringBuilder.append(' ');
        stringBuilder.append(wareHouse.id);
        stringBuilder.append(' ');
        stringBuilder.append(productType);
        stringBuilder.append(' ');
        stringBuilder.append(productNumber);
        commands.add(stringBuilder.toString());
    }

    private static void addDeliverCommand(Drone drone, Order order, int productType, int productNumber) {
        StringBuilder stringBuilder = new StringBuilder(5);
        stringBuilder.append(drone.id);
        stringBuilder.append(' ');
        stringBuilder.append('D');
        stringBuilder.append(' ');
        stringBuilder.append(order.id);
        stringBuilder.append(' ');
        stringBuilder.append(productType);
        stringBuilder.append(' ');
        stringBuilder.append(productNumber);
        commands.add(stringBuilder.toString());
    }

    private static void writeFile(String name) {
        try {
            PrintWriter writer = new PrintWriter(name);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Product product = (Product) o;

            if (weight != product.weight) return false;
            return type == product.type;

        }

        @Override
        public int hashCode() {
            int result = weight;
            result = 31 * result + type;
            return result;
        }
    }

    private static class Order {
        List<Product> products = new ArrayList<>();
        int x, y;
        public int id;

        public Order(int id) {
            this.id = id;
        }
    }

    private static class WareHouse {
        List<Product> products = new ArrayList<>();
        int x, y;
        public int id;

        public WareHouse(int id) {
            this.id = id;
        }
    }

    private static class Drone {
        List<Product> products = new ArrayList<>();
        int x, y, busy, id;

        public Drone(int x, int y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }
    }

    private static int distanceBetween(int xa, int ya, int xb, int yb) {
        return (int) ceil(sqrt((pow(abs((double) (xa - xb)), 2) + pow(abs((double) (ya - yb)), 2))));
    }


    // **************************************************
    // **************************************************
    // **************************************************
    //                  MAP STUFF
    // **************************************************
    // **************************************************
    // **************************************************
    private static void generateMap(String mapName) {
        try {
            PrintWriter writer = new PrintWriter(mapName);

            final char[] map = new char[rows * cols];
            Arrays.fill(map, '.');

            for (WareHouse warehouse : warehouses) {
                map[warehouse.x * cols + warehouse.y] = 'w';
            }

            for (Order order : orders) {
                map[order.x * cols + order.y] =  'o';
            }

            for (int r = 0; r < rows; r++) {
                final StringBuilder stringBuilder = new StringBuilder();
                for (int c = 0; c < cols; c++) {
                    stringBuilder.append(map[r * cols + c]);
                }
                stringBuilder.append('\n');
                writer.println(stringBuilder.toString());
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("error writing file " + e.getMessage());
        }
    }


}
