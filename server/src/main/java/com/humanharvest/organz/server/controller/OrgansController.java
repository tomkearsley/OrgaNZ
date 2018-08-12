package com.humanharvest.organz.server.controller;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonView;
import com.humanharvest.organz.Client;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.actions.Action;
import com.humanharvest.organz.actions.client.ManuallyOverrideOrganAction;
import com.humanharvest.organz.server.exceptions.GlobalControllerExceptionHandler;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.views.SingleStringView;
import com.humanharvest.organz.views.client.DonatedOrganView;
import com.humanharvest.organz.views.client.Views;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrgansController {

    /**
     * The GET endpoint for getting all organs currently available to be donated
     * @param authToken authentication token - only clinicians and administrators can access donatable organs
     * @return response entity containing all organs that are available for donation
     * @throws GlobalControllerExceptionHandler.InvalidRequestException
     */
    @JsonView(Views.Overview.class)
    @GetMapping("/clients/organs")
    public ResponseEntity<Collection<DonatedOrganView>> getOrgansToDonate(
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws GlobalControllerExceptionHandler.InvalidRequestException {

        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        Collection<DonatedOrganView> donatedOrgans = State.getClientManager().getAllOrgansToDonate().stream()
                .map(DonatedOrganView::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(donatedOrgans, HttpStatus.OK);
    }

    /**
     * GET endpoint for getting a clients donated organs
     * @param uid the uid of the client
     * @param authToken authorization token
     * @return response entity containing the clients donated organs
     */
    @JsonView(Views.Overview.class)
    @GetMapping("/clients/{uid}/donatedOrgans")
    public ResponseEntity<Collection<DonatedOrganView>> getClientDonatedOrgans(
            @PathVariable int uid,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken) {

        Optional<Client> optionalClient = State.getClientManager().getClientByID(uid);

        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();

            //Auth check
            State.getAuthenticationManager().verifyClientAccess(authToken, client);

            //Add the ETag to the headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("ETag", client.getETag());

            Collection<DonatedOrganView> donatedOrgans = client.getDonatedOrgans().stream()
                    .map(DonatedOrganView::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(donatedOrgans, headers, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * POST endpoint for manually overriding an available organ.
     * @param uid The UID of the client the organ was donated by.
     * @param id The ID of the available organ.
     * @param authToken Authentication token - only clinicians and administrators can override available organs.
     * @return Response entity containing the overriden organ.
     * @throws GlobalControllerExceptionHandler.InvalidRequestException If the organ has already been overriden, or if
     * the reason given is blank.
     */
    @JsonView(Views.Details.class)
    @PostMapping("/clients/{uid}/donatedOrgans/{id}/override")
    public ResponseEntity<DonatedOrgan> manuallyExpireOrgan(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody SingleStringView overrideReason,
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws GlobalControllerExceptionHandler.InvalidRequestException {

        // Check that the request is by an authorised user.
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        // Check that the UID corresponds to an existing client.
        Client client = State.getClientManager().getClientByID(uid).orElse(null);
        if (client == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check that the UID corresponds to an existing donated organ.
        DonatedOrgan donatedOrgan = client.getDonatedOrganById(id);
        if (donatedOrgan == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // The organ in question cannot already have been overriden, and the reason given must not be blank.
        if (donatedOrgan.getOverrideReason() != null || overrideReason.getValue().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Create the action and execute it
        Action action = new ManuallyOverrideOrganAction(donatedOrgan, overrideReason.getValue(),
                State.getClientManager());
        State.getActionInvoker(authToken).execute(action);

        // Retrieve client again (has to be done due to some weird caching issues).
        client = State.getClientManager()
                .getClientByID(client.getUid())
                .orElseThrow(IllegalStateException::new);

        // Return the now overriden version of the donated organ along with the client's new ETag.
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(client.getETag());
        return new ResponseEntity<>(donatedOrgan, headers, HttpStatus.OK);
    }
}
