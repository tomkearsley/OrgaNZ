package com.humanharvest.organz.server.controller.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.MedicationRecord;
import com.humanharvest.organz.actions.client.AddMedicationRecordAction;
import com.humanharvest.organz.actions.client.DeleteMedicationRecordAction;
import com.humanharvest.organz.actions.client.ModifyMedicationRecordAction;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.exceptions.IfMatchFailedException;
import com.humanharvest.organz.utilities.exceptions.IfMatchRequiredException;
import com.humanharvest.organz.views.client.CreateMedicationRecordView;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientMedicationsController {

    /**
     * Checks that the given ETag matches the current ETag for the client,
     * and exception is thrown if the ETag is missing or does not match
     * @param client client to validate the ETag for
     * @param ETag The corresponding If-Match header to check for concurrent update handling
     * @throws IfMatchRequiredException Thrown if the Etag header is missing
     * @throws IfMatchFailedException Thrown if the Etag does not match the clients current ETag
     */
    private void checkClientEtag(Client client, String ETag)
    throws IfMatchRequiredException, IfMatchFailedException {

        if (ETag == null) {
            throw new IfMatchRequiredException();
        } else if (!client.getETag().equals(ETag)) {
            System.out.println(client.getETag());
            throw new IfMatchFailedException();
        }
    }

    /**
     * The GET endpoint for getting all medications for a given client
     * @param uid the uid of the client
     * @return If successful, a ResponseEntity containing the full list of the clients MedicationRecords is
     * returned
     */
    @GetMapping("/clients/{uid}/medications")
    public ResponseEntity<List<MedicationRecord>> getMedications(@PathVariable int uid,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken) {

        Optional<Client> client = State.getClientManager().getClientByID(uid);

        if (client.isPresent()) {

            // Check authentication
            State.getAuthenticationManager().verifyClientAccess(authToken, client.get());

            HttpHeaders headers = new HttpHeaders();
            headers.setETag(client.get().getETag());

            return new ResponseEntity<>(client.get().getMedications(), headers, HttpStatus.OK);

        } else {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * The POST endpoint for adding a new medication for a given client
     * @param uid the uid of the client
     * @param medicationRecordView view to create MedicationRecord
     * @param ETag The corresponding If-Match header to check for concurrent update handling
     * @return If successful, a ResponseEntity containing all the clients past and current MedicationRecords is
     * returned
     * @throws IfMatchRequiredException Thrown if the Etag header is missing
     * @throws IfMatchFailedException Thrown if the Etag does not match the clients current ETag
     */
    @PostMapping("/clients/{uid}/medications")
    public ResponseEntity<List<MedicationRecord>> postMedication(
            @PathVariable int uid,
            @RequestBody CreateMedicationRecordView medicationRecordView,
            @RequestHeader(value = "If-Match", required = false) String ETag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws IfMatchRequiredException, IfMatchFailedException {

        Optional<Client> client = State.getClientManager().getClientByID(uid);

        if (!client.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check authentication
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        if (medicationRecordView.getName() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        checkClientEtag(client.get(), ETag);

        MedicationRecord record = new MedicationRecord(medicationRecordView.getName(),
                medicationRecordView.getStarted(),
                null);
        AddMedicationRecordAction action = new AddMedicationRecordAction(client.get(),
                record,
                State.getClientManager());
        State.getActionInvoker(authToken).execute(action);

        // TODO: Refactor this to fix this issue
        Client client1 = State.getClientManager()
                .getClientByID(client.get().getUid())
                .orElseThrow(IllegalStateException::new);

        HttpHeaders headers = new HttpHeaders();
        headers.setETag(client1.getETag());

        return new ResponseEntity<>(client1.getMedications(), headers, HttpStatus.CREATED);
    }

    /**
     * The DELETE endpoint for deleting a MedicationRecord for a client
     * @param uid The uid of the client
     * @param id The id of the medication to delete
     * @param ETag The corresponding If-Match header to check for concurrent update handling
     * @return If successful, a ResponseEntity with status CREATED (DELETED) is returned
     * @throws IfMatchRequiredException Thrown if the Etag header is missing
     * @throws IfMatchFailedException Thrown if the Etag does not match the clients current ETag
     */
    @DeleteMapping("/clients/{uid}/medications/{id}")
    public ResponseEntity deleteMedication(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestHeader(value = "If-Match", required = false) String ETag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws IfMatchRequiredException, IfMatchFailedException {

        Optional<Client> client = State.getClientManager().getClientByID(uid);

        if (!client.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check authentication
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        checkClientEtag(client.get(), ETag);

        MedicationRecord record = client.get().getMedicationRecord(id);

        if (record == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } else {
            DeleteMedicationRecordAction action = new DeleteMedicationRecordAction(client.get(), record, State
                    .getClientManager());
            State.getActionInvoker(authToken).execute(action);

            Client client1 = State.getClientManager()
                    .getClientByID(client.get().getUid())
                    .orElseThrow(IllegalStateException::new);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setETag(client1.getETag());

            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        }
    }

    /**
     * The POST endpoint for indicating that a medication has been started
     * @param uid The uid of the client
     * @param id The id of the medication to delete
     * @param ETag The corresponding If-Match header to check for concurrent update handling
     * @return If successful, a ResponseEntity with the altered record is returned
     * @throws IfMatchRequiredException Thrown if the Etag header is missing
     * @throws IfMatchFailedException Thrown if the Etag does not match the clients current ETag
     */
    @PostMapping("/clients/{uid}/medications/{id}/start")
    public ResponseEntity<MedicationRecord> postMedicationStart(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestHeader(value = "If-Match", required = false) String ETag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws IfMatchFailedException, IfMatchRequiredException {

        Optional<Client> client = State.getClientManager().getClientByID(uid);

        // Check authentication
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        if (!client.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        checkClientEtag(client.get(), ETag);

        MedicationRecord record = client.get().getMedicationRecord(id);

        if (record == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ModifyMedicationRecordAction action = new ModifyMedicationRecordAction(record, State.getClientManager());
        action.changeStarted(LocalDate.now());
        action.changeStopped(null);
        State.getActionInvoker(authToken).execute(action);

        HttpHeaders headers = new HttpHeaders();
        headers.setETag(client.get().getETag());

        return new ResponseEntity<>(record, headers, HttpStatus.OK);
    }

    /**
     * The POST endpoint for indicating that a medication has been stopped
     * @param uid The uid of the client
     * @param id The id of the medication to delete
     * @param ETag The corresponding If-Match header to check for concurrent update handling
     * @return If successful, a ResponseEntity with the altered record is returned
     * @throws IfMatchRequiredException Thrown if the Etag header is missing
     * @throws IfMatchFailedException Thrown if the Etag does not match the clients current ETag
     */
    @PostMapping("/clients/{uid}/medications/{id}/stop")
    public ResponseEntity<MedicationRecord> postMedicationStop(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestHeader(value = "If-Match", required = false) String ETag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws IfMatchFailedException, IfMatchRequiredException {

        // Check authentication
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        Optional<Client> client = State.getClientManager().getClientByID(uid);

        if (!client.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        checkClientEtag(client.get(), ETag);

        MedicationRecord record = client.get().getMedicationRecord(id);

        if (record == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ModifyMedicationRecordAction action = new ModifyMedicationRecordAction(record, State.getClientManager());
        action.changeStopped(LocalDate.now());
        State.getActionInvoker(authToken).execute(action);

        HttpHeaders headers = new HttpHeaders();
        headers.setETag(client.get().getETag());

        return new ResponseEntity<>(record, headers, HttpStatus.OK);
    }
}
