package com.humanharvest.organz.server.controller;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonView;
import com.humanharvest.organz.server.exceptions.GlobalControllerExceptionHandler;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.views.client.DonatedOrganView;
import com.humanharvest.organz.views.client.Views;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
