import java.util.Scanner;
public class TempConvertor {
    public static void main(String[] args) {
        Scanner in = new Scanner (System.in);
        System.out.println("Welcome to Temperature Convertor System");
        double temperature;
        while(true){
            System.out.println("Choose the Conversion Type");
            System.out.println("1. Celsius to Fehrenheit");
            System.out.println("2. Fehrenheit to Celsius");
            System.out.println("3. Celsius to Kelvin");
            System.out.println("4. Kelvin to Celsius");
            System.out.println("5. Fehrenheit to Kelvin");
            System.out.println("6. Kelvin to Fehrenheit");
            System.out.println("7. Exit");
            System.out.print("Type : ");
            int choice = in.nextInt();

            switch(choice){
                case 1:
                    System.out.print("Enter the temperature : " );
                    temperature = in.nextDouble();
                    double CF = (temperature * 9 / 5) + 32;
                    System.out.println(temperature+"`C in Fehrenheit is : "+CF+"`F");
                    break;
                case 2:
                    System.out.print("Enter the temperature : " );
                    temperature = in.nextDouble();
                    double FC = (temperature - 32) * 5 / 9;
                    System.out.println(temperature+"`F in Celsius is : "+FC+"`C");
                    break;
                case 3:
                    System.out.print("Enter the temperature : " );
                    temperature = in.nextDouble();
                    double CK = temperature + 273.15;
                    System.out.println(temperature+"`C in Kelvin is : "+CK+"`K");
                    break;
                case 4:
                    System.out.print("Enter the temperature : " );
                    temperature = in.nextDouble();
                    double KC = temperature - 273.15;
                    System.out.println(temperature+"`K in Celsius is : "+KC+"`C");
                    break;
                case 5:
                    System.out.print("Enter the temperature : " );
                    temperature = in.nextDouble();
                    double FK = (temperature - 32) * 5 / 9 + 273.15;
                    System.out.println(temperature+"`F in Kelvin is : "+FK+"`K");
                    break;
                case 6:
                    System.out.print("Enter the temperature : " );
                    temperature = in.nextDouble();
                    double KF = ((temperature - 273.15) * 9 / 5) + 32;
                    System.out.println(temperature+"`K in Fehrenheit is : "+KF+"`F");
                    break;
                case 7:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid Conversion Type. Try Again!");
            }
        }
    }
}
