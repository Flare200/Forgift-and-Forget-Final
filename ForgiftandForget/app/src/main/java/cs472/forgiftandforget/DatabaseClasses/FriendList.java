package cs472.forgiftandforget.DatabaseClasses;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Tristan on 2/25/2018.
 */

public class FriendList extends Database {
    public String userLoginString;
    public String friendLoginString;

    public FriendList(String userLogin, String friendLogin) {
        userLoginString = userLogin;
        friendLoginString = friendLogin;
    }

    public static boolean AddRow(FriendList row) {
        String cmd = String.format(Locale.US,
                "INSERT INTO friendList (userLoginString, friendLoginString) " +
                        " VALUES ('%s', '%s')",
                row.userLoginString, row.friendLoginString);

        ExecSqlCmd(cmd);
        return lastQuerySuccess;
    }

    public static boolean DeleteRow(FriendList row) {
        String cmd = String.format(Locale.US,
                "DELETE FROM friendList " +
                        "WHERE (userLoginString='%s' AND friendLoginString='%s')",
                row.userLoginString, row.friendLoginString);

        ExecSqlCmd(cmd);
        return lastQuerySuccess;
    }

    public static ArrayList<FriendList> GetRows(String key_userLoginString) {
        String cmd = String.format(Locale.US,
                "SELECT friendLoginString " +
                        "FROM friendList " +
                        "WHERE userLoginString='%s'",
                key_userLoginString);

        ExecSqlCmd(cmd);

        ArrayList<FriendList> arr = new ArrayList<>();
        try {
            while (lastQueryResult.next()) {
                String tmpStr = lastQueryResult.getString("friendLoginString");
                arr.add(new FriendList(key_userLoginString, tmpStr));
                System.out.println(tmpStr);
            }
            return arr;
        } catch (Exception e) {
            return null;
        }
    }

}
