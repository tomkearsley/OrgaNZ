package com.humanharvest.organz.controller.administrator;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.state.ClinicianManager;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.JSONConverter;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.PageNavigator;

public class StaffListController extends SubController {

    private ClinicianManager clinicianManager;
    @FXML
    private HBox menuBarPane;
    @FXML
    private TableView<Clinician> tableView;
    @FXML
    private TableColumn<Clinician, Integer> idCol;
    @FXML
    private TableColumn<Clinician, Clinician> firstNameCol;
    @FXML
    private TableColumn<Clinician, Clinician> lastNameCol;

    public StaffListController() {
        this.clinicianManager = State.getClinicianManager();
    }

    @Override
    public void setup(MainController mainController) {
        super.setup(mainController);
        mainController.setTitle("Staff list");
        mainController.loadMenuBar(menuBarPane);
    }

    @FXML
    private void initialize() {
        setupTable();
    }

    /**
     * Sets up each of the columns in the tables to be populated with data. Also handles the events of users either
     * double clicking or right clicking on a clinician.
     */
    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        tableView.setRowFactory(tableView -> {

            TableRow<Clinician> row = new TableRow<Clinician>() {
            };
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().setValue("Delete");
            deleteItem.setOnAction(event -> delete(getClinicianClickedOn().getStaffId()));
            ContextMenu contextMenu = new ContextMenu(deleteItem);
            row.setContextMenu(contextMenu);

            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                    loadUser(getClinicianClickedOn().getStaffId());
                }
            });

            return row;
        });
        refresh();
    }

    /**
     * Gets the clinician that has been clicked on.
     * @return The clinician object of the clinician who has been clicked on
     */
    private Clinician getClinicianClickedOn() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    /**
     * Refreshes the page including the staff member list.
     */
    @Override
    public void refresh() {
        super.refresh();
        tableView.setItems(FXCollections.observableArrayList(clinicianManager.getClinicians()));
    }

    /**
     * Loads the selected user's profile page in the current window.
     */
    private void loadUser(Integer staffId) {
        Optional<Clinician> clinician = clinicianManager.getClinicianByStaffId(staffId);
        State.setViewedClinician(clinician.get());
        PageNavigator.loadPage(Page.VIEW_CLINICIAN, mainController);
    }

    /**
     * Deletes the staff member clinician represented by "id".
     * @param id the staff member to delete
     */
    private void delete(int id) {
        String actionHistoryFilename = "action_history.json";

        if (id == 0) {
            PageNavigator.showAlert(Alert.AlertType.ERROR, "Cannot delete the default Clinician",
                    "The default clinician cannot be deleted from the system.");
            return;

        } else {
            Clinician clinician = clinicianManager.getClinicianByStaffId(id)
                    .orElseThrow(IllegalArgumentException::new);

            clinicianManager.removeClinician(clinician);

            HistoryItem deleteClinician = new HistoryItem("DELETE", "Clinician " + id + " deleted");
            JSONConverter.updateHistory(deleteClinician, actionHistoryFilename);
        }
        PageNavigator.refreshAllWindows();
    }

}
