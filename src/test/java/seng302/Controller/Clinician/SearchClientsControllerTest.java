package seng302.Controller.Clinician;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;
import static org.testfx.matcher.control.TextMatchers.hasText;

import java.time.LocalDate;

import seng302.Client;
import seng302.Clinician;
import seng302.Controller.ControllerTest;
import seng302.State.State;
import seng302.TransplantRequest;
import seng302.Utilities.Enums.Organ;
import seng302.Utilities.Enums.Region;
import seng302.Utilities.View.Page;
import seng302.Utilities.View.WindowContext;

import org.junit.Test;

public class SearchClientsControllerTest extends ControllerTest {


    // Test data

    private Clinician testClinician = new Clinician("A", "B", "C", "D",
            Region.UNSPECIFIED, 0, "E");

    private Client client1 = new Client("Client", "Number", "One", LocalDate.now(), 1);
    private Client client2 = new Client("Client", "Number", "Two", LocalDate.now(), 2);
    private Client client3 = new Client("Client", "Number", "Three", LocalDate.now(), 3);

    private Client[] clients = {client1, client2, client3};

    private String tick = "\u2713";


    // Overridden classes from parent class

    @Override
    protected Page getPage() {
        return Page.SEARCH;
    }

    @Override
    protected void initState() {
        State.init();
        State.login(testClinician);

        for (Client client : clients) {
            State.getClientManager().addClient(client);
        }

        client1.setRegion(Region.CANTERBURY);
        client2.setRegion(Region.AUCKLAND);
        // client3's region is left as null

        for (int i = 100; i < 218; i++) {
            Client client = new Client("Client", "Number", Integer.toString(i), LocalDate.now(), i);
            TransplantRequest request = new TransplantRequest(client, Organ.MIDDLE_EAR);
            client.addTransplantRequest(request);
            client.setRegion(Region.NELSON);
            State.getClientManager().addClient(client);
        }

        mainController.setWindowContext(new WindowContext.WindowContextBuilder()
                .build());
    }

    // Tests

    @Test
    public void testComponentsAreVisible() {
        verifyThat("#tableView", isVisible());
        verifyThat("#displayingXToYOfZText", isVisible());
        verifyThat("#sidebarPane", isVisible());
        verifyThat("#pagination", isVisible());
    }

    @Test
    public void testPaginationDescription() {
        verifyThat("#tableView", hasNumRows(30));
        verifyThat("#displayingXToYOfZText", hasText("Displaying 1-30 of 121"));
    }

    @Test
    public void testClientIsReceiver() {

    }

    @Test
    public void testClientIsDonor() {

    }

    @Test
    public void testClientNotDonorOrReceiver() {

    }
}
