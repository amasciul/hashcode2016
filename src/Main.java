import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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

        for (int i = 0; i < warehouseCount; i++) {
            final WareHouse wareHouse = warehouses.get(i);
            List<Order> ordersOrderedByDistance = wareHouse.ordersOrderedByDistance;
            ordersOrderedByDistance.addAll(orders);
            Collections.sort(ordersOrderedByDistance, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    int d1 = distanceBetween(wareHouse.x, wareHouse.y, o1.x, o1.y);
                    int d2 = distanceBetween(wareHouse.x, wareHouse.y, o2.x, o2.y);
                    return d1 - d2;
                }
            });
        }

        for (int i = 0; i < turns; i++) {
            // One tick
            System.out.println("tick " + i);
            for (int d = 0; d < dronesCount; d++) {
                // drones number d
                final Drone drone = drones.get(d);
                if (drone.busy > 0) {
                    // This turn, this drone is busy~
                    drone.busy--;
                } else {
                    // Sort the warehouse by distance.
                    ArrayList<WareHouse> wareHouses = new ArrayList<>(warehouses);
                    Collections.sort(wareHouses, new Comparator<WareHouse>() {
                        @Override
                        public int compare(WareHouse o1, WareHouse o2) {
                            int d1 = distanceBetween(drone.x, drone.y, o1.x, o1.y);
                            int d2 = distanceBetween(drone.x, drone.y, o2.x, o2.y);
                            return d1 - d2;
                        }
                    });

                    for (WareHouse wareHouse : wareHouses) {
                        final Delivery delivery = closetDelivery(wareHouse);
                        if (delivery != null) {
                            // hurray we got our job !
                            int distanceFromWareHouse = distanceBetween(drone.x, drone.y, wareHouse.x, wareHouse.y);
                            Map<Integer, Integer> reduction = reduce(delivery.products);
                            for (Map.Entry<Integer, Integer> entry : reduction.entrySet()) {
                                addLoadCommand(drone, delivery.from, entry.getKey(), entry.getValue());
                            }

                            for (Map.Entry<Integer, Integer> entry : reduction.entrySet()) {
                                addDeliverCommand(drone, delivery.to, entry.getKey(), entry.getValue());
                            }

                            // Update the drone.
                            drone.busy = 2 + distanceFromWareHouse + delivery.distance();
                            drone.x = delivery.to.x;
                            drone.y = delivery.to.y;
                            break;
                        }
                    }
                }
            }
        }

        writeFile(MOTHER_OF_ALL_WAREHOUSES_OUT);
    }

    private static Map<Integer, Integer> reduce(List<Product> products) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (Product product : products) {
            Integer count = map.get(product.type);
            if (count == null) {
                count = 0;
            }
            map.put(product.type, count + 1);
        }
        return map;
    }

    private static boolean canDispatch(Product product, WareHouse wareHouse) {
        return wareHouse.products.contains(product);
    }

    private static Delivery closetDelivery(WareHouse wareHouse) {
        for (Order order : wareHouse.ordersOrderedByDistance) {
            if (!order.products.isEmpty()) {
                // There are some products to take.
                Delivery delivery = new Delivery();
                delivery.from = wareHouse;
                delivery.to = order;

                Iterator<Product> iterator = order.products.iterator();
                while (iterator.hasNext()) {
                    Product next = iterator.next();
                    if (next.weight < delivery.freeWeight() && canDispatch(next, wareHouse)) {
                        // Remove from order
                        iterator.remove();
                        // Remove from warehouse
                        wareHouse.products.remove(next);
                        // add to delivery
                        delivery.products.add(next);
                    }
                }

                if (!delivery.products.isEmpty()) {
                    return delivery;
                }
            }
        }
        return null;
    }

    private static class Delivery {
        WareHouse from;
        Order to;
        List<Product> products = new ArrayList<>();

        int freeWeight() {
            int weight = 0;
            for (Product product : products) {
                weight += product.weight;
            }
            return maxLoad - weight;
        }

        int distance() {
            return distanceBetween(from.x, from.y, to.x, to.y);
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
        List<Order> ordersOrderedByDistance = new ArrayList<>();
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
                map[order.x * cols + order.y] = 'o';
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
