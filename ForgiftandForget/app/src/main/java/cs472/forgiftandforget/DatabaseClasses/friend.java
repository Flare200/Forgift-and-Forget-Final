package cs472.forgiftandforget.DatabaseClasses;

/**
 * Created by mike_ on 3/7/2018.
 */

public class friend {
    private String name;
    private String address;
    private String email;
    private String date;
    private String FID;

    public friend(){
        // need public empty constructor for firebase
    }

    // parametrized constructor, setting FID to null(will be updated on database addFriend function)
    public friend(String name, String address, String email, String date){
        this.address = address;
        this.email = email;
        this.name = name;
        this.date = date;
        this.FID = "null";
    }


    // need public setters and getters for firebase
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAddr() { return address; }

    public void setAddr(String address) { this.address = address; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date;}

    public String getFID() {return FID; }

    public void setFID(String FID) { this.FID = FID; }

}
