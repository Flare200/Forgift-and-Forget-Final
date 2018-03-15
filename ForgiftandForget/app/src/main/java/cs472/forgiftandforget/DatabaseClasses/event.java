package cs472.forgiftandforget.DatabaseClasses;

/**
 * Created by mike_ on 3/15/2018.
 */

public class event {
    private String name;
    private String date;
    private String eid;

    public event(){
        // empty constructor required for firebase
    }

    //parametrized constructor setting event id to null(will be updated on database addEvent method)
    public event(String name, String date){
        this.name = name;
        this.date = date;
        this.eid = "null";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

}
