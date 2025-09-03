import java.util.Scanner;
import java.util.ArrayList;

public class ParkingSystemPractice{
    static int totalSLots, availableSlots;
    static ArrayList<String> parkedCars = new ArrayList<>();

    public static void main(String[] args) {
        Scanner in = new Scanner (System.in);
        System.out.println("Welcome to Rohit's Parking System");
        System.out.print("Enter the total no. of slots : ");
        int totalSlots = in.nextInt();
        availableSlots = totalSlots;

        while(true){
            System.out.println("What do you want to perform");
            System.out.println("1. Park the Car");
            System.out.println("2. Remove the Car");
            System.out.println("3. View the Parked Cars");
            System.out.println("4. Exit");

            int choice = in.nextInt();
            switch(choice){
                case 1 :
                    parkCar();
                    break;
                case 2 :
                    removeCar();
                    break;
                case 3 :
                    viewParkedCars();
                    break;
                case 4 :
                    System.exit(0);
                    break;
                default :
                    System.out.println("Invalid Choice!");
            }
        }
    }
    public static void parkCar(){
        if(availableSlots == 0){
            System.out.println("Parking Area is full");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the licence plate of the Car : ");
        String licencePlate = in.next();
        parkedCars.add(licencePlate);
        System.out.println("The Car has been parked");
        availableSlots--;
        System.out.println("Slots available : "+availableSlots);
    }
    public static void removeCar(){
        if(availableSlots == totalSLots){
            System.out.println("Parking Area is Empty");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the licence plate of the Car : ");
        String licencePlate = in.next();
        if(parkedCars.contains(licencePlate)){
            parkedCars.remove(licencePlate);
            System.out.println("The Car has been removed");
            availableSlots++;
            System.out.println("Slots Available : "+availableSlots);
        }
        else{
            System.out.println("No such licence found!");
            System.out.println("Slots Available : "+availableSlots);
        }
    }
    public static void viewParkedCars(){
        if(availableSlots == totalSLots){
            System.out.println("The Parking Area is Empty!");
        }
        System.out.println("Parked Cars : ");
        for(String licencePlate : parkedCars){
            System.out.println(licencePlate);
        }
    }
}

