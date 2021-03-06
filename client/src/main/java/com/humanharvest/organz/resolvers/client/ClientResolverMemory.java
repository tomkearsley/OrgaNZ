package com.humanharvest.organz.resolvers.client;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.IllnessRecord;
import com.humanharvest.organz.MedicationRecord;
import com.humanharvest.organz.ProcedureRecord;
import com.humanharvest.organz.TransplantRequest;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.enums.Organ;
import com.humanharvest.organz.utilities.exceptions.OrganAlreadyRegisteredException;
import com.humanharvest.organz.views.client.CreateClientView;
import com.humanharvest.organz.views.client.CreateIllnessView;
import com.humanharvest.organz.views.client.CreateMedicationRecordView;
import com.humanharvest.organz.views.client.CreateProcedureView;
import com.humanharvest.organz.views.client.CreateTransplantRequestView;
import com.humanharvest.organz.views.client.ModifyClientObject;
import com.humanharvest.organz.views.client.ModifyIllnessObject;
import com.humanharvest.organz.views.client.ModifyProcedureObject;
import com.humanharvest.organz.views.client.ResolveTransplantRequestObject;
import org.springframework.beans.BeanUtils;

public class ClientResolverMemory implements ClientResolver {

    //------------GETs----------------

    public Map<Organ, Boolean> getOrganDonationStatus(Client client) {
        return client.getOrganDonationStatus();
    }

    public List<TransplantRequest> getTransplantRequests(Client client) {
        return client.getTransplantRequests();
    }

    public List<MedicationRecord> getMedicationRecords(Client client) {
        return client.getMedications();
    }

    public List<ProcedureRecord> getProcedureRecords(Client client) {
        return client.getProcedures();
    }

    public List<IllnessRecord> getIllnessRecords(Client client) {
        return client.getIllnesses();
    }

    public List<HistoryItem> getHistory(Client client) {
        return client.getChangesHistory();
    }

    //------------POSTs----------------

    public Client createClient(CreateClientView createClientView) {

        //Get the next empty UID
        Optional<Client> nextEmpty = State.getClientManager().getClients()
                .stream()
                .max(Comparator.comparing(Client::getUid));
        int nextId = 0;
        if (nextEmpty.isPresent()) {
            nextId = nextEmpty.get().getUid() + 1;
        }

        Client client = new Client(
                createClientView.getFirstName(),
                createClientView.getMiddleName(),
                createClientView.getLastName(),
                createClientView.getDateOfBirth(),
                nextId);
        State.getClientManager().addClient(client);
        return client;
    }

    public List<TransplantRequest> createTransplantRequest(Client client, CreateTransplantRequestView request) {
        TransplantRequest transplantRequest = new TransplantRequest(client, request.getRequestedOrgan());
        client.addTransplantRequest(transplantRequest);
        State.getClientManager().applyChangesTo(client);
        return client.getTransplantRequests();
    }

    public List<IllnessRecord> addIllnessRecord(Client client, CreateIllnessView createIllnessView) {
        IllnessRecord illnessRecord = new IllnessRecord(createIllnessView.getIllnessName(),
                createIllnessView.getDiagnosisDate(),
                createIllnessView.isChronic());
        client.addIllnessRecord(illnessRecord);
        State.getClientManager().applyChangesTo(client);
        return client.getIllnesses();
    }

    public List<MedicationRecord> addMedicationRecord(Client client, CreateMedicationRecordView medicationRecordView) {
        MedicationRecord medicationRecord = new MedicationRecord(medicationRecordView.getName(),
                LocalDate.now(), null);
        client.addMedicationRecord(medicationRecord);
        State.getClientManager().applyChangesTo(client);
        return client.getMedications();
    }

    public List<ProcedureRecord> addProcedureRecord(Client client, CreateProcedureView procedureView) {
        ProcedureRecord procedureRecord = new ProcedureRecord(
                procedureView.getSummary(),
                procedureView.getDescription(),
                procedureView.getDate());
        client.addProcedureRecord(procedureRecord);
        State.getClientManager().applyChangesTo(client);
        return client.getProcedures();
    }

    //------------PATCHs----------------

    @Override
    public Map<Organ, Boolean> modifyOrganDonation(Client client, Map<Organ, Boolean> changes) {
        for (Entry<Organ, Boolean> entry : changes.entrySet()) {
            try {
                client.setOrganDonationStatus(entry.getKey(), entry.getValue());
            } catch (OrganAlreadyRegisteredException e) {
                e.printStackTrace();
            }
        }
        return client.getOrganDonationStatus();
    }

    public TransplantRequest resolveTransplantRequest(Client client, TransplantRequest request,
            ResolveTransplantRequestObject resolveTransplantRequestObject) {
        request.setStatus(resolveTransplantRequestObject.getStatus());
        request.setResolvedReason(resolveTransplantRequestObject.getResolvedReason());
        request.setResolvedDate(resolveTransplantRequestObject.getResolvedDate());
        return request;
    }

    public Client modifyClientDetails(Client client, ModifyClientObject modifyClientObject) {
        BeanUtils.copyProperties(modifyClientObject, client, modifyClientObject.getUnmodifiedFields());
        return client;
    }

    public MedicationRecord modifyMedicationRecord(Client client, MedicationRecord record, LocalDate stopDate) {
        if (stopDate == null) {
            record.setStarted(LocalDate.now());
            record.setStopped(null);
        } else {
            record.setStopped(LocalDate.now());
        }
        return record;
    }

    public IllnessRecord modifyIllnessRecord(Client client, IllnessRecord toModify,
            ModifyIllnessObject modifyIllnessObject) {
        BeanUtils.copyProperties(modifyIllnessObject, toModify, modifyIllnessObject.getUnmodifiedFields());
        return toModify;
    }

    public ProcedureRecord modifyProcedureRecord(Client client, ProcedureRecord toModify, ModifyProcedureObject
            modifyProcedureObject) {

        BeanUtils.copyProperties(modifyProcedureObject, toModify, modifyProcedureObject.getUnmodifiedFields());
        return toModify;
    }

    //------------DELETEs----------------

    public void deleteIllnessRecord(Client client, IllnessRecord record) {
        client.deleteIllnessRecord(record);
    }

    public void deleteProcedureRecord(Client client, ProcedureRecord record) {
        client.deleteProcedureRecord(record);
    }

    @Override
    public void deleteMedicationRecord(Client client, MedicationRecord record) {
        client.deleteMedicationRecord(record);
    }
}
