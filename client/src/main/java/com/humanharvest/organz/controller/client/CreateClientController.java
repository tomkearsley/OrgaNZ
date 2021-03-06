package com.humanharvest.organz.controller.client;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.state.Session.UserType;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.ui.validation.UIValidation;
import com.humanharvest.organz.utilities.JSONConverter;
import com.humanharvest.organz.utilities.exceptions.ServerRestException;
import com.humanharvest.organz.utilities.validators.NotNullValidator;
import com.humanharvest.organz.utilities.validators.StringValidator;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.PageNavigator;
import com.humanharvest.organz.utilities.view.WindowContext;
import com.humanharvest.organz.views.client.CreateClientView;

/**
 * Controller for the create client page.
 */
public class CreateClientController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(CreateClientController.class.getName());

    @FXML
    private DatePicker dobFld;
    @FXML
    private TextField firstNameFld, middleNamefld, lastNamefld;
    @FXML
    private Button createButton, goBackButton;
    @FXML
    private Pane menuBarPane;

    private final ClientManager manager;
    private UIValidation validation;

    /**
     * Initializes the UI for this page.
     * - Gets the ClientManager and ActionInvoker from the current state.
     */
    public CreateClientController() {
        manager = State.getClientManager();
    }

    /**
     * Override so we can set the page title.
     * @param mainController The MainController
     */
    @Override
    public void setup(MainController mainController) {
        super.setup(mainController);
        mainController.setTitle("Create a new Client");

        if (State.getSession() != null) { //they're a clinician or admin
            mainController.loadMenuBar(menuBarPane);
            goBackButton.setVisible(false);
        }

        validation = new UIValidation()
                .add(firstNameFld, new StringValidator())
                .add(lastNamefld, new StringValidator())
                .add(dobFld, new NotNullValidator())
                .validate();
    }

    /**
     * Creates a new client based on the information supplied in the fields.
     * Shows appropriate alerts if the information is invalid, or if the client already exists.
     * Shows an alert if successful, then redirects to the view page for the new client.
     */
    @FXML
    private void createClient() {
        if (validation.isInvalid()) {
            PageNavigator.showAlert(AlertType.ERROR, "Required Field Empty",
                    "Please make sure that all the required fields are given.");
        } else {
            //Duplicate user warning alert
            if (manager.doesClientExist(firstNameFld.getText(), lastNamefld.getText(), dobFld.getValue())) {
                ButtonType option = PageNavigator.showAlert(AlertType.CONFIRMATION,
                        "Duplicate Client Warning",
                        "This client is a duplicate of one that already exists. Would you still like to create it?")
                        .orElse(ButtonType.CANCEL);
                if (option != ButtonType.OK) {
                    // ... user chose CANCEL or closed the dialog
                    return;
                }
            }

            CreateClientView newClient = new CreateClientView(firstNameFld.getText(), middleNamefld.getText(),
                    lastNamefld.getText(),
                    dobFld.getValue());

            Client client;
            try {
                client = State.getClientResolver().createClient(newClient);
            } catch (ServerRestException e) {
                LOGGER.severe(e.getMessage());
                PageNavigator.showAlert(AlertType.ERROR,
                        "Server Error",
                        "An error occurred while trying to fetch from the server.\nPlease try again later.");
                return;
            }

            HistoryItem save = new HistoryItem("CREATE CLIENT",
                    String.format("Client %s was created with ID %d", client.getFullName(), client.getUid()));
            JSONConverter.updateHistory(save, "action_history.json");

            if (State.getSession() == null) { // Someone creating a client
                try {
                    State.getAuthenticationManager().loginClient(client.getUid());
                } catch (ServerRestException e) {
                    LOGGER.severe(e.getMessage());
                    PageNavigator.showAlert(AlertType.ERROR,
                            "Server Error",
                            "An error occurred while trying to fetch from the server.\nPlease try again later.");
                    return;
                }
                PageNavigator.loadPage(Page.VIEW_CLIENT, mainController);

            } else { // Clinician or admin are creating a user.

                MainController newMain = PageNavigator.openNewWindow();
                if (newMain != null) {
                    newMain.setWindowContext(new WindowContext.WindowContextBuilder()
                            .setAsClinicianViewClientWindow()
                            .viewClient(client)
                            .build());
                    PageNavigator.loadPage(Page.VIEW_CLIENT, newMain);
                }
            }
        }
    }

    /**
     * Redirects the UI back to the previous page.
     */
    @FXML
    private void goBack() {
        if (State.getSession() == null) { // Someone creating a user
            PageNavigator.loadPage(Page.LANDING, mainController);

        } else if (State.getSession().getLoggedInUserType() == UserType.CLINICIAN) {
            PageNavigator.loadPage(Page.VIEW_CLINICIAN, mainController);
        }
    }
}
