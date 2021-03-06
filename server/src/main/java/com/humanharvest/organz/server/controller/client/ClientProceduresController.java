package com.humanharvest.organz.server.controller.client;

import java.util.Collection;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonView;
import com.humanharvest.organz.Client;
import com.humanharvest.organz.ProcedureRecord;
import com.humanharvest.organz.actions.Action;
import com.humanharvest.organz.actions.client.AddProcedureRecordAction;
import com.humanharvest.organz.actions.client.DeleteProcedureRecordAction;
import com.humanharvest.organz.actions.client.ModifyProcedureRecordAction;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.exceptions.AuthenticationException;
import com.humanharvest.organz.utilities.exceptions.IfMatchFailedException;
import com.humanharvest.organz.utilities.exceptions.IfMatchRequiredException;
import com.humanharvest.organz.views.client.ModifyProcedureObject;
import com.humanharvest.organz.views.client.Views;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides handlers for requests to these endpoints:
 * - GET /clients/{uid}/procedures
 * - POST /clients/{uid}/procedures
 * - PATCH /clients/{uid}/procedures/{id}
 * - DELETE /clients/{uid}/procedures/{id}
 */
@RestController
public class ClientProceduresController {

    @GetMapping("/clients/{uid}/procedures")
    public ResponseEntity<Collection<ProcedureRecord>> getProceduresForClient(
            @PathVariable int uid,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws AuthenticationException, IfMatchFailedException, IfMatchRequiredException {

        Optional<Client> client = State.getClientManager().getClientByID(uid);
        if (client.isPresent()) {
            // Check request has authorization to view client's procedures
            State.getAuthenticationManager().verifyClientAccess(authToken, client.get());

            // Add the ETag to the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setETag(client.get().getETag());

            // Returns the pending procedures for the client
            return new ResponseEntity<>(client.get().getProcedures(), headers, HttpStatus.OK);
        } else {
            // Client does not exist, return 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/clients/{uid}/procedures")
    public ResponseEntity<Collection<ProcedureRecord>> createProcedureRecord(
            @RequestBody ProcedureRecord procedureRecord,
            @PathVariable int uid,
            @RequestHeader(value = "If-Match", required = false) String eTag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws AuthenticationException, IfMatchFailedException, IfMatchRequiredException {

        // Check request has authorization to create a procedure
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        Optional<Client> client = State.getClientManager().getClientByID(uid);
        if (client.isPresent()) {
            // Check that eTag is still valid
            if (eTag == null) {
                throw new IfMatchRequiredException();
            } else if (!client.get().getETag().equals(eTag)) {
                throw new IfMatchFailedException();
            }

            // Execute add procedure action
            Action action = new AddProcedureRecordAction(client.get(), procedureRecord, State.getClientManager());
            State.getActionInvoker(authToken).execute(action);

            // Add the new ETag to the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setETag(client.get().getETag());

            // Return response containing list of client's procedures
            return new ResponseEntity<>(client.get().getProcedures(), headers, HttpStatus.CREATED);
        } else {
            // No client exists with that id, return 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/clients/{uid}/procedures/{id}")
    @JsonView(Views.Overview.class)
    public ResponseEntity<ProcedureRecord> modifyProcedureRecord(
            @RequestBody ModifyProcedureObject modifyProcedureObject,
            @PathVariable int uid,
            @PathVariable int id,
            @RequestHeader(value = "If-Match", required = false) String eTag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken) throws AuthenticationException {

        // Check request has authorization to patch a procedure
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        // Try to find a client with matching uid
        Optional<Client> client = State.getClientManager().getClientByID(uid);
        if (client.isPresent()) {
            // Try to find a procedure record with matching id
            Optional<ProcedureRecord> toModify = client.get().getProcedures().stream()
                    .filter(procedure -> procedure.getId() != null && procedure.getId() == id)
                    .findFirst();

            if (toModify.isPresent()) {
                // Check that eTag is still valid
                if (eTag == null) {
                    throw new IfMatchRequiredException();
                } else if (!client.get().getETag().equals(eTag)) {
                    throw new IfMatchFailedException();
                }

                // Create action
                ModifyProcedureRecordAction action = new ModifyProcedureRecordAction(
                        toModify.get(), State.getClientManager());

                // Register all changes
                if (modifyProcedureObject.getSummary() != null) {
                    action.changeSummary(modifyProcedureObject.getSummary());
                }
                if (modifyProcedureObject.getDescription() != null) {
                    action.changeDescription(modifyProcedureObject.getDescription());
                }
                if (modifyProcedureObject.getDate() != null) {
                    action.changeDate(modifyProcedureObject.getDate());
                }
                if (modifyProcedureObject.getAffectedOrgans() != null) {
                    action.changeAffectedOrgans(modifyProcedureObject.getAffectedOrgans());
                }

                // Execute the action
                try {
                    State.getActionInvoker(authToken).execute(action);
                } catch (IllegalStateException exc) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }

                // Add the new ETag to the headers
                HttpHeaders headers = new HttpHeaders();
                headers.setETag(client.get().getETag());

                // Return OK response
                return new ResponseEntity<>(toModify.get(), headers, HttpStatus.CREATED);
            }
        }

        // No client/procedure exists with those ids, return 404
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/clients/{uid}/procedures/{id}")
    public ResponseEntity deleteProcedureRecord(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestHeader(value = "If-Match", required = false) String eTag,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws AuthenticationException, IfMatchFailedException, IfMatchRequiredException {

        // Check request has authorization to delete a procedure
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        // Try to find a client with matching uid
        Optional<Client> client = State.getClientManager().getClientByID(uid);
        if (client.isPresent()) {
            // Try to find a procedure record with matching id
            Optional<ProcedureRecord> toDelete = client.get().getProcedures().stream()
                    .filter(procedure -> procedure.getId() != null && procedure.getId() == id)
                    .findFirst();

            if (toDelete.isPresent()) {
                // Check that eTag is still valid
                if (eTag == null) {
                    throw new IfMatchRequiredException();
                } else if (!client.get().getETag().equals(eTag)) {
                    throw new IfMatchFailedException();
                }

                // Execute delete procedure action
                Action action = new DeleteProcedureRecordAction(client.get(), toDelete.get(), State.getClientManager());
                State.getActionInvoker(authToken).execute(action);

                // Add the new ETag to the headers
                HttpHeaders headers = new HttpHeaders();
                headers.setETag(client.get().getETag());

                // Return OK response
                return new ResponseEntity<>(headers, HttpStatus.OK);
            }
        }

        // No client/procedure exists with those ids, return 404
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
