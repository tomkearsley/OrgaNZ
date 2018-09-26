package com.humanharvest.organz.controller.clinician;

import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.DashboardStatistics;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.controller.components.DeceasedDonorCell;
import com.humanharvest.organz.controller.components.DonatedOrganCell;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.state.Session;
import com.humanharvest.organz.state.State;

public class DashboardController extends SubController {

    private final Session session;
    private DashboardStatistics statistics;
    private final ClientManager manager;


    @FXML
    private Pane menuBarPane;

    @FXML
    private Label totalClientsNum, organsNum, requestNum;

    @FXML
    private PieChart pieChart;

    @FXML
    private ListView<Client> deceasedDonorsList;

    @FXML
    private ListView<DonatedOrgan> expiringOrgansList;

    private ObservableList<DonatedOrgan> observableOrgansToDonate =  FXCollections.observableArrayList();


    public DashboardController() {
        session = State.getSession();
        manager = State.getClientManager();
    }

    @Override
    public void setup(MainController mainController) {
        super.setup(mainController);
        mainController.setTitle("Dashboard");
        mainController.loadNavigation(menuBarPane);


        refresh();
    }

    private void generatePieChartData(){
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new Data("Donors",statistics.getDonorCount()),
            new Data("Receivers",statistics.getReceiverCount()),
            new Data("Both",statistics.getDonorReceiverCount()),
            new Data("Neither",statistics.getNeitherCount())

        );
        pieChart.setData(pieChartData);
    }

    @Override
    public void refresh() {
        statistics = manager.getStatistics();
        totalClientsNum.setText(String.valueOf(statistics.getClientCount()));
        organsNum.setText(String.valueOf(statistics.getOrganCount()));
        requestNum.setText(String.valueOf(statistics.getRequestCount()));

        deceasedDonorsList.getItems().setAll(manager.getViableDeceasedDonors());



        generatePieChartData();
        updateOrgansToDonateList();
    }

    /**
     * Initialize the page.
     */
    @FXML
    private void initialize() {

        updateOrgansToDonateList();

        deceasedDonorsList.setItems((FXCollections.observableArrayList(manager.getViableDeceasedDonors())));

        deceasedDonorsList.setCellFactory(param -> {
            DeceasedDonorCell item = new DeceasedDonorCell();
            item.setMaxWidth(deceasedDonorsList.getWidth());
            System.out.println(deceasedDonorsList.getWidth());

            return item;
        });

        expiringOrgansList.setItems(observableOrgansToDonate);

        expiringOrgansList.setCellFactory(param -> {
            DonatedOrganCell item = new DonatedOrganCell();
            item.setMaxWidth(expiringOrgansList.getWidth());

            return item;
        });

        // Attach timer to update table each second (for time until expiration)
        Timeline clock = new Timeline(new KeyFrame(
                javafx.util.Duration.millis(1000),
                event -> {
                    expiringOrgansList.refresh();
                    observableOrgansToDonate.removeIf(donatedOrgan ->
                            donatedOrgan.getOverrideReason() != null ||
                                    donatedOrgan.getDurationUntilExpiry() != null &&
                                            donatedOrgan.getDurationUntilExpiry().minusSeconds(1).isNegative());
                }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        updateOrgansToDonateList();
    }

    private void updateOrgansToDonateList() {
        observableOrgansToDonate = FXCollections.observableArrayList(manager.getAllOrgansToDonate
                ().stream().filter(o -> o.getDurationUntilExpiry() != null).collect(Collectors.toList()));
    }
}
