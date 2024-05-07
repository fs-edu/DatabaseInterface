import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {

    String AdminUsername, AdminPassword, Endpoint;

    public Main() {
        Endpoint = "jdbc:mysql://localhost:3306/versand";
    }

    public void print(String msg) {
        System.out.println(msg);
    }

    public void printImportantMsg(String msg) {
        String line = "=".repeat(msg.length());
        System.out.println(line);
        print(msg);
        System.out.println(line);
        System.out.println("\n");
    }

    public String getUserInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        print(prompt);
        return scanner.nextLine();
    }

    private void setAdminUsername(String adminUsername) {
        AdminUsername = adminUsername;
    }

    private void setAdminPassword(String adminPassword) {
        AdminPassword = adminPassword;
    }

    public Boolean testConnection(String username, String password) {
        try {
            Connection con = DriverManager.getConnection(
                    Endpoint,
                    username,
                    password
            );
            con.createStatement();
            return true;
        } catch (SQLException e) {
            this.printImportantMsg(e.getMessage());
            return false;
        }
    }

    public void Login() {
        String tempUsername = getUserInput("Bitte Benutzernamen eingeben:");
        String tempPassword = getUserInput("Bitte Kennwort eingeben:");

        Boolean connectionTest = this.testConnection(tempUsername, tempPassword);

        if (connectionTest) {
            this.printImportantMsg("Verbindung war erfolgreich! \nWillkommen " + tempUsername);
            this.setAdminUsername(tempUsername);
            this.setAdminPassword(tempPassword);
        } else {
            this.Login();
        }
    }

    public Boolean executeSql(String sqlStatement) {
        try {
            Connection con = DriverManager.getConnection(
                    this.Endpoint,
                    this.AdminUsername,
                    this.AdminPassword
            );
            Statement stmt = con.createStatement();
            stmt.execute(sqlStatement);
            return true;
        } catch (SQLException e) {
            this.printImportantMsg(e.getMessage());
            return false;
        }
    }

    public void addNewUser() {

        String newUsername = getUserInput("Bitte neuen Benutzernamen eingeben:");
        String newPassword = getUserInput("Bitte neues Kennwort eingeben:");

        String sqlStatement = """
                CREATE USER '%s'@'localhost' IDENTIFIED BY '%s';
                """;

        if (this.executeSql(sqlStatement.formatted(newUsername, newPassword))) {
            printImportantMsg("Benutzer wurde angelegt!");
        }
        this.getNextTask();
    }

    public void getNextTask() {
        String task = getUserInput("Soll ein neuer Benutzer angelegt werden? (y/n)");

        if (task.equalsIgnoreCase("y")) {
            this.addNewUser();
        } else if (task.equalsIgnoreCase("n")) {
            this.print("Bye");
        } else {
            this.print("wat?");
            this.getNextTask();
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.Login();
        app.getNextTask();
    }
}
