import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class Practice{
    public static void main(String[] args) {
        ArrayList items = new ArrayList();
        double total = 0;
        double subtotal = 0;
        double tax = 0;
        double discount = 0;
        double finaltotal = 0;

        System.out.println("Welcome to the Super Market Billing System");
        System.out.println("Enter the details of the items");

        while(true){
            Scanner in = new Scanner (System.in);
            System.out.print("Enter the name of the items :");
            String itemName = in.next();
            if(itemName.equalsIgnoreCase("exit")){
                break;
            }
            System.out.print("Enter the quantity of the item : ");
            int itemQuantity = in.nextInt();
            System.out.print("Enter the price of the item :$ ");
            double itemPrice = in.nextDouble();
            Items item = new Items(itemName, itemQuantity, itemPrice);
            items.add(item);

            System.out.println("Item :"+itemName+" Quantity :"+itemQuantity+" Price :$"+itemPrice);
            subtotal += itemQuantity * itemPrice;
        }
        Scanner in = new Scanner (System.in);
        System.out.print("Enter the Tax Rate % : ");
        double taxRate = in.nextDouble();
        tax = (subtotal * taxRate)/100;
        System.out.print("Enter the discount amount :$ ");
        int Discount = in.nextInt();
        finaltotal = (subtotal + tax) - Discount;

        System.out.println("Subtotal :$"+subtotal);
        System.out.println("Tax :$"+tax);
        System.out.println("Discount :$"+Discount);
        System.out.println("FinalTotal :$"+finaltotal);
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

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}