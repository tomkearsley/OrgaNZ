package com.humanharvest.organz.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import com.humanharvest.organz.BaseTest;
import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.utilities.enums.Region;
import org.junit.Before;
import org.junit.Test;

public class ClinicianManagerTest extends BaseTest {

    private ClinicianManager manager;

    @Before
    public void init() {
        manager = new ClinicianManagerMemory();
    }

    @Test
    public void addClinicianTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.toString(), null, 1, "pass");
        manager.addClinician(clinician);
        assertTrue(manager.getClinicians().contains(clinician));
    }


    @Test
    public void getCliniciansTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.toString(), null, 1, "pass");
        Clinician clinician2 = new Clinician("First2", null, "Last2", "Address", Region.UNSPECIFIED.toString(), null, 2,
                "pass");
        ArrayList<Clinician> clinicians = new ArrayList<>();
        clinicians.add(clinician);
        clinicians.add(clinician2);
        manager = new ClinicianManagerMemory(clinicians);

        assertTrue(manager.getClinicians().contains(clinician));
        assertTrue(manager.getClinicians().contains(clinician2));
    }

    @Test
    public void removeClinicianTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.name(), null, 1, "pass");
        Clinician clinician2 = new Clinician("First2", null, "Last2", "Address", Region.UNSPECIFIED.name(), null, 2, "pass");
        ArrayList<Clinician> clinicians = new ArrayList<>();
        clinicians.add(clinician);
        clinicians.add(clinician2);
        manager = new ClinicianManagerMemory(clinicians);

        manager.removeClinician(clinician2);

        assertTrue(manager.getClinicians().contains(clinician));
        assertFalse(manager.getClinicians().contains(clinician2));
    }

    @Test
    public void updateClinicianTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.name(), null, 1, "pass");
        Clinician clinician2 = new Clinician("First2", null, "Last2", "Address", Region.UNSPECIFIED.name(), null, 2,
                "pass");
        ArrayList<Clinician> clinicians = new ArrayList<>();
        clinicians.add(clinician);
        clinicians.add(clinician2);
        manager = new ClinicianManagerMemory(clinicians);

        clinician.setFirstName("New");

        assertTrue(manager.getClinicians().contains(clinician));
        assertEquals("New", manager.getClinicianByStaffId(1).orElseThrow(IllegalStateException::new).getFirstName());
    }


    @Test
    public void collisionExsistsNoCollisionTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.name(), null, 1,
                "pass");
        ArrayList<Clinician> clinicians = new ArrayList<>();
        clinicians.add(clinician);
        manager = new ClinicianManagerMemory(clinicians);

        assertFalse(manager.doesStaffIdExist(2));
    }

    @Test
    public void collisionExsistsTrueTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.name(), null, 1, "pass");
        ArrayList<Clinician> clinicians = new ArrayList<>();
        clinicians.add(clinician);
        manager = new ClinicianManagerMemory(clinicians);

        assertTrue(manager.doesStaffIdExist(1));
    }

    @Test
    public void collisionExsistsTrueMultipleTest() {
        Clinician clinician = new Clinician("First", null, "Last", "Address", Region.UNSPECIFIED.name(), null, 1, "pass");
        Clinician clinician2 = new Clinician("First2", null, "Last2", "Address", Region.UNSPECIFIED.name(), null, 2,
                "pass");
        ArrayList<Clinician> clinicians = new ArrayList<>();
        clinicians.add(clinician);
        clinicians.add(clinician2);
        manager = new ClinicianManagerMemory(clinicians);

        assertTrue(manager.doesStaffIdExist(2));
    }
}
