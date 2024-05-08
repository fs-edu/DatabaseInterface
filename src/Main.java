import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;


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

    public String hashWithSalt(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(salt);

        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    public boolean verifyPassword(String inputPassword, String storedHash, byte[] salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);

        byte[] hashedInputPassword = md.digest(inputPassword.getBytes());
        String newHash = Base64.getEncoder().encodeToString(hashedInputPassword);

        return newHash.equals(storedHash);
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
        String newHashedPassword = this.hashWithSalt(newPassword);

        String sqlStatement = """
                CREATE USER '%s'@'localhost' IDENTIFIED BY '%s';
                """;

        if (this.executeSql(sqlStatement.formatted(newUsername, newHashedPassword))) {
            printImportantMsg("Benutzer wurde angelegt!");
            this.askForRoles(newUsername);
        } else {
            this.getNextTask();
        }

    }

    public void grandRoles(String username) {
        String sqlStatement = """
                GRANT ALL PRIVILEGES ON *.* TO '%s'@'localhost' WITH GRANT OPTION;
                """;
        if (this.executeSql(sqlStatement.formatted(username))) {
            printImportantMsg("Rechte wurde angelegt!");
        }
        this.getNextTask();
    }

    public void askForRoles(String username) {
        String task = getUserInput("Soll der Benutzer alle Rechte bekommen? (y/n)");
        if (task.equalsIgnoreCase("y")) {
            this.grandRoles(username);
        } else if (task.equalsIgnoreCase("n")) {
            this.getNextTask();
        } else {
            this.print("wat?");
            this.askForRoles(username);
        }
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
