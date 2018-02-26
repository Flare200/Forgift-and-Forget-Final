package cs472.forgiftandforget.DatabaseClasses;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Created by Tristan on 2/25/2018.
 */

public class Reminder {
    public String userLoginString;
    public String recipLoginString;
    public Date triggerDate;
    public int refGiftID;

    public static boolean AddRow(){
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static boolean UpdateRow(){
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static boolean DeleteRow(){
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static ArrayList<Reminder> GetRow(){
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
