import java.util.ArrayList;
import java.util.Scanner;

public class SuperMarketBillSystem {
    public static void main(String[] args) {
        ArrayList items = new ArrayList();
        Scanner in = new Scanner(System.in);
        double total = 0;
        double subtotal = 0;
        double tax = 0;
        double discount = 0;
        double finaltotal = 0;
        System.out.println("Welcome! to the Super Market Billing System");
        System.out.println("Enter the item details");

        while (true) {
            System.out.print("Enter the item name : ");
            String itemName = in.next();
            if (itemName.equalsIgnoreCase("exit")) {
                break;
            }
            System.out.print("Enter the item quantity : ");
            int itemQuantity = in.nextInt();
            System.out.print("Enter the price of the item : ");
            double itemPrice = in.nextDouble();
            Items item = new Items(itemName, itemQuantity, itemPrice);
            items.add(item);
            subtotal += itemQuantity * itemPrice;
            System.out.println("Item : " +itemName+ " Quantity :" +itemQuantity+ " Price :"+itemPrice);
        }
        System.out.print("Enter the tax rate(%) : ");
        double taxRate = in.nextDouble();
        tax = (taxRate * subtotal) / 100;
        System.out.print("Enter the discount : ");
        int Discount = in.nextInt();

        finaltotal = subtotal + tax - Discount;
        System.out.println("SubTotal $:" + subtotal);
        System.out.println("Tax $:" + tax);
        System.out.println("Discount $:" + Discount);
        System.out.println("Total $:" + finaltotal);
    }
    static class Items {
        private String name;
        private int quantity;
        private double price;

        public Items(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getname() {
            return name;
        }

        public int getquantity() {
            return quantity;
        }

        public double getprice() {
            return price;
        }
    }
}
