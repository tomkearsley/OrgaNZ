package com.humanharvest.organz.server.controller.administrator;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonView;
import com.humanharvest.organz.Administrator;
import com.humanharvest.organz.server.exceptions.GlobalControllerExceptionHandler.InvalidRequestException;
import com.humanharvest.organz.utilities.exceptions.AuthenticationException;
import com.humanharvest.organz.utilities.validators.administrator.CreateAdministratorValidator;
import com.humanharvest.organz.views.administrator.CreateAdministratorView;
import com.humanharvest.organz.views.client.Views;
import com.humanharvest.organz.state.AdministratorManager;
import com.humanharvest.organz.state.State;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdministratorController {

    /**
     * Returns all administrators or some optional subset by filtering
     * @return A list of Administrator overviews
     * @throws AuthenticationException throws when a non-administrator attempts access.
     */
    @GetMapping("/administrators")
    @JsonView(Views.Overview.class)
    public ResponseEntity<Iterable<Administrator>> getAdministrators(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer count,
            @RequestHeader(value = "X-Auth-Token", required = false) String authentication) {
        State.getAuthenticationManager().verifyAdminAccess(authentication);

        AdministratorManager administratorManager = State.getAdministratorManager();

        return new ResponseEntity<>(
                administratorManager.getAdministratorsFiltered(query, offset, count),
                HttpStatus.OK);
    }

    /**
     * Returns all administrators or some optional subset by filtering
     * @return A list of Administrator overviews
     * @throws AuthenticationException throws when a non-administrator attempts access.
     */
    @PostMapping("/administrators")
    public ResponseEntity<Administrator> addAdministrator(
            @RequestBody CreateAdministratorView createAdministratorView,
            @RequestParam(required = false) String view,
            @RequestHeader(value = "X-Auth-Token", required = false) String authentication) {
        State.getAuthenticationManager().verifyAdminAccess(authentication);

        //Validate the request, if there are any errors an exception will be thrown.
        if (!CreateAdministratorValidator.isValid(createAdministratorView)) {
            throw new InvalidRequestException();
        }

        Administrator administrator = new Administrator(
                createAdministratorView.getUsername(),
                createAdministratorView.getPassword());

        AdministratorManager administratorManager = State.getAdministratorManager();

        administratorManager.addAdministrator(administrator);

        //Add the new ETag to the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(administrator.getEtag());

        return new ResponseEntity<>(administrator, headers, HttpStatus.CREATED);
    }

    /**
     * Returns an administrator from the given username
     * @return An administrator detail view
     * @throws AuthenticationException throws when a non-administrator attempts access.
     */
    @GetMapping("/administrators/{username}")
    @JsonView(Views.Details.class)
    public ResponseEntity<Administrator> getAdministrator(
            @PathVariable String username,
            @RequestHeader(value = "X-Auth-Token", required = false) String authentication) {
        State.getAuthenticationManager().verifyAdminAccess(authentication);

        AdministratorManager administratorManager = State.getAdministratorManager();
        Optional<Administrator> administrator = administratorManager.getAdministratorByUsername(username);
        if (administrator.isPresent()) {
            //Add the new ETag to the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setETag(administrator.get().getEtag());

            return new ResponseEntity<>(administrator.get(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
