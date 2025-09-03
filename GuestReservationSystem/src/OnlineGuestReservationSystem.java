import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Reservation{
    private int GuestId;
    private String GuestName;
    private String ArrivalDate;
    private int numberOfGuest;

    public Reservation(int GuestId, String GuestName, String ArrivalDate, int numberOfGuest){
        this.GuestId = GuestId;
        this.GuestName = GuestName;
        this.ArrivalDate = ArrivalDate;
        this.numberOfGuest = numberOfGuest;
    }
    public int getGuestId(){
        return GuestId;
    }
    public String getGuestName(){
        return GuestName;
    }
    public String getArrivalDate(){
        return ArrivalDate;
    }
    public int getNumberOfGuest(){
        return numberOfGuest;
    }
}
class ReservationSystem{
    private List<Reservation> reservations = new ArrayList();
    private int nextGuestId = 1;

    public Reservation makeReservation(String GuestName, String ArrivalDate, int numberOfGuest){
    Reservation reservation = new Reservation(nextGuestId++, GuestName, ArrivalDate, numberOfGuest);
    reservations.add(reservation);
    return reservation;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
    public Reservation getReservationById(int GuestId){
        for(Reservation reservation : reservations){
            if(reservation.getGuestId()==GuestId){
                return reservation;
            }
        }
        System.out.println("Not found any");
        return null;
    }
    public boolean cancelReservation(int GuestId){
        Reservation reservation = getReservationById(GuestId);
        if(reservation != null){
            reservations.remove(reservation);
            return true;
        }
        return false;
    }
}
class ReservationSystemUI{
    private ReservationSystem reservationSystem = new ReservationSystem();

    public void start(){
        Scanner in = new Scanner (System.in);
        while(true){
            System.out.println("1. Make a Reservation");
            System.out.println("2. View all Reservation");
            System.out.println("3. Remove a Reservation");
            System.out.println("4. Exit");

            int choice = in.nextInt();

            switch(choice){
                case 1:
                    System.out.print("Enter the name of the Guest : ");
                    String GuestName = in.next();
                    System.out.print("Enter the guest's Arrival Date : ");
                    String ArrivalDate = in.next();
                    System.out.print("Enter the number of guest : ");
                    int numberOfGuest = in.nextInt();
                    Reservation reservation = reservationSystem.makeReservation(GuestName,ArrivalDate,numberOfGuest);
                    System.out.println("The ID of the guest is : "+reservation.getGuestId());
                    break;
                case 2:
                    System.out.println("Reservations : ");
                    for(Reservation r : reservationSystem.getReservations()){
                        System.out.println(r.getGuestId()+" - "+ r.getGuestName()+" - "+r.getArrivalDate()+" - "+r.getNumberOfGuest());
                    }
                    break;
                case 3:
                    System.out.print("Enter the ID you wish to remove : ");
                    int GuestId = in.nextInt();
                    if(reservationSystem.cancelReservation(GuestId)){
                        System.out.println("Reservation Canceled");
                    }
                    else{
                        System.out.println("Reservation not found");
                    }
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid Choice");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        ReservationSystemUI obj = new ReservationSystemUI();
        obj.start();
    }
}

