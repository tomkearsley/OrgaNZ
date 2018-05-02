package seng302.Controller.Components;

import java.util.HashSet;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import seng302.Utilities.Enums.Organ;

import org.controlsfx.control.CheckComboBox;

public class OrganCheckComboBoxCell<T> extends TableCell<T, Set<Organ>> {
    private final CheckComboBox<Organ> checkComboBox;

    public OrganCheckComboBoxCell(TableColumn<T, Set<Organ>> column) {
        checkComboBox = new CheckComboBox<>();
        checkComboBox.getItems().setAll(Organ.values());
        checkComboBox.disableProperty().bind(column.editableProperty().not());

        checkComboBox.addEventHandler(ComboBox.ON_SHOWN, event -> {
            final TableView<T> tableView = getTableView();
            tableView.getSelectionModel().select(getTableRow().getIndex());
            tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
        });

        checkComboBox.addEventHandler(ComboBox.ON_HIDDEN, event -> cancelEdit());

        checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<Organ>) change -> {
            if (isEditing()) {
                commitEdit(new HashSet<>(change.getList()));
            }
        });
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Set<Organ> item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        if (item == null || empty) {
            setGraphic(null);
        } else {
            for (Organ organ : item) {
                if (!checkComboBox.getCheckModel().isChecked(organ)) {
                    checkComboBox.getCheckModel().check(organ);
                }
            }
            setGraphic(checkComboBox);
        }
    }
}
