package com.humanharvest.organz;

import java.time.LocalDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.humanharvest.organz.utilities.enums.Organ;
import com.humanharvest.organz.utilities.enums.TransplantRequestStatus;
import com.humanharvest.organz.views.client.Views;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Represents a request for a client to receive a transplant for a given organ.
 */
@Entity
@Table
@Access(AccessType.FIELD)
public class TransplantRequest {

    @Id
    @GeneratedValue
    @JsonView(Views.Overview.class)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "Client_uid")
    @JsonBackReference
    private Client client;
    @JsonView(Views.Overview.class)
    private Organ requestedOrgan;
    @JsonView(Views.Overview.class)
    private LocalDateTime requestDate;
    @JsonView(Views.Overview.class)
    private LocalDateTime resolvedDate;
    @Enumerated(EnumType.STRING)
    @JsonView(Views.Overview.class)
    private TransplantRequestStatus status = TransplantRequestStatus.WAITING;
    @JsonView(Views.Overview.class)
    @Column(columnDefinition = "text")
    private String resolvedReason;

    protected TransplantRequest() {
    }

    public TransplantRequest(Client client, Organ requestedOrgan) {
        this.client = client;
        this.requestedOrgan = requestedOrgan;
        requestDate = LocalDateTime.now();
    }

    public TransplantRequest(Client client, Organ requestedOrgan, LocalDateTime requestDate) {
        this.client = client;
        this.requestedOrgan = requestedOrgan;
        this.requestDate = requestDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    /**
     * This method should be called only when this record is added to/removed from a client's collection.
     * Therefore it is package-private so it may only be called from Client.
     *
     * @param client The client to set this record as belonging to.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    public Organ getRequestedOrgan() {
        return requestedOrgan;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public TransplantRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TransplantRequestStatus status) {
        this.status = status;
    }

    public String getResolvedReason() {
        return resolvedReason;
    }

    /**
     * Should only be used by ResolveTransplantRequestAction
     *
     * @param resolvedReason reason that the transplant request was resolved
     */
    public void setResolvedReason(String resolvedReason) {
        this.resolvedReason = resolvedReason;
    }
}
