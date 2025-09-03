import java.util.ArrayList;
import java.util.Scanner;

public class ParkinggSystem{
    static int totalSlots, availableSlots;
    static ArrayList<String> parkedCar = new ArrayList<String>();

    public static void main(String[] args) {
        Scanner in = new Scanner (System.in);
        System.out.println("Welcome to Rohit's Parking System Program");
        System.out.print("Enter the number of slots : ");
        totalSlots = in.nextInt();
        availableSlots = totalSlots;

        while(true){
            System.out.println("What do you want to do?");
            System.out.println("1.Park the car. ");
            System.out.println("2. Remove the car. ");
            System.out.println("3. View the parked cars. ");
            System.out.println("4. Exit. ");
            int choice = in.nextInt();

            switch(choice){
                case 1:
                    parkCar();
                    break;
                case 2:
                    removeCar();
                    break;
                case 3:
                    viewParkedCar();
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid Choice!, please try again. ");
            }
        }
    }
    public static void parkCar(){
        if(availableSlots==0){
            System.out.println("Parking Area is full");
            return;
        }
        Scanner in = new Scanner (System.in);
        System.out.println("Enter the license plate of the car : ");
        String licensePlate = in.nextLine();
        parkedCar.add(licensePlate);
        System.out.print("  Your car has been parked");
        availableSlots--;
        System.out.println("Slots Available : "+availableSlots);
    }
    public static void removeCar(){
        if(availableSlots==totalSlots){
            System.out.println("Parking Area is Empty");
            return;
        }
        Scanner in = new Scanner (System.in);
        System.out.println("Enter the license plate of the car : ");
        String licensePlate = in.nextLine();
        if(parkedCar.contains(licensePlate)){
            parkedCar.remove(licensePlate);
            System.out.println("Your car has been removed");
            availableSlots++;
            System.out.println("Slots Available : "+availableSlots);
        }
        else{
            System.out.println("The car is not here");
            System.out.println("Slots Available : "+availableSlots);
        }
    }
    public static void viewParkedCar(){
        if(availableSlots==totalSlots){
            System.out.println("Parking Area is Empty");
            return;
        }
        System.out.println("Parked Cars : ");
        for(String licensePlate : parkedCar){
            System.out.println(licensePlate);
        }
    }
}