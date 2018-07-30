package com.humanharvest.organz.utilities.view;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import com.humanharvest.organz.AppTUIO;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.state.State;

/**
 * Utility class for controlling navigation between pages.
 * All methods on the navigator are static to facilitate simple access from anywhere in the application.
 */
public class PageNavigator {

    private static final Logger LOGGER = Logger.getLogger(PageNavigator.class.getName());

    public static double startDragX;
    public static double startDragY;

    /**
     * Loads the given page in the given MainController.
     * @param page the Page (enum including path to fxml file) to be loaded.
     * @param controller the MainController to load this page on to.
     */
    public static void loadPage(Page page, MainController controller) {
        try {
            LOGGER.info("Loading page: " + page);
            FXMLLoader loader = new FXMLLoader(PageNavigator.class.getResource(page.getPath()));
            Node loadedPage = loader.load();
            SubController subController = loader.getController();
            subController.setup(controller);
            controller.setSubController(subController);
            controller.setPage(page, loadedPage);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Couldn't load the page", e);
            showAlert(Alert.AlertType.ERROR, "Could not load page: " + page,
                    "The page loader failed to load the layout for the page.");
        }
    }

    /**
     * Refreshes all windows, to be used when an update occurs. Only refreshes titles and sidebars
     */
    public static void refreshAllWindows() {
        LOGGER.info("Refreshing all windows");
        for (MainController controller : State.getMainControllers()) {
            controller.refresh();
        }
    }

    /**
     * Opens a new window.
     * @return The MainController for the new window, or null if the new window could not be created.
     */
    public static MainController openNewWindow() {
        LOGGER.info("Opening new window");
        try {
            Stage newStage = new Stage();
            newStage.setTitle("Organ Client Management System");
            FXMLLoader loader = new FXMLLoader();
            Pane mainPane = loader.load(PageNavigator.class.getResourceAsStream(Page.MAIN.getPath()));
            MainController mainController = loader.getController();
            mainController.setStage(newStage);
            State.addMainController(mainController);
            newStage.setOnCloseRequest(e -> State.deleteMainController(mainController));
            TitledPane pane = new TitledPane("New", mainPane);
            pane.getProperties().put("focusArea", "true");

            pane.setStyle("   -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 10, 10);"
                    + "-fx-background-color: derive(-fx-background,0%);");

            pane.setOnMousePressed(event -> {
                System.out.println("p");
                EventTarget t = event.getTarget();
                System.out.println(t);
                pane.toFront();
                startDragX = event.getSceneX();
                startDragY = event.getSceneY();
            });

            pane.setOnMouseDragged(event -> {
                Class<? extends EventTarget> clazz = event.getTarget().getClass();
                if (clazz.getName().contains("Slider")) {
                    return;
                }
                pane.toFront();
                //TODO: Not hardcode res and not have static startDrag vars
                pane.setTranslateX(withinRange(0, 1920 - pane.getWidth(), pane.getTranslateX() + event.getSceneX() - startDragX));
                pane.setTranslateY(withinRange(0, 1080 - pane.getHeight(), pane.getTranslateY() + event.getSceneY() - startDragY));
                startDragX = event.getSceneX();
                startDragY = event.getSceneY();
            });

            pane.setOnScroll(event -> {
                Class<? extends EventTarget> clazz = event.getTarget().getClass();
                if (clazz.getName().contains("Slider")) {
                    return;
                }
                pane.toFront();
                //TODO: not hardcode res and not have static startDrag vars
//                pane.setTranslateX(withinRange(0, 1920 - pane.getWidth(), pane.getTranslateX() + event.getDeltaX()));
//                pane.setTranslateY(withinRange(0, 1080 - pane.getHeight(), pane.getTranslateY() + event.getDeltaY()));
                pane.setTranslateX(pane.getTranslateX() + event.getDeltaX());
                pane.setTranslateY(pane.getTranslateY() + event.getDeltaY());

            });
            pane.setOnRotate(event -> {
                pane.toFront();
                pane.setRotate(pane.getRotate() + event.getAngle());
            });

            pane.setOnZoom(event -> {
                pane.setScaleX(pane.getScaleX() * event.getZoomFactor());
                pane.setScaleY(pane.getScaleY() * event.getZoomFactor());
            });

            AppTUIO.root.getChildren().add(pane);

            return mainController;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading new window\n", e);
            // Will throw if MAIN's fxml file could not be loaded.
            showAlert(Alert.AlertType.ERROR, "New window could not be created",
                    "The page loader failed to load the layout for the new window.");
            return null;
        }
    }

    /**
     * Sets the alert window at the right size so that all the text can be read.
     */
    private static void resizeAlert(Alert alert) {
        alert.getDialogPane().getScene().getWindow().sizeToScene();
    }

    /**
     * Generates a pop-up alert of the given type.
     * @param alertType the type of alert to show (can determine its style and button options).
     * @param title the text to show as the title and heading of the alert.
     * @param bodyText the text to show within the body of the alert.
     * @return The generated alert.
     */
    public static Alert generateAlert(Alert.AlertType alertType, String title, String bodyText) {
        Alert alert = new Alert(alertType);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.contentTextProperty().addListener(observable -> resizeAlert(alert));

        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(bodyText);
        return alert;
    }

    /**
     * Shows a pop-up alert of the given type, and awaits user input to dismiss it (blocking).
     * @param alertType the type of alert to show (can determine its style and button options).
     * @param title the text to show as the title and heading of the alert.
     * @param bodyText the text to show within the body of the alert.
     * @return an Optional for the button that was clicked to dismiss the alert.
     */
    public static Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String bodyText) {
        Alert alert = generateAlert(alertType, title, bodyText);
        return alert.showAndWait();
    }

    private static double withinRange(double min, double max, double value) {
        return Math.min(Math.max(value, min), max);
    }
}
