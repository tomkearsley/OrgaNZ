package seng302.Controller;

import static org.junit.Assert.*;

import seng302.Administrator;
import seng302.State.State;
import seng302.Utilities.View.Page;
import seng302.Utilities.View.WindowContext.WindowContextBuilder;

import org.junit.Test;

public class MenuBarControllerAdminTest extends ControllerTest {

    private Administrator testAdmin = new Administrator("username", "password");

    @Test
    public void initState() {
        State.reset(false);
        State.login(testAdmin);
        State.getAdministratorManager().addAdministrator(testAdmin);
        mainController.setWindowContext(new WindowContextBuilder().build());
    }

    @Override
    protected Page getPage() {
        return Page.MENU_BAR;
    }

    @Test
    public void refresh() {
    }

    @Test
    public void testClickLogout() {
        clickOn("#filePrimaryItem");
        clickOn("#logOutItem");
        assertEquals(Page.LANDING, mainController.getCurrentPage());
    }

    @Test
    public void testClickSearchClients() {
        clickOn("#clientPrimaryItem");
        clickOn("#searchClientItem");
        assertEquals(Page.SEARCH, mainController.getCurrentPage());
    }

    @Test
    public void testClickCreateClient() {
        clickOn("#clientPrimaryItem");
        clickOn("#createClientItem");
        assertEquals(Page.CREATE_CLIENT, mainController.getCurrentPage());
    }

    @Test
    public void testClickSearchStaff() {
        clickOn("#staffPrimaryItem");
        clickOn("#searchStaffItem");
        assertEquals(Page.STAFF_LIST, mainController.getCurrentPage());
    }

    @Test
    public void testClickCreateAdministrator() {
        clickOn("#staffPrimaryItem");
        clickOn("#createAdministratorItem");
        assertEquals(Page.CREATE_ADMINISTRATOR, mainController.getCurrentPage());
    }

    @Test
    public void testClickCreateClinician() {
        clickOn("#staffPrimaryItem");
        clickOn("#createClinicianItem");
        assertEquals(Page.CREATE_CLINICIAN, mainController.getCurrentPage());
    }

    @Test
    public void testClickTransplantRequests() {
        clickOn("#transplantsPrimaryItem");
        clickOn("#searchTransplantsItem");
        assertEquals(Page.TRANSPLANTS, mainController.getCurrentPage());
    }

    @Test
    public void testClickHistory() {
        clickOn("#profilePrimaryItem");
        clickOn("#historyItem");
        assertEquals(Page.HISTORY, mainController.getCurrentPage());
    }

    @Test
    public void testClickCLI() {
        clickOn("#profilePrimaryItem");
        clickOn("#cliItem");
        assertEquals(Page.COMMAND_LINE, mainController.getCurrentPage());
    }
}