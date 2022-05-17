import java.sql.*;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.util.Properties;


/**
 * Main entry to program.
 */
public class p2 {
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;

	// JDBC Objects
	private static Connection con;
	private static Statement stmt;
	private static CallableStatement cstmt;
	private static ResultSet rs;

	private static Scanner scan;
	private static int customerID = -1;

	/**
	 * Initialize database connection given properties file.
	 * @param filename name of properties file
	 */
	public static void init(String filename) {
		try {
			Properties props = new Properties();						// Create a new Properties object
			FileInputStream input = new FileInputStream(filename);	// Create a new FileInputStream object using our filename parameter
			props.load(input);										// Load the file contents into the Properties object
			driver = props.getProperty("jdbc.driver");				// Load the driver
			url = props.getProperty("jdbc.url");						// Load the url
			username = props.getProperty("jdbc.username");			// Load the username
			password = props.getProperty("jdbc.password");			// Load the password
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test database connection.
	 */
	public static void testConnection() {
		System.out.println(":: TEST - CONNECTING TO DATABASE");
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, username, password);
			con.close();
			System.out.println(":: TEST - SUCCESSFULLY CONNECTED TO DATABASE");
		} catch (Exception e) {
			System.out.println(":: TEST - FAILED CONNECTED TO DATABASE");
			e.printStackTrace();
		}
	}

	public static void main(String argv[]) {

		if (argv.length < 1) {
			System.out.println("Need database properties filename");
		} else {
			p2.init(argv[0]);
			p2.testConnection();
			System.out.println();
		}

		scan = new Scanner(System.in);
		welcomeMenu();
	}

	public static void welcomeMenu() {

		boolean done = false;
		while (!done) {
			System.out.println("Welcome to the Self Services Banking System!");
			System.out.println("1. New Customer\n" + "2. Customer Login\n" + "3. Exit\n");
			System.out.println("Please enter the Corresponding Number from the given choice:");
			try {
				String input = scan.nextLine();
				int i = Integer.parseInt(input);
				if (i == 1) {
					con = DriverManager.getConnection(url, username, password);
					cstmt = con.prepareCall("{CALL P2.CUST_CRT(?,?,?,?,?,?,?)}");
					newCusMenu();
					cstmt.close();
					con.close();
				} else if (i == 2) {
					con = DriverManager.getConnection(url, username, password);
					cstmt = con.prepareCall("{CALL P2.CUST_LOGIN(?,?,?,?,?)}");
					loginMenu();
					cstmt.close();
					con.close();
				} else if (i == 3) {
					System.out.println("Exit - Hope you have a good day!\n");
					done = true;
				} else {
					System.out.println("Error - Input Must Be 1, 2, or 3\n ");
				}
			} catch (Exception e) {
				System.out.println("Error - Invalid Input\n ");
			}
		}
	}

	public static void newCusMenu() {
		System.out.println("\n\n\n***********************************************\n" +
				"New Customer - Create your account\n");
		boolean done = false;
		while (!done) {
			try {
				System.out.println("Enter y to continue: ");
				String input = scan.nextLine();
				if (!"y".equals(input)){
					return;
				}
				System.out.println("Please Enter your Name: ");
				String name = scan.nextLine();
				System.out.println("Please Enter your Gender (M or F): ");
				String gender = scan.nextLine();
				System.out.println("Please Enter your Age (Number Only): ");
				String in = scan.nextLine();
				int age = Integer.parseInt(in);
				System.out.println("Please Enter your Pin (Number Only): ");
				in = scan.nextLine();
				int pin = Integer.parseInt(in);
				cstmt.setString(1, name);
				cstmt.setString(2, gender);
				cstmt.setInt(3, age);
				cstmt.setInt(4, pin);
				cstmt.registerOutParameter(5, java.sql.Types.INTEGER);
				cstmt.registerOutParameter(6, java.sql.Types.INTEGER);
				cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
				cstmt.execute();
				int id = cstmt.getInt(5);
				int sqlCode = cstmt.getInt(6);
				if (sqlCode == 0) {
					System.out.println("\n-------------------------------------------------\n" + "THIS IS YOUR CUSTOMER ID: " + id + "\n\n\n");
					done = true;
				}
				else {
					System.out.println("\nError - " + cstmt.getString(7));
				}
			} catch (Exception e) {
				System.out.println("\nError - Invalid Input\n\n");
			}
		}
	}

	public static void loginMenu() {
		System.out.println("\n\n\n***********************************************\n" +
				"Login - Please Provide Your Information\n");
		boolean done = false;
		while (!done) {
			try {
				System.out.println("Enter y to continue: ");
				String input = scan.nextLine();
				if (!"y".equals(input)){
					welcomeMenu();
					return;
				}
				System.out.println("Please Enter your Customer ID (Number Only): ");
				String in = scan.nextLine();
				int id = Integer.parseInt(in);
				customerID = id;
				System.out.println("Please Enter your Pin (Number Only): ");
				in = scan.nextLine();
				int pin = Integer.parseInt(in);
				if (id == 0 && pin == 0){
					adminMainMenu();
					return;
				}
				cstmt.setInt(1, id);
				cstmt.setInt(2, pin);
				cstmt.registerOutParameter(3, java.sql.Types.INTEGER);
				cstmt.registerOutParameter(4, java.sql.Types.INTEGER);
				cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
				cstmt.execute();
				int sqlCode = cstmt.getInt(4);
				if (sqlCode == 0) {
					done = true;
					if (cstmt.getInt(3) == 1) {
						System.out.println("Successfully Login!\n\n");
						cusMainMenu(customerID);
					} else {
						System.out.println("Incorrect Pin!\n\n");
					}
				}
				else {
					System.out.println("\nError - " + cstmt.getString(5));
				}
				System.out.println("\n");
			} catch (Exception e) {
				System.out.println("Error - Invalid Input\n\n");
			}
		}
	}

	public static void cusMainMenu(int id){

		boolean done = false;
		while (!done) {
			try {
				System.out.println("\n\n\n***********************************************\n" +
						"Customer Main Menu!\n");
				System.out.println("1. Open Account\n" + "2. Close Account\n" + "3. Deposite\n" + "4. Withdraw\n" + "5. Transfer\n" + "6. Exist\n");
				System.out.println("Please enter the Corresponding Number from the given choice:");
				String input = scan.nextLine();
				int i = Integer.parseInt(input);
				if (i == 1) {
					cstmt = con.prepareCall("{CALL P2.ACCT_OPN(?,?,?,?,?,?)}");
					System.out.println("Please Enter the Customer ID for the new Account: ");
					String in = scan.nextLine();
					int cusid = Integer.parseInt(in);
					System.out.println("Please Enter Account Type (C for Checking, or S for Saving): ");
					String type = scan.nextLine();
					System.out.println("Please Enter your Initial Deposite: ");
					in = scan.nextLine();
					int balance = Integer.parseInt(in);
					cstmt.setInt(1, cusid);
					cstmt.setInt(2, balance);
					cstmt.setString(3, type);
					cstmt.registerOutParameter(4, java.sql.Types.INTEGER);
					cstmt.registerOutParameter(5, java.sql.Types.INTEGER);
					cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
					cstmt.execute();
					int sqlCode = cstmt.getInt(5);
					if (sqlCode == 0) {
						int num = cstmt.getInt(4);
						System.out.println("\n*************************************\n" + "THIS IS YOUR ACCOUNT NUMBER: " + num + "\n\n\n");
						System.out.println("Open Account Successfully!\n");
					} else {
						System.out.println("\nError - " + cstmt.getString(6) + "\n");
					}
				} else if (i == 2){
					cstmt = con.prepareCall("{CALL P2.ACCT_CLS(?,?,?)}");
					stmt = con.createStatement();
					System.out.println("Please Enter Account Number: ");
					String in = scan.nextLine();
					int number = Integer.parseInt(in);
					String query = "Select count(*) from p2.account where id = " + id + " and number = " + number;
					rs = stmt.executeQuery(query);
					int count = 0;
					while (rs.next()){
						count = rs.getInt(1);
					}
					if (count > 0){
						cstmt.setInt(1, number);
						cstmt.registerOutParameter(2, java.sql.Types.INTEGER);
						cstmt.registerOutParameter(3, java.sql.Types.VARCHAR);
						cstmt.execute();
						int sqlCode = cstmt.getInt(2);
						if (sqlCode == 0){
							System.out.println("Close Account Successfully!\n");
						} else {
							System.out.println("\nError - " + cstmt.getString(3) + "\n");
						}
					} else {
						System.out.println("Error - Unauthorized Account Number");
					}
					stmt.close();
					rs.close();
				} else if (i == 3) {
					cstmt = con.prepareCall("{CALL P2.ACCT_DEP(?,?,?,?)}");
					System.out.println("Please Enter Account Number: ");
					String in = scan.nextLine();
					int accNum = Integer.parseInt(in);
					System.out.println("Please Enter Deposit Amount: ");
					in = scan.nextLine();
					int depoAmount = Integer.parseInt(in);
					cstmt.setInt(1, accNum);
					cstmt.setInt(2, depoAmount);
					cstmt.registerOutParameter(3, java.sql.Types.INTEGER);
					cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
					cstmt.execute();
					int sqlCode = cstmt.getInt(3);
					if (sqlCode == 0){
						System.out.println("Desposit Successfully!\n");
					} else {
						System.out.println("\nError - " + cstmt.getString(4) + "\n");
					}
				} else if (i == 4){
					cstmt = con.prepareCall("{CALL P2.ACCT_WTH(?,?,?,?)}");
					stmt = con.createStatement();
					System.out.println("Please Enter Account Number: ");
					String in = scan.nextLine();
					int accNum = Integer.parseInt(in);
					System.out.println("Please Enter Withdraw Amount: ");
					in = scan.nextLine();
					int withdrawAmount = Integer.parseInt(in);
					String query = "Select count(*) from p2.account where id = " + id + " and number = " + accNum;
					rs = stmt.executeQuery(query);
					int count = 0;
					while (rs.next()){
						count = rs.getInt(1);
					}
					if (count > 0){
						cstmt.setInt(1, accNum);
						cstmt.setInt(2, withdrawAmount);
						cstmt.registerOutParameter(3, java.sql.Types.INTEGER);
						cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
						cstmt.execute();
						int sqlCode = cstmt.getInt(3);
						if (sqlCode == 0){
							System.out.println("Withdraw Successfully!\n");
						} else {
							System.out.println("\nError - " + cstmt.getString(4) + "\n");
						}
					} else {
						System.out.println("Error - Unauthorized Account Number");
					}
					stmt.close();
					rs.close();
				} else if (i == 5){
					cstmt = con.prepareCall("{CALL P2.ACCT_TRX(?,?,?,?,?)}");
					stmt = con.createStatement();
					System.out.println("Please Enter Source Account Number: ");
					String in = scan.nextLine();
					int srcAccNum = Integer.parseInt(in);
					System.out.println("Please Enter Destination Account Amount: ");
					in = scan.nextLine();
					int destAccNum = Integer.parseInt(in);
					System.out.println("Please Enter Transfer Amount: ");
					in = scan.nextLine();
					int transferAmou = Integer.parseInt(in);
					String query = "Select count(*) from p2.account where id = " + id + " and number = " + srcAccNum;
					rs = stmt.executeQuery(query);
					int count = 0;
					while (rs.next()){
						count = rs.getInt(1);
					}
					if (count > 0){
						cstmt.setInt(1, srcAccNum);
						cstmt.setInt(2, destAccNum);
						cstmt.setInt(3, transferAmou);
						cstmt.registerOutParameter(4, java.sql.Types.INTEGER);
						cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
						cstmt.execute();
						int sqlCode = cstmt.getInt(4);
						if (sqlCode == 0){
							System.out.println("Transfer Successfully!\n");
						} else {
							System.out.println("\nError - " + cstmt.getString(5) + "\n");
						}
					} else {
						System.out.println("Error - Unauthorized Account Number");
					}
					stmt.close();
					rs.close();
				} else if (i == 6){
					System.out.println("Exit - Hope you have a good day!\n\n");
					done = true;
				} else {
					System.out.println("\nError - Input Must Be 1, 2, 3, 4 ,5 ,6 or 7\n");
				}
			} catch (Exception e) {
				System.out.println("\nError - Invalid Input\n\n");
			}
		}
	}

	public static void adminMainMenu(){
		boolean done = false;
		while (!done) {
			try {
				System.out.println("\n\n\n***********************************************\n" +
						"Administor Main Menu!\n");
				System.out.println("1. Add Interest\n" + "2. Exist\n");
				System.out.println("Please enter the Corresponding Number from the given choice:");
				String input = scan.nextLine();
				int i = Integer.parseInt(input);
				if (i == 1){
					cstmt = con.prepareCall("{CALL P2.ADD_INTEREST(?,?,?,?)}");
					System.out.println("Please Enter Saving Rate (Must Be in Float, Such as 0.05): ");
					String in = scan.nextLine();
					float savingRate = Float.parseFloat(in);
					System.out.println("Please Enter Checking Rate (Must Be in Float, Such as 0.05): ");
					in = scan.nextLine();
					float checkingRate = Float.parseFloat(in);
					cstmt.setFloat(1, savingRate);
					cstmt.setFloat(2, checkingRate);
					cstmt.registerOutParameter(3, java.sql.Types.INTEGER);
					cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
					cstmt.execute();
					int sqlCode = cstmt.getInt(3);
					if (sqlCode == 0){
						System.out.println("Add Interest Successfully!\n");
					} else {
						System.out.println("\nError - " + cstmt.getString(4) + "\n");
					}
				} else if (i == 2){
					System.out.println("Exit - Hope you have a good day!\n\n");
					done = true;
				} else {
					System.out.println("Error - Input Must Be 1 or 2\n");
				}
			} catch (Exception e) {
				System.out.println("Error - Invalid Input\n");
			}
		}
	}
}