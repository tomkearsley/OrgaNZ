package com.humanharvest.organz.controller.spiderweb;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.controller.components.ExpiryBarUtils;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.touch.FocusArea;
import com.humanharvest.organz.touch.MultitouchHandler;
import com.humanharvest.organz.touch.PointUtils;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.PageNavigator;

/**
 * The Spider web controller which handles everything to do with displaying panes in the spider web stage.
 */
public class SpiderWebController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(SpiderWebController.class.getName());
    private static final double LABEL_OFFSET = 50.0;

    private final Client client;

    private final Pane canvas;
    private Pane deceasedDonorPane;
    private final List<Pane> organNodes = new ArrayList<>();

    public SpiderWebController(Client client) {
        this.client = client;

        canvas = MultitouchHandler.getCanvas();
        canvas.getChildren().clear();

        // Close existing windows
        MultitouchHandler.setPhysicsHandler(new SpiderPhysicsHandler(MultitouchHandler.getRootPane()));
        for (MainController mainController : State.getMainControllers()) {
            mainController.closeWindow();
        }

        client.setDonatedOrgans(State.getClientResolver().getDonatedOrgans(client));
        displayDonatingClient();
        displayOrgans();
    }

    /**
     * Sets a node's position using an {@link Affine} transform. The node must have an {@link FocusArea} for its
     * {@link Node#getUserData()}.
     *
     * @param node The node to apply the transform to. Must have a focusArea
     * @param x The x translation
     * @param y The y translation
     * @param angle The angle to rotate (degrees)
     */
    private static void setPositionUsingTransform(Node node, double x, double y, double angle) {
        FocusArea focusArea = (FocusArea) node.getUserData();

        Point2D centre = PointUtils.getCentreOfNode(node);

        Affine transform = new Affine();
        transform.append(new Translate(x, y));
        transform.append(new Rotate(angle, centre.getX(), centre.getY()));
        focusArea.setTransform(transform);
        node.setCacheHint(CacheHint.QUALITY);
    }

    /**
     * Loads a window for each non expired organ.
     */
    private void displayOrgans() {
        for (DonatedOrgan organ : client.getDonatedOrgans()) {
            if (!organ.hasExpired()) {
                addOrganNode(organ);
            }
        }
        layoutOrganNodes(300);
    }

    private static void updateConnector(DonatedOrgan donatedOrgan, Line line, Text durationText) {
        Duration duration = donatedOrgan.getDurationUntilExpiry();
        if (ExpiryBarUtils.isDurationZero(duration)) {
            line.setStroke(ExpiryBarUtils.greyColour);
        } else {
            // Progress as a decimal. starts at 0 (at time of death) and goes to 1.
            double progressDecimal = donatedOrgan.getProgressDecimal();
            double fullMarker = donatedOrgan.getFullMarker();

            LinearGradient linearGradient = ExpiryBarUtils.getLinearGradient(progressDecimal, fullMarker,
                    line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());

            line.setStroke(linearGradient);
        }

        durationText.setText(ExpiryBarUtils.getDurationString(donatedOrgan));

        durationText.getTransforms().removeIf(transform -> {
            return transform instanceof Affine;
        });

        Affine trans = new Affine();

        double xWidth = line.getStartX() - line.getEndX();
        double yWidth = line.getStartY() - line.getEndY();
        double lineWidth = Math.sqrt(Math.pow(xWidth, 2) + Math.pow(yWidth, 2));
        trans.prepend(new Translate(Math.max(LABEL_OFFSET, lineWidth * 0.2), -5));

        double angle = getAngle(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
        trans.prepend(new Rotate(angle));
        durationText.getTransforms().add(trans);

        double x = line.getEndX();
        durationText.setTranslateX(x);

        double y = line.getEndY();
        durationText.setTranslateY(y);
    }

    private static double getAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private void addOrganNode(DonatedOrgan organ) {
        State.setOrganToDisplay(organ);
        MainController newMain = PageNavigator.openNewWindow(80, 80);
        PageNavigator.loadPage(Page.ORGAN_IMAGE, newMain);
        newMain.getStyles().clear();
        Pane organPane = newMain.getPane();
        FocusArea organFocus = (FocusArea) organPane.getUserData();
        organFocus.setScalable(false);
        organFocus.setCollidable(true);
        organNodes.add(organPane);
        organPane.setOnMouseClicked(click -> {
            if (click.getClickCount() == 3) {
                if (organ.getOverrideReason() == null) {
                    State.getClientResolver()
                            .manuallyOverrideOrgan(organ, "Manually Overridden by Doctor using WebView");
                    organ.manuallyOverride("Manually Overridden by Doctor using WebView");

                } else {
                    State.getClientResolver().cancelManualOverrideForOrgan(organ);
                    organ.cancelManualOverride();
                }
            }
        });
        // Create the line
        Line connector = new Line();
        connector.setStrokeWidth(4);
        Text durationText = new Text(ExpiryBarUtils.getDurationString(organ));

        // Redraws lines when organs or donor pane is moved
        deceasedDonorPane.localToParentTransformProperty().addListener((observable, oldValue, newValue) -> {
            Bounds bounds = deceasedDonorPane.getBoundsInParent();
            connector.setStartX(bounds.getMinX() + bounds.getWidth() / 2);
            connector.setStartY(bounds.getMinY() + bounds.getHeight() / 2);
            updateConnector(organ, connector, durationText);
        });
        organPane.localToParentTransformProperty().addListener((observable, oldValue, newValue) -> {
            Bounds bounds = organPane.getBoundsInParent();
            connector.setEndX(bounds.getMinX() + bounds.getWidth() / 2);
            connector.setEndY(bounds.getMinY() + bounds.getHeight() / 2);
            updateConnector(organ, connector, durationText);
        });

        canvas.getChildren().add(0, connector);
        canvas.getChildren().add(0, durationText);

        Bounds bounds = deceasedDonorPane.getBoundsInParent();
        connector.setStartX(bounds.getMinX() + bounds.getWidth() / 2);
        connector.setStartY(bounds.getMinY() + bounds.getHeight() / 2);
        updateConnector(organ, connector, durationText);

        // Attach timer to update connector each second (for time until expiration)
        final Timeline clock = new Timeline(new KeyFrame(
                javafx.util.Duration.seconds(1),
                event -> {
                    updateConnector(organ, connector, durationText);
                }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

    }

    private void displayDonatingClient() {
        MainController newMain = PageNavigator.openNewWindow(200, 320);
        PageNavigator.loadPage(Page.DECEASED_DONOR_OVERVIEW, newMain);
        deceasedDonorPane = newMain.getPane();
        FocusArea deceasedDonorFocus = (FocusArea) deceasedDonorPane.getUserData();
        deceasedDonorFocus.setTranslatable(false);
//        deceasedDonorFocus.setCollidable(true); TODO does this need to be here?

        Bounds bounds = deceasedDonorPane.getBoundsInParent();
        double centerX = (Screen.getPrimary().getVisualBounds().getWidth() - bounds.getWidth()) / 2;
        double centerY = (Screen.getPrimary().getVisualBounds().getHeight() - bounds.getHeight()) / 2;
        setPositionUsingTransform(deceasedDonorPane, centerX, centerY, 0);
    }

    private void layoutOrganNodes(double radius) {
        final int numNodes = organNodes.size();
        final double angleSize = (Math.PI * 2) / numNodes;

        Bounds bounds = deceasedDonorPane.getBoundsInParent();
        double centreX = bounds.getMinX() + bounds.getWidth() / 2;
        double centreY = bounds.getMinY() + bounds.getHeight() / 2;
        for (int i = 0; i < numNodes; i++) {
            setPositionUsingTransform(organNodes.get(i),
                    centreX + radius * Math.sin(angleSize * i),
                    centreY + radius * Math.cos(angleSize * i),
                    360 - Math.toDegrees(angleSize * i));
        }
    }
}