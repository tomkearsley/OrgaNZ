package com.humanharvest.organz.utilities.view;

/**
 * An enum to represent pages in the GUI application.
 * Includes a link to the FXML file for each page.
 */
public enum Page {
    MAIN("/fxml/main.fxml"),
    LANDING("/fxml/landing.fxml"),
    LOGIN_CLIENT("/fxml/login_client.fxml"),
    CREATE_CLIENT("/fxml/create_client.fxml"),
    MENU_BAR("/fxml/menu_bar.fxml"),
    SIDEBAR("/fxml/sidebar.fxml"),
    VIEW_CLIENT("/fxml/view_client.fxml"),
    VIEW_CLINICIAN("/fxml/view_clinician.fxml"),
    REGISTER_ORGAN_DONATIONS("/fxml/register_organ_donation.fxml"),
    REQUEST_ORGANS("/fxml/request_organs.fxml"),
    HISTORY("/fxml/history.fxml"),
    SEARCH("/fxml/search_clients.fxml"),
    TRANSPLANTS("/fxml/transplants.fxml"),
    ORGANS_TO_DONATE("/fxml/organs_to_donate.fxml"),
    LOGIN_STAFF("/fxml/login_staff.fxml"),
    CREATE_CLINICIAN("/fxml/create_clinician.fxml"),
    CREATE_ADMINISTRATOR("/fxml/create_administrator.fxml"),
    VIEW_MEDICATIONS("/fxml/view_medications.fxml"),
    VIEW_MEDICAL_HISTORY("/fxml/view_medical_history.fxml"),
    VIEW_PROCEDURES("/fxml/view_procedures.fxml"),
    STAFF_LIST("/fxml/staff_list.fxml"),
    COMMAND_LINE("/fxml/command_line.fxml"),
    ADMIN_CONFIG("/fxml/admin_config.fxml"),
    SUBMIT_DEATH_DETAILS("/fxml/submit_death_details.fxml");

    private String path;

    Page(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
