package com.humanharvest.organz.resolvers.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.IllnessRecord;
import com.humanharvest.organz.MedicationRecord;
import com.humanharvest.organz.ProcedureRecord;
import com.humanharvest.organz.TransplantRequest;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.enums.Organ;
import com.humanharvest.organz.views.client.CreateClientView;
import com.humanharvest.organz.views.client.CreateIllnessView;
import com.humanharvest.organz.views.client.CreateMedicationRecordView;
import com.humanharvest.organz.views.client.CreateProcedureView;
import com.humanharvest.organz.views.client.CreateTransplantRequestView;
import com.humanharvest.organz.views.client.ModifyClientObject;
import com.humanharvest.organz.views.client.ModifyIllnessObject;
import com.humanharvest.organz.views.client.ModifyProcedureObject;
import com.humanharvest.organz.views.client.ResolveTransplantRequestObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ClientResolverRest implements ClientResolver {

    //------------GETs----------------


    public Map<Organ, Boolean> getOrganDonationStatus(Client client) {
        HttpHeaders httpHeaders = createHeaders(false);
        ResponseEntity<Map<Organ, Boolean>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/donationStatus",
                HttpMethod.GET,
                new ParameterizedTypeReference<Map<Organ, Boolean>>() {
                }, client.getUid());

        client.setOrganDonationStatus(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public List<TransplantRequest> getTransplantRequests(Client client) {
        HttpHeaders httpHeaders = createHeaders(false);
        ResponseEntity<List<TransplantRequest>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/transplantRequests",
                HttpMethod.GET,
                new ParameterizedTypeReference<List<TransplantRequest>>() {
                }, client.getUid());

        client.setTransplantRequests(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public List<MedicationRecord> getMedicationRecords(Client client) {
        HttpHeaders httpHeaders = createHeaders(false);
        ResponseEntity<List<MedicationRecord>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/medications",
                HttpMethod.GET,
                new ParameterizedTypeReference<List<MedicationRecord>>() {
                }, client.getUid());

        client.setMedicationHistory(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public List<ProcedureRecord> getProcedureRecords(Client client) {
        HttpHeaders httpHeaders = createHeaders(false);
        httpHeaders.setETag(State.getClientEtag());

        ResponseEntity<List<ProcedureRecord>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/procedures",
                HttpMethod.GET,
                new ParameterizedTypeReference<List<ProcedureRecord>>() {
                }, client.getUid());
        return responseEntity.getBody();
    }

    public List<IllnessRecord> getIllnessRecords(Client client) {
        HttpHeaders httpHeaders = createHeaders(false);
        httpHeaders.setETag(State.getClientEtag());

        ResponseEntity<List<IllnessRecord>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/illnesses",
                HttpMethod.GET,
                new ParameterizedTypeReference<List<IllnessRecord>>() {
                }, client.getUid());
        return responseEntity.getBody();
    }

    @Override
    public List<HistoryItem> getHistory(Client client) {
        HttpHeaders httpHeaders = createHeaders(false);
        httpHeaders.setETag(State.getClientEtag());

        ResponseEntity<List<HistoryItem>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/history",
                HttpMethod.GET,
                new ParameterizedTypeReference<List<HistoryItem>>() {
                }, client.getUid());
        return responseEntity.getBody();
    }

    //------------POSTs----------------

    @Override
    public Client createClient(CreateClientView createClientView) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("X-Auth-Token", State.getToken());
        HttpEntity entity = new HttpEntity<>(createClientView, httpHeaders);

        ResponseEntity<Client> responseEntity = State.getRestTemplate()
                .postForEntity(State.BASE_URI + "clients", entity, Client.class);

        State.setClientEtag(responseEntity.getHeaders().getETag());
        return responseEntity.getBody();
    }

    public List<TransplantRequest> createTransplantRequest(Client client, CreateTransplantRequestView request) {
        HttpHeaders httpHeaders = createHeaders(true);
        System.out.println("State get clients etag: " + State.getClientEtag());
        System.out.println("Client's IfMatch: " + httpHeaders.getIfMatch());
        ResponseEntity<List<TransplantRequest>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/transplantRequests",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<TransplantRequest>>() {
                }, client.getUid());
        return responseEntity.getBody();
    }

    public List<IllnessRecord> addIllnessRecord(Client client, CreateIllnessView createIllnessView) {
        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<List<IllnessRecord>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/illnesses",
                HttpMethod.POST,
                createIllnessView,
                new ParameterizedTypeReference<List<IllnessRecord>>() {
                }, client.getUid());

        client.setIllnessHistory(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public List<MedicationRecord> addMedicationRecord(Client client, CreateMedicationRecordView medicationRecordView) {
        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<List<MedicationRecord>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/medications",
                HttpMethod.POST,
                medicationRecordView,
                new ParameterizedTypeReference<List<MedicationRecord>>() {
                }, client.getUid());

        client.setMedicationHistory(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public List<ProcedureRecord> addProcedureRecord(Client client, CreateProcedureView procedureView) {
        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<List<ProcedureRecord>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/procedures",
                HttpMethod.POST,
                procedureView,
                new ParameterizedTypeReference<List<ProcedureRecord>>() {
                }, client.getUid());

        return responseEntity.getBody();
    }

    //------------PATCHs----------------

    @Override
    public Map<Organ, Boolean> modifyOrganDonation(Client client, Map<Organ, Boolean> changes) {

        ParameterizedTypeReference<Map<Organ, Boolean>> mapRef =
                new ParameterizedTypeReference<Map<Organ, Boolean>>() {
                };

        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<Map<Organ, Boolean>> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{uid}/donationStatus",
                HttpMethod.PATCH,
                changes,
                mapRef,
                client.getUid());

        return responseEntity.getBody();
    }

    public TransplantRequest resolveTransplantRequest(Client client, TransplantRequest request,
            ResolveTransplantRequestObject resolveTransplantRequestObject) {
        HttpHeaders httpHeaders = createHeaders(true);
        long id = request.getId();
        ResponseEntity<TransplantRequest> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/transplantRequests/{requestIndex}",
                HttpMethod.PATCH,
                resolveTransplantRequestObject,
                TransplantRequest.class,
                client.getUid(),
                id);

        return responseEntity.getBody();
    }

    public Client modifyClientDetails(Client client, ModifyClientObject modifyClientObject) {
        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<Client> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}",
                HttpMethod.PATCH,
                modifyClientObject,
                Client.class,
                client.getUid());

        return responseEntity.getBody();
    }

    public MedicationRecord modifyMedicationRecord(Client client, MedicationRecord record, LocalDate stopDate) {

        String modification;
        if (stopDate == null) {
            modification = "start";
        } else {
            modification = "stop";
        }

        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<MedicationRecord> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/medications/{medicationId}/" + modification,
                HttpMethod.POST,
                MedicationRecord.class,
                client.getUid(),
                record.getId());

        return responseEntity.getBody();
    }

    public IllnessRecord modifyIllnessRecord(Client client, IllnessRecord toModify,
            ModifyIllnessObject modifyIllnessObject) {
        System.out.println(modifyIllnessObject.isChronic());
        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<IllnessRecord> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/illnesses/{illnessId}",
                HttpMethod.PATCH,
                modifyIllnessObject,
                IllnessRecord.class,
                client.getUid(),
                toModify.getId());

        return responseEntity.getBody();
    }

    public ProcedureRecord modifyProcedureRecord(Client client, ProcedureRecord toModify, ModifyProcedureObject
            modifyProcedureObject) {
        HttpHeaders httpHeaders = createHeaders(true);
        ResponseEntity<ProcedureRecord> responseEntity = sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/procedures/{procedureId}",
                HttpMethod.PATCH,
                modifyProcedureObject,
                ProcedureRecord.class,
                client.getUid(),
                toModify.getId());

        return responseEntity.getBody();
    }

    //------------DELETEs----------------

    public void deleteIllnessRecord(Client client, IllnessRecord record) {

        HttpHeaders httpHeaders = createHeaders(true);
        sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/illnesses/{illnessId}",
                HttpMethod.DELETE,
                IllnessRecord.class,
                client.getUid(),
                record.getId());

        client.deleteIllnessRecord(record);
    }

    public void deleteProcedureRecord(Client client, ProcedureRecord record) {
        HttpHeaders httpHeaders = createHeaders(true);
        sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/procedures/{procedureId}",
                HttpMethod.DELETE,
                ProcedureRecord.class,
                client.getUid(),
                record.getId());

        client.deleteProcedureRecord(record);
    }

    @Override
    public void deleteMedicationRecord(Client client, MedicationRecord record) {
        HttpHeaders httpHeaders = createHeaders(true);
        sendQuery(httpHeaders,
                State.BASE_URI + "clients/{id}/medications/{procedureId}",
                HttpMethod.DELETE,
                MedicationRecord.class,
                client.getUid(),
                record.getId());

        client.deleteMedicationRecord(record);
    }

    //------------Templates----------------

    private static HttpHeaders createHeaders(boolean addIfMatch) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Auth-Token", State.getToken());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        if (addIfMatch) {
            httpHeaders.setIfMatch(State.getClientEtag());
        }
        return httpHeaders;
    }

    private static <T> ResponseEntity<T> sendQuery(HttpHeaders httpHeaders, String url, HttpMethod method,
            ParameterizedTypeReference<T> typeReference, Object... uriVariables) {

        HttpEntity<?> entity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<T> responseEntity = State.getRestTemplate().exchange
                (url, method, entity, typeReference, uriVariables);

        if (!method.equals(HttpMethod.GET) && !method.equals(HttpMethod.HEAD)) { // if the method isn't safe
            State.setClientEtag(responseEntity.getHeaders().getETag());
        }
        return responseEntity;
    }

    private static <T> ResponseEntity<T> sendQuery(HttpHeaders httpHeaders, String url, HttpMethod method,
            Class<T> typeReference, Object... uriVariables) {

        HttpEntity<?> entity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<T> responseEntity = State.getRestTemplate().exchange
                (url, method, entity, typeReference, uriVariables);

        if (!method.equals(HttpMethod.GET) && !method.equals(HttpMethod.HEAD)) { // if the method isn't safe
            State.setClientEtag(responseEntity.getHeaders().getETag());
        }
        return responseEntity;
    }

    private static <T, Y> ResponseEntity<T> sendQuery(HttpHeaders httpHeaders, String url, HttpMethod method,
            Y value, ParameterizedTypeReference<T> typeReference, Object... uriVariables) {

        HttpEntity<Y> entity = new HttpEntity<>(value, httpHeaders);

        ResponseEntity<T> responseEntity = State.getRestTemplate().exchange
                (url, method, entity, typeReference, uriVariables);

        if (!method.equals(HttpMethod.GET) && !method.equals(HttpMethod.HEAD)) { // if the method isn't safe
            State.setClientEtag(responseEntity.getHeaders().getETag());
        }
        return responseEntity;
    }

    private static <T, Y> ResponseEntity<T> sendQuery(HttpHeaders httpHeaders, String url, HttpMethod method,
            Y value, Class<T> typeReference, Object... uriVariables) {

        HttpEntity<Y> entity = new HttpEntity<>(value, httpHeaders);

        ResponseEntity<T> responseEntity = State.getRestTemplate().exchange
                (url, method, entity, typeReference, uriVariables);

        if (!method.equals(HttpMethod.GET) && !method.equals(HttpMethod.HEAD)) { // if the method isn't safe
            State.setClientEtag(responseEntity.getHeaders().getETag());
        }
        return responseEntity;
    }
}

