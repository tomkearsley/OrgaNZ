package com.humanharvest.organz.controller;

import static org.junit.Assert.assertEquals;

import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.WindowContext;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing that the landing page allows for navigation to the correct pages.
 */
public class LandingControllerTest extends ControllerTest {

    @Override
    protected Page getPage() {
        return Page.LANDING;
    }

    @Override
    protected void initState() {
        State.reset();
        mainController.setWindowContext(WindowContext.defaultContext());
    }

    @Test@Ignore
    public void goToCreateClientTest() {
        clickOn("#createClientButton");
        assertEquals(Page.CREATE_CLIENT, mainController.getCurrentPage());
    }

    @Test@Ignore
    public void goToClientLoginTest() {
        clickOn("#loginClientButton");
        assertEquals(Page.LOGIN_CLIENT, mainController.getCurrentPage());
    }

    @Test@Ignore
    public void goToCreateClinicianTest() {
        clickOn("#createClinicianButton");
        assertEquals(Page.CREATE_CLINICIAN, mainController.getCurrentPage());
    }

    @Test@Ignore
    public void goToClinicianLoginTest() {
        clickOn("#loginClinicianButton");
        assertEquals(Page.LOGIN_STAFF, mainController.getCurrentPage());
    }
}