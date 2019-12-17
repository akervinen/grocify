package me.aleksi.grocify;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.math.BigDecimal;

public class GroceryList extends TableView<GroceryListItem> {
    private ObservableList<GroceryListItem> data = FXCollections.observableArrayList();
    private String name;
    private File file;

    private ObjectProperty<Boolean> dirty = new SimpleObjectProperty<>(false);

    public GroceryList() {
        this("Untitled");
    }

    public GroceryList(String name) {
        this.setEditable(true);
        this.setItems(data);

        setName(name);

        // Disable focus border on table
        this.setStyle("-fx-background-color: -fx-box-border, -fx-control-inner-background; -fx-background-insets: 0, 1;");

        var nameCol = new TableColumn<GroceryListItem, String>("Name");
        nameCol.setEditable(true);
        nameCol.prefWidthProperty().bind(this.widthProperty().divide(2));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(tc -> new EditableCell<>() {
            @Override
            protected String fromString(String str) {
                return str;
            }
        });
        nameCol.setOnEditCommit(cee -> {
            setDirty(true);
            var item = cee.getRowValue();
            item.setName(cee.getNewValue());
            if (item.isEmpty()) {
                data.remove(item);
            }
        });

        var amountCol = new TableColumn<GroceryListItem, Integer>("Amount");
        amountCol.setEditable(true);
        amountCol.prefWidthProperty().bind(this.widthProperty().divide(4));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(tc -> new EditableCell<>(GrocifyFx.getFormatter(GrocifyFx.FORMAT_AMOUNT)) {
            @Override
            protected Integer fromString(String str) {
                return str.isBlank() ? null : Integer.valueOf(str);
            }
        });
        amountCol.setOnEditCommit(cee -> {
            setDirty(true);
            cee.getRowValue().setAmount(cee.getNewValue());
        });

        var priceCol = new TableColumn<GroceryListItem, BigDecimal>("Price per Unit");
        priceCol.setEditable(true);
        priceCol.prefWidthProperty().bind(this.widthProperty().divide(4));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        priceCol.setCellFactory(tc -> new EditableCell<>(GrocifyFx.getFormatter(GrocifyFx.FORMAT_PRICE)) {
            @Override
            protected BigDecimal fromString(String str) {
                return str.isBlank() ? null : new BigDecimal(str);
            }
        });
        priceCol.setOnEditCommit(cee -> {
            setDirty(true);
            cee.getRowValue().setPricePerUnit(cee.getNewValue());
        });

        this.getColumns().add(nameCol);
        this.getColumns().add(amountCol);
        this.getColumns().add(priceCol);

        this.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.DELETE) {
                var idx = this.getSelectionModel().getSelectedIndex();
                if (idx >= 0) {
                    data.remove(idx);
                    setDirty(true);
                }
            }
        });

        this.setOnDragOver(e -> {
            if (e.getGestureSource() != this && e.getDragboard().hasFiles()) {
                if (e.getDragboard().getFiles().size() == 1) {
                    var first = e.getDragboard().getFiles().get(0);
                    var extIdx = first.getName().lastIndexOf('.');
                    if (extIdx != -1 && first.getName().substring(extIdx).equals(".json")) {
                        e.acceptTransferModes(TransferMode.COPY);
                    }
                }
            }
            e.consume();
        });

        this.getItems().addListener((ListChangeListener<? super GroceryListItem>) e -> this.setDirty(true));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }

    public ObjectProperty<Boolean> dirtyProperty() {
        return dirty;
    }
}
