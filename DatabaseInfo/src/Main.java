import javax.xml.crypto.Data;

public class Main
{

	public static void main(String[] args)
	{
		Database db = new Database();

		int retVal = db.ExecSqlCmd("select * from loginInfo where loginString='john.smith';");
		System.out.println("The sql cmd returned: " + retVal);
	}


}