package helper;

import controller.Helper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.sql.*;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Elias Adams-Roberts
 * Contains all methods which interact with the database
 */
public abstract class JDBC {

    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER"; // LOCAL
    private static final String driver = "com.mysql.cj.jdbc.Driver"; // Driver reference
    private static final String userName = "sqlUser"; // Username
    public static Connection connection;  // Connection Interface
    public static int currentUser;

    public static void openConnection()
    {
        try {
            Class.forName(driver); // Locate Driver
            // Password
            String password = "Passw0rd!";
            connection = DriverManager.getConnection(jdbcUrl, userName, password); // Reference Connection object
            System.out.println("Connection successful!");
        }
        catch(Exception e)
        {
            System.out.println("Error:" + e.getMessage());
        }
    }

    /** Close current connection to database */
    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection closed!");
        }
        catch(Exception e)
        {
            System.out.println("Error:" + e.getMessage());
        }
    }

    /**
     * Get current user
     * @return currentUser
     */
    public static int getCurrentUser() {
        return currentUser;
    }

    /** Method to get current user's name
     * @return String current user's name */
    public static String getCurrentUserName(int userId) throws SQLException {
        String userName = null;
        String sql = "SELECT User_Name FROM users WHERE User_ID = " + userId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while (rs.next()) {
            userName = rs.getString("User_Name");
        }
        return userName;
    }

    /**
     * Sets the current user
     * @param user ID of the user who just logged in
     */
    public static void setCurrentUser(int user) {
        currentUser = user;
    }

    /**
     * Return name of state (division) when given ID
     * @return String divisionName */
    public static String stateNameFromId(int id) throws SQLException {
        String divisionName = null;
        String sql = "SELECT Division FROM first_level_divisions WHERE Division_ID = " + id;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while (rs.next()) {
            divisionName = rs.getString("Division");
        }
        return divisionName;
    }

    /**
     * Return ID of state (division) when given name
     * @param name String
     * @return int divisionId */
    public static int stateIdFromName(String name) throws SQLException {
        int divisionId = 0;
        String sql = "SELECT Division_ID FROM first_level_divisions WHERE Division = '" + name + "'";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while (rs.next()) {
            divisionId = rs.getInt("Division_ID");
        }
        return divisionId;
    }

    /**
     * Return name of Country from Division ID
     * @param id int
     * @return string countryName */
    public static String countryFromDivisionId(int id) throws SQLException {
      int countryId = 0;
      String countryName = null;
      String sql = "SELECT Country_ID FROM first_level_divisions WHERE Division_ID = " + id;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while (rs.next()) {
            countryId = rs.getInt("Country_ID");
        }
        switch (countryId) {
            case 1 -> countryName = "U.S";
            case 2 -> countryName = "UK";
            case 3 -> countryName = "Canada";
        }
        return countryName;
    }

    /***
     * Method to delete customer from database
     * @param customerId */
    public static void deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM CUSTOMERS WHERE Customer_ID = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setInt(1, customerId);
        ps.executeUpdate();
    }

    /**
     * Method to loop through users table and look for a username/password match
     * @param userName String
     * @param password String
     * @return true if user/password match is found, false otherwise */
    public static boolean checkLogin(String userName, String password) throws SQLException {
        boolean match = false;
        String sql = "SELECT * FROM USERS";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            String user = rs.getString("User_Name");
            String userPassword = rs.getString("Password");
            if((Objects.equals(user, userName)) && (Objects.equals(userPassword, password))) {
                setCurrentUser(rs.getInt("User_ID"));
                match = true;
            }
        }
        return match;
    }

    /**
     * Retrieve data from customers table to fill Customers tableview in CustomerForm.java
     * @return ObservableList customers */
    public static ObservableList<Customer> getCustomers() throws SQLException {
        String sql = "SELECT * FROM CUSTOMERS";
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int customerId = rs.getInt("Customer_ID");
            String customerName = rs.getString("Customer_Name");
            String customerAddress = rs.getString("Address");
            int customerDivision = rs.getInt("Division_ID");
            String customerPostal = rs.getString("Postal_Code");
            String customerPhone = rs.getString("Phone");
            String isVIP = rs.getString("is_VIP");
            Customer customer = new Customer(customerId, customerName, customerAddress, customerDivision, customerPostal, customerPhone, isVIP);
            customers.add(customer);
        }
        return customers;
    }

    /**
     * Method to retrieve list customers whose name matches search term */
    public static ObservableList<Customer> getCustomersBySearch(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM CUSTOMERS WHERE Customer_Name LIKE '%" + searchTerm + "%'";
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int customerId = rs.getInt("Customer_ID");
            String customerName = rs.getString("Customer_Name");
            String customerAddress = rs.getString("Address");
            int customerDivision = rs.getInt("Division_ID");
            String customerPostal = rs.getString("Postal_Code");
            String customerPhone = rs.getString("Phone");
            String isVIP = rs.getString("is_VIP");
            Customer customer = new Customer(customerId, customerName, customerAddress, customerDivision, customerPostal, customerPhone, isVIP);
            customers.add(customer);
        }
        return customers;
    }

    /** Method to retrieve list of customers where Is_VIP = 1 */
    public static ObservableList<Customer> getVIPCustomers() throws SQLException {
        String sql = "SELECT * FROM CUSTOMERS WHERE is_VIP = 'Yes'";
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int customerId = rs.getInt("Customer_ID");
            String customerName = rs.getString("Customer_Name");
            String customerAddress = rs.getString("Address");
            int customerDivision = rs.getInt("Division_ID");
            String customerPostal = rs.getString("Postal_Code");
            String customerPhone = rs.getString("Phone");
            String isVIP = rs.getString("is_VIP");
            Customer customer = new Customer(customerId, customerName, customerAddress, customerDivision, customerPostal, customerPhone, isVIP);
            customers.add(customer);
        }
        return customers;
    }

    /** Method to retrieve VIP status of customer using customer ID */
    public static String getVIPStatus(int customerId) throws SQLException {
        String sql = "SELECT Is_VIP FROM CUSTOMERS WHERE Customer_ID = " + customerId;
        String VIP = null;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            VIP = rs.getString("is_VIP");
        }
        return VIP;
    }

    /** Method to retrieve list of all products */
    public static ObservableList<Product> getProducts() throws SQLException {
        String sql = "SELECT * FROM PRODUCTS";
        ObservableList<Product> products = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int productId = rs.getInt("Product_ID");
            String productName = rs.getString("Name");
            double productPrice = rs.getDouble("Price");
            Product product = new Product(productId, productName, productPrice);
            products.add(product);
        }
        return products;
    }

    /** Method to retrieve list of all product names */
    public static ObservableList<String> getProductNames() throws SQLException {
        String sql = "SELECT Name FROM PRODUCTS";
        ObservableList<String> productNames = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            String productName = rs.getString("Name");
            productNames.add(productName);
        }
        return productNames;
    }

    /** Method to retrieve product price using product name */
    public static double getProductPrice(String productName) throws SQLException {
        String sql = "SELECT Price FROM PRODUCTS WHERE Name = '" + productName + "'";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        double productPrice = 0;
        while(rs.next()) {
            productPrice = rs.getDouble("Price");
        }
        return productPrice;
    }

    /** Method to retrieve product ID using product name */
    public static int getProductId(String productName) throws SQLException {
        String sql = "SELECT Product_ID FROM PRODUCTS WHERE Name = '" + productName + "'";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        int productId = 0;
        while(rs.next()) {
            productId = rs.getInt("Product_ID");
        }
        return productId;
    }

    /** Method to add a new product to the database */
    public static void addProduct(String name, double price) throws SQLException {
        String sql = "INSERT INTO PRODUCTS (Name, Price) VALUES (?, ?)";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, name);
        ps.setDouble(2, price);
        ps.executeUpdate();
    }

    /** Method to update a product in the database */
    public static void updateProduct(int productId, String name, double price) throws SQLException {
        String sql = "UPDATE PRODUCTS SET Name = ?, Price = ? WHERE Product_ID = " + productId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, name);
        ps.setDouble(2, price);
        ps.executeUpdate();
    }

    /** Method to delete a product from the database */
    public static void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM PRODUCTS WHERE Product_ID = " + productId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.executeUpdate();
    }

    /** Method to add a sale to the database */
    public static void addSale(double price, int customerId, int userId, int productId, String productName) throws SQLException {
        String sql = "INSERT INTO SALES (Price, Customer_ID, User_ID, Product_ID, Product_Name, Sale_Date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setDouble(1, price);
        ps.setInt(2, customerId);
        ps.setInt(3, userId);
        ps.setInt(4, productId);
        ps.setString(5, productName);
        ps.executeUpdate();
    }

    /** Method which returns true if product ID exists in sales table */
    public static boolean productExists(int productId) throws SQLException {
        String sql = "SELECT * FROM SALES WHERE Product_ID = " + productId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        boolean exists = false;
        while(rs.next()) {
            exists = true;
        }
        return exists;
    }

    /** Method to remove a sale from the database */
    public static void removeSale(int saleId) throws SQLException {
        String sql = "DELETE FROM SALES WHERE Sale_ID = " + saleId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.executeUpdate();
    }

    /** Retrieve list of sales by customer name */
    public static ObservableList<Sale> getSalesByCustomerId(int id) throws SQLException {
        String sql = "SELECT * FROM SALES WHERE Customer_ID = " + id;
        ObservableList<Sale> sales = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int saleId = rs.getInt("Sale_ID");
            double price = rs.getDouble("Price");
            int customerId = rs.getInt("Customer_ID");
            int userId = rs.getInt("User_ID");
            int productId = rs.getInt("Product_ID");
            String productName = rs.getString("Product_Name");
            Timestamp saleDate = rs.getTimestamp("Sale_Date");
            Sale sale = new Sale(saleId, price, customerId, userId, productId, productName, saleDate);
            sales.add(sale);
        }
        return sales;
    }


    /**
     * Retrieve Appointments associated with Customer_ID
     * @return ObservableList divisions */
    // TODO: Might need to also add one using USER_ID instead/also
    public static ObservableList<String> getDivisionsById(String id) throws SQLException {
        String sql = "SELECT Division_ID, Division FROM first_level_divisions WHERE Country_ID=" + id;
        ObservableList<String> divisions = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            String divisionName = rs.getString("Division");
            divisions.add(divisionName);
        }
        return divisions;
    }

    /** Method to retrieve all appointments
     * @return Observablelist of all appointments */
    public static ObservableList<Appointment> getAppointments() throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS";
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        appointments.clear();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int appointmentId = rs.getInt("Appointment_ID");
            String appointmentTitle = rs.getString("Title");
            String appointmentDescription = rs.getString("Description");
            String appointmentLocation = rs.getString("Location");
            String appointmentType = rs.getString("Type");
            Timestamp appointmentStart = rs.getTimestamp("Start");
            Timestamp appointmentEnd = rs.getTimestamp("End");
            int appointmentCustomer = rs.getInt("Customer_ID");
            int appointmentUser = rs.getInt("User_ID");
//            Timestamp start = Helper.toLocal(appointmentStart);
//            Timestamp end = Helper.toLocal(appointmentEnd);
            Timestamp start = Timestamp.valueOf(appointmentStart.toLocalDateTime());
            Timestamp end = Timestamp.valueOf(appointmentEnd.toLocalDateTime());
            Appointment appointment = new Appointment(appointmentId, appointmentTitle, appointmentDescription,
                    appointmentLocation, appointmentType, start,
                    end, appointmentCustomer, appointmentUser);

            appointments.add(appointment);
        }
        return appointments;
    }

    /** Method to retrieve all of the current user's appointments
     * @return ObservableList of appointments */
    public static ObservableList<Appointment> getUserAppointments() throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE User_ID = " + getCurrentUser();
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        appointments.clear();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int appointmentId = rs.getInt("Appointment_ID");
            String appointmentTitle = rs.getString("Title");
            String appointmentDescription = rs.getString("Description");
            String appointmentLocation = rs.getString("Location");
            String appointmentType = rs.getString("Type");
            Timestamp appointmentStart = rs.getTimestamp("Start");
            Timestamp appointmentEnd = rs.getTimestamp("End");
            int appointmentCustomer = rs.getInt("Customer_ID");
            int appointmentUser = rs.getInt("User_ID");
//            Timestamp start = Helper.toLocal(appointmentStart);
//            Timestamp end = Helper.toLocal(appointmentEnd);
            Timestamp start = Timestamp.valueOf(appointmentStart.toLocalDateTime());
            Timestamp end = Timestamp.valueOf(appointmentEnd.toLocalDateTime());
            Appointment appointment = new Appointment(appointmentId, appointmentTitle, appointmentDescription,
                    appointmentLocation, appointmentType, start,
                    end, appointmentCustomer, appointmentUser);

            appointments.add(appointment);
        }
        return appointments;
    }

    /** Method to retrieve all appointments occurring this month
     * @return ObservableList of appointments occurring this month */
    public static ObservableList<Appointment> getMonthAppointments() throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE MONTH(Start) = MONTH(CURRENT_DATE())";
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        appointments.clear();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int appointmentId = rs.getInt("Appointment_ID");
            String appointmentTitle = rs.getString("Title");
            String appointmentDescription = rs.getString("Description");
            String appointmentLocation = rs.getString("Location");
            String appointmentType = rs.getString("Type");
            Timestamp appointmentStart = rs.getTimestamp("Start");
            Timestamp appointmentEnd = rs.getTimestamp("End");
            int appointmentCustomer = rs.getInt("Customer_ID");
            int appointmentUser = rs.getInt("User_ID");
            Timestamp start = Helper.toLocal(appointmentStart);
            Timestamp end = Helper.toLocal(appointmentEnd);
            Appointment appointment = new Appointment(appointmentId, appointmentTitle, appointmentDescription,
                    appointmentLocation, appointmentType, start,
                    end, appointmentCustomer, appointmentUser);

            appointments.add(appointment);
        }
        return appointments;
    }

    /** Method to retrieve all appointments occurring this week
     * @return Observablelist of appointments happening this week */
    public static ObservableList<Appointment> getWeekAppointments() throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE WEEK(Start) = WEEK(CURRENT_DATE())";
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        appointments.clear();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int appointmentId = rs.getInt("Appointment_ID");
            String appointmentTitle = rs.getString("Title");
            String appointmentDescription = rs.getString("Description");
            String appointmentLocation = rs.getString("Location");
            String appointmentType = rs.getString("Type");
            Timestamp appointmentStart = rs.getTimestamp("Start");
            Timestamp appointmentEnd = rs.getTimestamp("End");
            int appointmentCustomer = rs.getInt("Customer_ID");
            int appointmentUser = rs.getInt("User_ID");
            Timestamp start = Timestamp.valueOf(appointmentStart.toLocalDateTime());
            Timestamp end = Timestamp.valueOf(appointmentEnd.toLocalDateTime());
            Appointment appointment = new Appointment(appointmentId, appointmentTitle, appointmentDescription,
                    appointmentLocation, appointmentType, start,
                    end, appointmentCustomer, appointmentUser);

            appointments.add(appointment);
        }
        return appointments;
    }

    /**
     * Retrieve Appointments associated with Customer_ID
     * @return Observablelist of appointments */
    public static ObservableList<Appointment> getAppointmentsById(String id) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE Customer_ID=" + id;
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int appointmentId = rs.getInt("Appointment_ID");
            String title = rs.getString("Title");
            String description = rs.getString("Description");
            String location = rs.getString("Location");
            String type = rs.getString("Type");
            Timestamp appointmentStart = rs.getTimestamp("Start");
            Timestamp appointmentEnd = rs.getTimestamp("End");
            int customerId = rs.getInt("Customer_ID");
            int userId = rs.getInt("User_ID");
//            Timestamp start = Helper.toLocal(appointmentStart);
//            Timestamp end = Helper.toLocal(appointmentEnd);
            Timestamp start = Timestamp.valueOf(appointmentStart.toLocalDateTime());
            Timestamp end = Timestamp.valueOf(appointmentEnd.toLocalDateTime());

            Appointment appointment = new Appointment(appointmentId, title, description, location, type, start, end, customerId, userId);
            appointments.add(appointment);
        }
        return appointments;
    }


    /**
     * Retrieve All Divisions
     * @return ObservableList of all divisions info */
    // TODO: Might need to also add one using USER_ID instead/also
    public static ObservableList<Division> getAllDivisions() throws SQLException {
        String sql = "SELECT * FROM first_level_divisions";
        ObservableList<Division> divisions = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
//        ps.setString(1, id);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            int divisionId = rs.getInt("Division_ID");
            String divisionName = rs.getString("Division");
            Timestamp createDate = rs.getTimestamp("Create_Date");
            String createdBy = rs.getString("Created_By");
            Timestamp lastUpdate = rs.getTimestamp("Last_Update");
            String lastUpdatedBy = rs.getString("Last_Updated_By");
            int countryId = rs.getInt("Country_ID");

            Division division = new Division(divisionId, divisionName, createDate, createdBy, lastUpdate, lastUpdatedBy, countryId);
            divisions.add(division);
        }
        return divisions;
    }

    /**
     * Method to delete appointment from database
     * @param appointmentId int */
    public static void deleteAppointment(int appointmentId) throws SQLException {
        String sql = "DELETE FROM APPOINTMENTS WHERE Appointment_ID = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setInt(1, appointmentId);
        ps.executeUpdate();
    }

// ********* Contacts have been removed from the database ********
//    public static ObservableList<Contact> getContacts() throws SQLException {
//        String sql = "SELECT * FROM CONTACTS";
//        ObservableList<Contact> contacts = FXCollections.observableArrayList();
//        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
//        ResultSet rs = ps.executeQuery(sql);
//        while(rs.next()) {
//            int contactId = rs.getInt("Contact_ID");
//            String contactName = rs.getString("Contact_Name");
//            String contactEmail = rs.getString("Email");
//            Contact contact = new Contact(contactId, contactName, contactEmail);
//            contacts.add(contact);
//        }
//        return contacts;
//    }


//    public static ObservableList<String> getContactNames() throws SQLException {
//        String sql = "SELECT Contact_Name FROM CONTACTS";
//        ObservableList<String> contactNames = FXCollections.observableArrayList();
//        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
//        ResultSet rs = ps.executeQuery(sql);
//        while(rs.next()) {
//            String contactName = rs.getString("Contact_Name");
//            contactNames.add(contactName);
//        }
//        return contactNames;
//    }

    /** Method to retrieve and return all customer names
     * @return Observablelist of all customer names */
    public static ObservableList<String> getCustomerNames() throws SQLException {
        String sql = "SELECT Customer_Name FROM CUSTOMERS";
        ObservableList<String> customerNames = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            String customerName = rs.getString("Customer_Name");
            customerNames.add(customerName);
        }
        return customerNames;
    }

    /** Method to retrieve and return all user names
     * @return Observablelist of all user names */
    public static ObservableList<String> getUserNames() throws SQLException {
        String sql = "SELECT User_Name FROM USERS";
        ObservableList<String> userNames = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) {
            String userName = rs.getString("User_Name");
            userNames.add(userName);
        }
        return userNames;
    }

    /** Method to return observable list of all users
     * @return Observablelist of all users */
    public static ObservableList<User> getAllUsers() throws SQLException {
        String sql = "SELECT User_ID, User_Name from users";
        ObservableList<User> users = FXCollections.observableArrayList();
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int userId = rs.getInt("User_ID");
            String userName = rs.getString("User_Name");
            User user = new User(userId, null, userName);
            users.add(user);
        }
        return users;
    }

    /**
     * Method to modify existing customer
     * @param customerId int
     * @param customerName String
     * @param address String
     * @param postal String
     * @param phone String
     * @param divisionID int
     * */ // TODO: FIX isVIP error
    public static void updateCustomer(int customerId, String customerName, String address, String postal,
                                      String phone, int divisionID, String isVIP) throws SQLException {
        String sql = "UPDATE CUSTOMERS SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, "+
                "Last_Update = CURRENT_TIMESTAMP, Last_Updated_by = ?, Division_ID = ?, is_VIP = ? WHERE Customer_ID = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, customerName);
        ps.setString(2, address);
        ps.setString(3, postal);
        ps.setString(4, phone);
        ps.setInt(5, getCurrentUser());
        ps.setInt(6, divisionID);
        ps.setString(7, isVIP);
        ps.setInt(8, customerId);

        ps.executeUpdate();
    }

    /**
     * Method to add customer to database
     * @param customerName String
     * @param address String
     * @param postal String
     * */
    public static void addCustomer(String customerName, String address, String postal,
                                   String phone, int divisionID, String isVIP) throws SQLException {
        String sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, "+
                "Create_Date, Created_By, Last_Update, Last_Updated_by, Division_ID, is_VIP)"+
                "VALUES (?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?,?)";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, customerName);
        ps.setString(2, address);
        ps.setString(3, postal);
        ps.setString(4, phone);
        ps.setString(5, getCurrentUserName(currentUser));
        ps.setInt(6, getCurrentUser());
        ps.setInt(7, divisionID);
        ps.setString(8, isVIP);
        ps.executeUpdate();
    }

    /**
     * Method to add appointment to database
     * @param title String appointment title
     * @param description String appt description
     * @param location String appt location
     * @param type String appt type
     * @param start Timestamp appt start time
     * @param end Timestamp appt end time
     * @param customerId int
     * @param userId int
     * @param contactId int
     * @throws SQLException
     */
    public static void addAppointment(String title, String description, String location, String type, Timestamp start, Timestamp end, int customerId, int userId) throws SQLException {
        String sql = "INSERT INTO APPOINTMENTS (Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?)";
        // convert start and end time to UTC
        start = Helper.toUTC(start);
        end = Helper.toUTC(end);
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, title);
        ps.setString(2, description);
        ps.setString(3, location);
        ps.setString(4, type);
        ps.setTimestamp(5, start);
        ps.setTimestamp(6, end);
        ps.setString(7, getCurrentUserName(currentUser));
        ps.setString(8, getCurrentUserName(currentUser));
        ps.setInt(9, customerId);
        ps.setInt(10, userId);
        ps.executeUpdate();
    }

    /**
     * Method to update existing appointment in database
     * @param appointmentId int
     * @param title String
     * @param description String
     * @param location String
     * @param type String
     * @param start Timestamp
     * @param end Timestamp
     * @param customerId int
     * @param userId int
     * @throws SQLException
     */
    public static void updateAppointment(int appointmentId, String title, String description, String location, String type, Timestamp start, Timestamp end, int customerId, int userId) throws SQLException {
        String sql = "UPDATE APPOINTMENTS SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Update = CURRENT_TIMESTAMP, Last_Updated_By = ?, Customer_ID = ?, User_ID = ? WHERE Appointment_ID = ?";
        // convert start and end time to UTC
        start = Helper.toUTC(start);
        end = Helper.toUTC(end);
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, title);
        ps.setString(2, description);
        ps.setString(3, location);
        ps.setString(4, type);
        ps.setTimestamp(5, start);
        ps.setTimestamp(6, end);
        ps.setString(7, getCurrentUserName(currentUser));
        ps.setInt(8, customerId);
        ps.setInt(9, userId);
        ps.setInt(10, appointmentId);
        ps.executeUpdate();
    }

    /**
     *
     * @param contactId int
     * @return String of contact name
     * @throws SQLException
     */
    public static String getContactName(int contactId) throws SQLException {
        String sql = "SELECT Contact_Name FROM CONTACTS WHERE Contact_ID = " + contactId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        String contactName = "";
        while(rs.next()) {
            contactName = rs.getString("Contact_Name");
        }
        return contactName;
    }

    /**
     *
     * @param customerId int
     * @return String customer name
     * @throws SQLException
     */
    public static String getCustomerName(int customerId) throws SQLException {
        String sql = "SELECT Customer_Name FROM CUSTOMERS WHERE Customer_ID = " + customerId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        String customerName = "";
        while(rs.next()) {
            customerName = rs.getString("Customer_Name");
        }
        return customerName;
    }

    /**
     *
     * @param userId int
     * @return String of user name
     * @throws SQLException
     */
    public static String getUserName(int userId) throws SQLException {
        String sql = "SELECT User_Name FROM USERS WHERE User_ID = " + userId;
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        String userName = "";
        while(rs.next()) {
            userName = rs.getString("User_Name");
        }
        return userName;
    }

    /**
     *
     * @param userName String
     * @return String user Id
     * @throws SQLException
     */
    public static int getUserId(String userName) throws SQLException {
        String sql = "SELECT User_ID FROM USERS WHERE User_Name = '" + userName + "'";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        int userId = 0;
        while(rs.next()) {
            userId = rs.getInt("User_ID");
        }
        return userId;
    }

    /**
     *
     * @param contactName String
     * @return int contact Id
     * @throws SQLException
     */
    public static int getContactId(String contactName) throws SQLException {
        String sql = "SELECT Contact_ID FROM CONTACTS WHERE Contact_Name = '" + contactName + "'";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        int contactId = 0;
        while(rs.next()) {
            contactId = rs.getInt("Contact_ID");
        }
        return contactId;
    }

    /** Method to generate appointments report for all contacts
     * @param id Contact ID
     * @return ObservableList with contact's appointment details */
//    public static ObservableList<String> contactAppointmentsById(String id) throws SQLException {
//        ObservableList<String> appointments = FXCollections.observableArrayList();
//        String sql = "SELECT * FROM APPOINTMENTS WHERE Contact_ID = ?";
//        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
//        ps.setString(1, id);
//        ResultSet results = ps.executeQuery();
//
//        while (results.next()) {
//            String appointmentId = results.getString("Appointment_ID");
//            String title = results.getString("Title");
//            String type = results.getString("Type");
//            String description = results.getString("Description");
//            Timestamp start = results.getTimestamp("Start");
//            Timestamp end = results.getTimestamp("End");
//            String customerId = results.getString("Customer_ID");
////            Timestamp localStart = Helper.toLocal(start);
////            Timestamp localEnd = Helper.toLocal(end);
//            Timestamp localStart = Timestamp.valueOf(start.toLocalDateTime());
//            Timestamp localEnd = Timestamp.valueOf(end.toLocalDateTime());
//
//            String line = "Appointment ID: " + appointmentId + "\n";
//            line += "Title: " + title + "\n";
//            line += "Type: " + type + "\n";
//            line += "Description: " + description + "\n";
//            line += "Start date/time: " + localStart + " " + TimeZone.getDefault().getDisplayName() + "\n";
//            line += "End date/time: " + localEnd + " " + TimeZone.getDefault().getDisplayName() + "\n";
//            line += "Customer ID: " + customerId + "\n\n";
//            appointments.add(line);
//        }
//        return appointments;
//    }

    /**
     *
     * @param customerName String
     * @return int customer Id
     * @throws SQLException
     */
    public static int getCustomerId(String customerName) throws SQLException {
        String sql = "SELECT Customer_ID FROM CUSTOMERS WHERE Customer_Name = '" + customerName + "'";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        int customerId = 0;
        while(rs.next()) {
            customerId = rs.getInt("Customer_ID");
        }
        return customerId;
    }

}
