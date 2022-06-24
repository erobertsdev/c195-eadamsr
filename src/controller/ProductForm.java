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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Product;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProductForm extends Helper implements Initializable {

    @FXML
    private Button deleteButton;

    @FXML
    private Button exitButton;

    @FXML
    private TableColumn<Product, Integer> productIDCol;

    @FXML
    private TextField productIDTextField;

    @FXML
    private TableColumn<Product, String> productNameCol;

    @FXML
    private TextField productNameTextField;

    @FXML
    private TableColumn<Product, Double> productPriceCol;

    @FXML
    private TextField productPriceTextField;

    @FXML
    private Button saveButton;

    @FXML
    private TableView<Product> productsTableView;

    /** Method to populate the table with all products */
    public void populateProductsTableView() throws SQLException {
        ObservableList<Product> products = JDBC.getProducts();
        productIDCol.setCellValueFactory(new PropertyValueFactory<>("productID"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        productsTableView.setItems(products);
    }

    /** Populate text fields when product is selected */
    public void populateProductTextFields() {
        Product product = productsTableView.getSelectionModel().getSelectedItem();
        if (product != null) {
            productIDTextField.setText(Integer.toString(product.getProductID()));
            productNameTextField.setText(product.getProductName());
            productPriceTextField.setText(Double.toString(product.getProductPrice()));
        }
    }

    public void handleExitButton(ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/CustomerForm.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    // TODO: edit button, delete button, clear selection button
    public void handleSaveButton(ActionEvent event) throws SQLException {
        if (productIDTextField.getText().trim().isEmpty() && productNameTextField.getText().trim().isEmpty() && productPriceTextField.getText().trim().isEmpty()) {
            Helper.errorDialog("Name and price fields are required.");
        } else if (productIDTextField.getText().matches("[0-9]+") && productNameTextField.getText().matches("[a-zA-Z]+") && !productPriceTextField.getText().matches("\\d+")) {
            // Update product
            int productID = Integer.parseInt(productIDTextField.getText());
            String productName = productNameTextField.getText();
            double productPrice = Double.parseDouble(productPriceTextField.getText());
            JDBC.updateProduct(productID, productName, productPrice);
            Helper.errorDialog("Product updated!");
            populateProductsTableView();
        } else if (productIDTextField.getText().trim().isEmpty() && productNameTextField.getText().matches("[a-zA-Z]+") && !productPriceTextField.getText().matches("\\d+")) {
            // Add product to database
            String productName = productNameTextField.getText();
            double productPrice = Double.parseDouble(productPriceTextField.getText());
            JDBC.addProduct(productName, productPrice);
            Helper.errorDialog("Product added successfully!");
            populateProductsTableView();
        } else {
            Helper.errorDialog("Invalid input. Please check all fields.");
        }
    }

    public void handleDeleteButton(ActionEvent event) throws SQLException {
        // Check if product is in sales table in which case it cannot be deleted
        if (JDBC.productExists(Integer.parseInt(productIDTextField.getText()))) {
            Helper.errorDialog("Product is currently being rented and cannot be deleted.");
        } else {
            // Delete product
            Product product = productsTableView.getSelectionModel().getSelectedItem();
            if (product != null) {
                JDBC.deleteProduct(product.getProductID());
                Helper.errorDialog("Product deleted!");
                populateProductsTableView();
            } else {
                Helper.errorDialog("No product selected.");
            }
        }
    }

    public void handleClearSelectionButton(ActionEvent event) {
        // Clear text fields
        productIDTextField.clear();
        productNameTextField.clear();
        productPriceTextField.clear();
        // Clear productsTableView selection
        productsTableView.getSelectionModel().clearSelection();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populate productsTableView
        try {
            populateProductsTableView();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
