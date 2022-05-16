package controller;

import helper.JDBC;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.Appointment;
import model.Customer;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CustomerForm<Item> extends Helper implements Initializable {
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
    @FXML private TableView<Appointment> appointmentsTableView;

    public void handleEditCustomer() {

    }

    public void handleAddCustomer() {

    }

    public void handleDeleteCustomer() {

    }

    public void handleEditAppt() {

    }

    public void handleAddAppt() {

    }

    public void handleDeleteAppt() {

    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
//            customersTableview.setOnMouseClicked((
//                   MouseEvent event) -> {
//               if(event.getButton().equals(MouseButton.PRIMARY)){
//                   System.out.println(customersTableview.getSelectionModel().getSelectedItem());
//                   System.out.println(getCellValue());
//               }
//           });

            // Technically a Lambda function TODO: GET IT TO WORK
            customersTableview.setRowFactory(tv -> {
                TableRow<Customer> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    // check for non-empty rows
                    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                        Customer element = row.getItem();
                        int col = element.getId();
                        try {
                            JDBC.getAppointmentsById(String.valueOf(col));
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
