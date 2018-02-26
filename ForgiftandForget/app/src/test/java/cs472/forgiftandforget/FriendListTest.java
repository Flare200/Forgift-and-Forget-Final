package cs472.forgiftandforget;

import org.junit.Test;

import java.util.ArrayList;

import cs472.forgiftandforget.DatabaseClasses.FriendList;

import static org.junit.Assert.*;

/**
 * Created by Tristan on 2/25/2018.
 */

public class FriendListTest {

    @Test
    public void Dev(){
        boolean sdf = FriendList.AddRow(new FriendList("molly", "john"));
        boolean sdfsd = FriendList.AddRow(new FriendList("molly", "john"));
        ArrayList<FriendList> arr = FriendList.GetRows("molly");
        boolean sdlfkj = FriendList.DeleteRow(new FriendList("molly", "john"));
    }
}