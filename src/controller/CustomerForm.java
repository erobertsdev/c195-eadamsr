package controller;

import com.mysql.cj.xdevapi.Table;
import helper.JDBC;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.ResourceBundle;

import static helper.JDBC.getAppointmentsById;

public class CustomerForm extends Helper implements Initializable {
    @FXML private TableView<Customer> customersTableview;
    @FXML private TableColumn<Customer, Integer> customerIdCol;
    @FXML private TableColumn<Customer, String> customerNameCol;
    @FXML private TableColumn<Customer, String> customerAddressCol;
    @FXML private TableColumn<Customer, Integer> customerDivisionCol;
    @FXML private TableColumn<Customer, String> customerPostalCol;
    @FXML private TableColumn<Customer, String> customerPhoneCol;
    @FXML private Button editCustomerButton;
    @FXML private Button addCustomerButton;
    @FXML private Button deleteCustomerButton;
    @FXML private TableView<Appointment> appointmentsTableview;
    @FXML private TableColumn<Appointment, Integer> appointmentIdCol;
    @FXML private TableColumn<Appointment, String> appointmentTitleCol;
    @FXML private TableColumn<Appointment, String> appointmentDescriptionCol;
    @FXML private TableColumn<Appointment, String> appointmentLocationCol;
    @FXML private TableColumn<Appointment, String> appointmentTypeCol;
    @FXML private TableColumn<Appointment, Timestamp> appointmentStartCol;
    @FXML private TableColumn<Appointment, Timestamp> appointmentEndCol;
    public static Customer selectedCustomer = null;
    public static Appointment selectedAppointment = null;
    // Differentiates between adding/updating for the customer and appointment editing forms
    public static boolean addingCustomer;
    public static boolean addingAppointment;

    public static Customer getSelectedCustomer() {
        return selectedCustomer;
    }
    public static Appointment getSelectedAppointment() { return selectedAppointment; }

    /**
     * Method to check if customer has appointments before deleting
     * @return boolean true if customer has appointments
     */
    public boolean checkForAppointments(int customerId) throws SQLException {
        return JDBC.getAppointmentsById(String.valueOf(customerId)).size() != 0;
    }

    public void handleEditCustomer(ActionEvent event) throws IOException {
        try {
            // Throw error if no customer selected
            if (customersTableview.getSelectionModel().getSelectedItem() == null) {
                Helper.errorDialog("Please select a customer to edit.");
            } else {
            // Get selected customer info
                selectedCustomer = customersTableview.getSelectionModel().getSelectedItem();
                Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/editCustomerForm.fxml")));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e){
            e.printStackTrace();
            Helper.errorDialog("Problem editing customer. Please try again.");
        }
    }

    public void handleAddCustomer(ActionEvent event) throws IOException {
        // Inform EditCustomerForm that customer is being added not updated
        addingCustomer = true;
        // Open edit customer dialog
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/editCustomerForm.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void handleDeleteCustomer() throws SQLException {
        selectedCustomer = customersTableview.getSelectionModel().getSelectedItem();
        // Throw error if no customer selected
        if (customersTableview.getSelectionModel().getSelectedItem() == null) {
            Helper.errorDialog("Please select a customer to delete.");
        } else {
            if (checkForAppointments(selectedCustomer.getId())) {
                Helper.errorDialog("All of a customer's appointments must be deleted before the customer can be deleted.");
            } else {
                // Get selected customer info
                Customer selectedCustomer = customersTableview.getSelectionModel().getSelectedItem();
                // Delete customer from database
                try {
                    JDBC.deleteCustomer(selectedCustomer.getId());
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.errorDialog("Problem deleting customer. Please try again.");
                }
                // Refresh tableview
                Helper.errorDialog("Customer ID: " + selectedCustomer.getId() + " with the name " + selectedCustomer.getName() + " successfully deleted.");
                customersTableview.getItems().remove(selectedCustomer);
            }
        }
    }

    /** Method to edit selected appointment and open appointment form */
    public void handleEditAppointment(ActionEvent event) throws IOException {
        try {
            // Throw error if no appointment selected
            if (appointmentsTableview.getSelectionModel().getSelectedItem() == null) {
                Helper.errorDialog("Please select an appointment to edit.");
            } else {
                // Get selected appointment info
                selectedAppointment = appointmentsTableview.getSelectionModel().getSelectedItem();
                Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/AppointmentForm.fxml")));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e){
            e.printStackTrace();
            Helper.errorDialog("Problem editing appointment. Please try again.");
        }
    }

    /** Method to open appointmentForm to add appointment */
    public void handleAddAppointment(ActionEvent event) throws IOException {
        addingAppointment = true;
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/AppointmentForm.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void handleDeleteAppointment() {
        // Get selected appointment info
        selectedAppointment = appointmentsTableview.getSelectionModel().getSelectedItem();
        // Throw error if no appointment selected
        if (appointmentsTableview.getSelectionModel().getSelectedItem() == null) {
            Helper.errorDialog("Please select an appointment to delete.");
        } else {
            // Delete appointment from database
            try {
                JDBC.deleteAppointment(selectedAppointment.getId());
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.errorDialog("Problem deleting appointment. Please try again.");
            }
            // Refresh tableview
            // Throw error with deleted appointment ID and Type of appointment
            Helper.errorDialog("Appointment ID: " + selectedAppointment.getId() + " Type: " + selectedAppointment.getType() + " successfully deleted.");
            appointmentsTableview.getItems().remove(selectedAppointment);
        }
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addingAppointment = false;
        addingCustomer = false;
        // Fill Customers Table
        try {
            customersTableview.getItems().setAll(JDBC.getCustomers());
            customerIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            customerNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            customerAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
            customerDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
            customerPostalCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
            customerPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
            customersTableview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            customersTableview.setItems(JDBC.getCustomers());
            

            /** Event listener to detect when row in Customers tableview is selected and
             * populate the Appointments tableview with that customer's appointments */
            // ****** Lambda function *******
            customersTableview.setRowFactory(tv -> {
                TableRow<Customer> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    // check for non-empty rows
                    if (!row.isEmpty()) {
                        Customer element = row.getItem();
                        int col = element.getId();
                        try {
                            appointmentsTableview.getItems().setAll(getAppointmentsById(String.valueOf(col)));
                            appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
                            appointmentTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
                            appointmentDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
                            appointmentLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
                            appointmentTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
                            appointmentStartCol.setCellValueFactory(new PropertyValueFactory<>("start"));
                            appointmentEndCol.setCellValueFactory(new PropertyValueFactory<>("end"));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return row;
            });

        } catch (SQLException e) {
            Helper.errorDialog("Problem retrieving customers. Please try again.");
        }
    }
}
