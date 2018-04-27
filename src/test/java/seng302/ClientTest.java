package seng302;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.Period;

import seng302.Utilities.Enums.Organ;
import seng302.Utilities.Exceptions.OrganAlreadyRegisteredException;

import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    private Client client;

    @Before
    public void createClient() {
        client = new Client();
    }

    @Test
    public void getBMI1Test() {
        client.setWeight(70);
        client.setHeight(180);
        assertEquals(client.getBMI(), 21.6, 0.01);
    }

    @Test
    public void getBMI2Test() {
        client.setWeight(0);
        client.setHeight(180);
        assertEquals(client.getBMI(), 0, 0.0);
    }

    @Test
    public void getAgeTest() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        int age = Period.between(dob, LocalDate.now()).getYears();
        client.setDateOfBirth(dob);
        assertEquals(client.getAge(), age);

    }

    @Test
    public void getAge2Test() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        LocalDate dod = LocalDate.of(2010, 1, 1);
        client.setDateOfBirth(dob);
        client.setDateOfDeath(dod);
        assertEquals(10, client.getAge());
    }

    @Test
    public void CheckNameContainsValidTest() {
        client = new Client("First", null, "Last", LocalDate.of(1970, 1, 1), 1);
        assertTrue(client.nameContains("First"));
    }

    @Test
    public void CheckNameContainsCaseInsensitivityValidTest() {
        client = new Client("First", null, "Last", LocalDate.of(1970, 1, 1), 1);
        assertTrue(client.nameContains("first"));
    }

    @Test
    public void CheckNameContainsLastNameValidTest() {
        client = new Client("First", null, "Last", LocalDate.of(1970, 1, 1), 1);
        assertTrue(client.nameContains("La"));
    }

    @Test
    public void CheckNameContainsMiddleNameValidTest() {
        client = new Client("First", "middlename", "Last", LocalDate.of(1970, 1, 1), 1);
        assertTrue(client.nameContains("mid"));
    }

    @Test
    public void CheckNameContainsNotValidTest() {
        client = new Client("First", "middlename", "Last", LocalDate.of(1970, 1, 1), 1);
        assertFalse(client.nameContains("notin"));
    }

    @Test
    public void CheckNameContainsMultipleChecksValidTest() {
        client = new Client("First", "middlename", "Last", LocalDate.of(1970, 1, 1), 1);
        assertTrue(client.nameContains("F Last"));
    }

    @Test
    public void CheckNameContainsMultipleChecksOneInvalidTest() {
        client = new Client("First", "middlename", "Last", LocalDate.of(1970, 1, 1), 1);
        assertFalse(client.nameContains("F mid not"));
    }

    @Test
    public void GetFullNameNoMiddleNameTest() {
        client = new Client("First", null, "Last", LocalDate.of(1970, 1, 1), 1);
        assertEquals("First Last", client.getFullName());
    }

    @Test
    public void GetFullNameWithMiddleNameTest() {
        client = new Client("First", "Mid Name", "Last", LocalDate.of(1970, 1, 1), 1);
        assertEquals("First Mid Name Last", client.getFullName());
    }

    @Test
    public void noRequestCurrentOrganRequest1Test() {
        assertEquals(false, client.currentOrganRequest());
    }

    @Test
    public void noRequestCurrentOrganRequest2Test() {
        Organ o = Organ.HEART;
        TransplantRequest t = new TransplantRequest(o, false);
        client.addTransplantRequest(t);
        assertEquals(false, client.currentOrganRequest());
    }

    @Test
    public void validCurrentOrganRequestTest() {
        Organ o = Organ.HEART;
        TransplantRequest t = new TransplantRequest(o, true);
        client.addTransplantRequest(t);
        assertEquals(true, client.currentOrganRequest());
    }

    @Test
    public void getOrganStatusStringEmpty1Test() {
        assertEquals("No organs found", client.getOrganStatusString("requests"));
    }

    @Test
    public void getOrganStatusStringEmpty2Test() {
        assertEquals("No organs found", client.getOrganStatusString("donations"));
    }

    @Test
    public void getOrganStatusStringInvalidStringTest() {
        assertEquals("Invalid input", client.getOrganStatusString(""));
    }

    @Test
    public void getOrganStatusStringValid1Test() {
        Organ o = Organ.HEART;
        try {
            client.setOrganRequestStatus(o, true);
        } catch (OrganAlreadyRegisteredException ex) {
            System.out.println(ex);
        }
        assertEquals("Heart", client.getOrganStatusString("requests"));
    }

    @Test
    public void getOrganStatusStringValid2Test() {
        Organ o = Organ.HEART;
        try {
            client.setOrganDonationStatus(o, true);
        } catch (OrganAlreadyRegisteredException ex) {
            System.out.println(ex);
        }
        assertEquals("Heart", client.getOrganStatusString("donations"));
    }

    @Test
    public void getOrganStatusStringValid3Test() {
        try {
            client.setOrganRequestStatus(Organ.BONE, true);
            client.setOrganRequestStatus(Organ.HEART, true);
            client.setOrganRequestStatus(Organ.LIVER, true);
        } catch (OrganAlreadyRegisteredException ex) {
            fail(ex.getMessage());
        }
        String organStatusString = client.getOrganStatusString("requests");
        assertTrue(organStatusString.contains("Bone"));
        assertTrue(organStatusString.contains("Heart"));
        assertTrue(organStatusString.contains("Liver"));
    }

    @Test(expected = OrganAlreadyRegisteredException.class)
    public void getOrganStatusStringInvalidTest() throws OrganAlreadyRegisteredException {
        client.setOrganRequestStatus(Organ.BONE, true);
        client.setOrganRequestStatus(Organ.BONE, true);
    }
}
