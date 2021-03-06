package com.humanharvest.organz.controller.clinician;

import static org.junit.Assert.assertEquals;

import com.humanharvest.organz.Administrator;
import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.controller.ControllerTest;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.enums.Country;
import com.humanharvest.organz.utilities.enums.Region;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.WindowContext;
import org.junit.Test;

public class StaffLoginControllerTest extends ControllerTest {
    private Clinician testClinician = new Clinician("Mr", null, "Tester", "9 Fake St", Region.AUCKLAND.name(), Country.NZ, 3,
            "k");

    private Administrator testAdministrator;
    private String adminUsername = "test";
    private String adminPassword = "test";

    @Override
    protected Page getPage() {
        return Page.LOGIN_STAFF;
    }

    @Override
    protected void initState() {
        State.reset();
        testAdministrator = new Administrator(adminUsername, adminPassword);
        State.getAdministratorManager().addAdministrator(testAdministrator);
        State.getClinicianManager().addClinician(testClinician);
        mainController.setWindowContext(WindowContext.defaultContext());
    }

    @Test
    public void loginDefaultClinician() {
        clickOn("#staffId").write("0");
        clickOn("#password").write("admin");
        clickOn("Log in");
        assertEquals(Page.VIEW_CLINICIAN, mainController.getCurrentPage());
    }

    @Test
    public void loginTestClinician() {
        clickOn("#staffId").write("3");
        clickOn("#password").write("k");
        clickOn("Log in");
        assertEquals(Page.VIEW_CLINICIAN, mainController.getCurrentPage());
    }

    @Test
    public void nonExistingId() {
        clickOn("#staffId").write("9");
        clickOn("#password").write("k");
        clickOn("Log in");
        clickOn("OK");
        assertEquals(Page.LOGIN_STAFF, mainController.getCurrentPage());
    }

    @Test
    public void incorrectPassword() {
        clickOn("#staffId").write("0");
        clickOn("#password").write("k");
        clickOn("Log in");
        clickOn("OK");
        assertEquals(Page.LOGIN_STAFF, mainController.getCurrentPage());
    }

    @Test
    public void invalidStaffIdInput() {
        clickOn("#staffId").write("a");
        clickOn("#password").write("k");
        clickOn("Log in");
        clickOn("OK");
        assertEquals(Page.LOGIN_STAFF, mainController.getCurrentPage());
    }

    @Test
    public void goBackButtonTest() {
        clickOn("Back");
        assertEquals(Page.LANDING, mainController.getCurrentPage());
    }

    @Test
    public void testLoginAdminValid() {
        clickOn("#staffId").write(adminUsername);
        clickOn("#password").write(adminPassword);
        clickOn("Log in");
        assertEquals(Page.SEARCH, mainController.getCurrentPage());
    }

    @Test
    public void testLoginAdminDefault() {
        clickOn("#staffId").write("admin");
        clickOn("#password");
        clickOn("Log in");
        assertEquals(Page.SEARCH, mainController.getCurrentPage());
    }
}