package controller;

import helper.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Country;
import model.Customer;
import model.Division;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class EditCustomerForm extends Helper implements Initializable {
    @FXML private TextField customerIdTextField;
    @FXML private TextField customerNameTextField;
    @FXML private TextField customerPhoneTextField;
    @FXML private TextField customerStreetTextField;
    @FXML private TextField customerPostalTextField;
    @FXML private ComboBox<String> countryCombo;
    @FXML private ComboBox<String> stateCombo;
    public ObservableList<String> countryNames;
    private final Customer selectedCustomer = CustomerForm.getSelectedCustomer();

    /**
     * Sets Countries in Country combobox and retrieves and sets the 1st level data in the state combobox */
    public void handleSelectCountry(ActionEvent event) throws IOException, SQLException {
        int countryId = 0;
        String countryString = countryCombo.getSelectionModel().getSelectedItem();
        switch (countryString) {
            case "U.S" -> countryId = 1;
            case "UK" -> countryId = 2;
            case "Canada" -> countryId = 3;
        }
        countryNames = JDBC.getDivisionsById(String.valueOf(countryId)); // Retrieve all states in selected country
        stateCombo.setItems(countryNames); // Add appropriate "states" according to selected countries
    }

    public void handleSaveButton() {

    }

    public void handleCancelButton(ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/CustomerForm.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: Populate country/state with selected user's data
        if (selectedCustomer != null) {
            ObservableList<String> countries = FXCollections.observableArrayList("U.S", "UK", "Canada");
            countryCombo.setItems(countries);
            customerIdTextField.setText(Integer.toString(selectedCustomer.getId()));
            customerNameTextField.setText(selectedCustomer.getName());
            customerPhoneTextField.setText(selectedCustomer.getPhoneNumber());
            customerStreetTextField.setText(selectedCustomer.getAddress());
            customerPostalTextField.setText(selectedCustomer.getPostalCode());
        }

    }
}