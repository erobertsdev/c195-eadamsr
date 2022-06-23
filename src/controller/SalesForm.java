package controller;

import helper.JDBC;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Customer;
import model.Sale;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class SalesForm extends Helper implements Initializable {
    @FXML private ComboBox customerCombo;
    @FXML private ComboBox productCombo;
    @FXML private TableView<Sale> salesTableView;
    @FXML private TableColumn<Sale, Integer> productIDCol;
    @FXML private TableColumn<Sale, String> productNameCol;
    @FXML private TableColumn<Sale, Double> productPriceCol;
    @FXML private TableColumn<Sale, String> soldByCol;
    @FXML private TableColumn<Sale, String> saleDateCol;
    @FXML private TableColumn<Sale, Integer> saleIdCol;
    @FXML private Button refundButton;
    @FXML private Label totalCostLabel;

    /** Method to add a sale to the database when the sell button is clicked */
    // Check that customer and product are selected before adding sale
    public void handleSellButton(ActionEvent event) throws SQLException {
        String productString = (String) productCombo.getSelectionModel().getSelectedItem();
        String customerString = (String) customerCombo.getSelectionModel().getSelectedItem();
        if (customerCombo.getValue() != null && productCombo.getValue() != null) {
            // Get current user's ID
            int userID = JDBC.getUserId(LoginForm.currentUser);
            try {
                // Add sale to database
                JDBC.addSale(JDBC.getProductPrice(productString), JDBC.getCustomerId(customerString), userID, JDBC.getProductId(productString), productString);
                Helper.noticeDialog("Sale added successfully!");
                // Refresh salesTableView
                int customerId = JDBC.getCustomerId((String) customerCombo.getSelectionModel().getSelectedItem());
                ObservableList<Sale> sales = JDBC.getSalesByCustomerId(customerId);
                populateSalesTableView(sales);
            } catch (SQLException e) {
                Helper.errorDialog("Error adding sale!");
            }
        }
    }

    /** Get selected customer's sales and display them in the table */
    // TODO: Fix getSalesByCustomer method to return ObservableList<Sale>
    public void handleSelectCustomer(ActionEvent event) throws SQLException {
        int customerId = JDBC.getCustomerId((String) customerCombo.getSelectionModel().getSelectedItem());
        ObservableList<Sale> sales = JDBC.getSalesByCustomerId(customerId);
        populateSalesTableView(sales);
    }

    // Return to customerForm when cancel button is clicked
    public void handleCancelButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../view/customerForm.fxml")));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    // Retrieve price of selected product and set total cost label
    public void handleSelectProduct(ActionEvent event) throws SQLException {
        String productString = (String) productCombo.getSelectionModel().getSelectedItem();
        double productPrice = JDBC.getProductPrice(productString);
        totalCostLabel.setText("$" + productPrice);
    }

    // Handle refund
    public void handleRefundButton(ActionEvent event) throws IOException, SQLException {
        // Get selected sale
        Sale selectedSale = salesTableView.getSelectionModel().getSelectedItem();
        if (selectedSale != null) {
            // Get selected sale's ID
            int saleId = selectedSale.getSaleID();
            try {
                JDBC.removeSale(saleId);
                Helper.noticeDialog("Sale refunded successfully!");
                // Refresh salesTableView
                int customerId = JDBC.getCustomerId((String) customerCombo.getSelectionModel().getSelectedItem());
                ObservableList<Sale> sales = JDBC.getSalesByCustomerId(customerId);
                populateSalesTableView(sales);
            } catch (SQLException e) {
                Helper.errorDialog("Error removing sale! Please try again.");
            }
        } else {
            Helper.errorDialog("Please select a sale to refund!");
        }
    }

    // Method to populate sales tableview with observable list of sales
    public void populateSalesTableView(ObservableList<Sale> sales) {
        productIDCol.setCellValueFactory(new PropertyValueFactory<>("productID"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        soldByCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        saleDateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        saleIdCol.setCellValueFactory(new PropertyValueFactory<>("saleID"));
        salesTableView.setItems(sales);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Refund button only visible if user is admin
        if (!LoginForm.currentUser.equals("admin")) {
            refundButton.setVisible(false);
        }
        ObservableList<String> customerNames = null;
        ObservableList<String> productNames = null;
        try {
            customerNames = JDBC.getCustomerNames();
            productNames = JDBC.getProductNames();
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.errorDialog("Error retrieving customers/products from database. Please try again.");
        }
        customerCombo.setItems(customerNames);
        productCombo.setItems(productNames);
    }
}