import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.Math.*;

public class Main {
    private static final String FILE_IN = "src/busy_day.in";
    private static final String FILE_IN2 = "src/mother_of_all_warehouses.in";
    private static final String FILE_IN3 = "src/redundancy.in";
    private static final String FILE_OUT = "src/output_1.out";
    private static final String FILE_OUT2 = "src/output_2.out";
    private static final String FILE_OUT3 = "src/output_3.out";
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
        readFile(FILE_IN);
        processFile();
        writeFile(FILE_OUT);
        clear();

        readFile(FILE_IN2);
        processFile();
        writeFile(FILE_OUT2);
        clear();

        readFile(FILE_IN3);
        processFile();
        writeFile(FILE_OUT3);
        clear();
    }

    private static void clear() {
        drones = new ArrayList<>();
        commands = new ArrayList<>();
    }

    private static void processFile() {
        for (int i = 0; i < turns; i++) {
            // One tick
            for (int d = 0; d < dronesCount; d++) {
                // drones number d
                final Drone drone = drones.get(d);
                if (drone.busy > 0) {
                    // This turn, this drone is busy~
                    drone.busy--;
                } else {
                    if (i < 8264) {
                        // find closest warehouse
                        Collections.sort(warehouses, new Comparator<WareHouse>() {
                            @Override
                            public int compare(WareHouse o1, WareHouse o2) {
                                int dist1 = distanceBetween(drone.x, drone.y, o1.x, o1.y);
                                int dist2 = distanceBetween(drone.x, drone.y, o2.x, o2.y);
                                return dist1 - dist2;
                            }
                        });

                        WareHouse foundWarehouse = null;
                        Order foundOrder = null;
                        for (int w = 0; w < warehouses.size(); w++) {
                            final WareHouse wareHouse = warehouses.get(w);
                            Collections.sort(orders, new Comparator<Order>() {
                                @Override
                                public int compare(Order o1, Order o2) {
                                    int dist1 = distanceBetween(wareHouse.x, wareHouse.y, o1.x, o1.y);
                                    int dist2 = distanceBetween(wareHouse.x, wareHouse.y, o2.x, o2.y);
                                    return dist1 - dist2;
                                }
                            });

                            for (int j = 0; j < orders.size(); j++) {
                                Order order = orders.get(j);
                                boolean fullfield = true;
                                for (Product product : order.products) {
                                    if (wareHouse.stocks.get(product.type) == null) {
                                        fullfield = false;
                                        break;
                                    }
                                }

                                if (fullfield) {
                                    foundWarehouse = wareHouse;
                                    foundOrder = order;
                                    break;
                                }
                            }

                            if (foundWarehouse != null && foundOrder != null) {
                                break;
                            }
                        }

                        if (foundWarehouse != null && foundOrder != null) {
                            processDelivery(drone, foundWarehouse, foundOrder);
                            System.out.println("delivering : " + i);
                        }
                    }
                }
            }
        }
    }

    private static void processDelivery(Drone drone, WareHouse wareHouse, Order order) {
        Product product = order.products.remove(0);
        addLoadCommand(drone, wareHouse, product.type, 1);
        addDeliverCommand(drone, order, product.type, 1);
        drone.busy += distanceBetween(drone.x, drone.y, wareHouse.x, wareHouse.y) + 1;
        drone.busy += distanceBetween(drone.x, drone.y, order.x, order.y) + 1;

        //clear
        Integer remainingStock = wareHouse.stocks.get(product.type);
        remainingStock = remainingStock - 1;
        if (remainingStock <= 0) {
            wareHouse.stocks.remove(product.type);
        } else {
            wareHouse.stocks.put(product.type, remainingStock);
        }

        if (order.products.size() == 0) {
            orders.remove(order);
        }
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
                    for (int k = 0; k < n; k++) w.addProduct(new Product(j, productWeigths[j]));
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
        public HashMap<Integer, Integer> stocks;

        public WareHouse(int id) {
            this.id = id;
            this.stocks = new HashMap<>();
        }

        public void addProduct(Product product) {
            Integer stock = this.stocks.get(product.type);
            if (stock == null) {
                stocks.put(product.type, 0);
            } else {
                stocks.put(product.type, stock + 1);
            }
            products.add(product);
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
}
