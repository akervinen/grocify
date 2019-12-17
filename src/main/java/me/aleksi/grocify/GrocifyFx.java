package me.aleksi.grocify;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import me.aleksi.jayson.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * <p>GrocifyFx class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class GrocifyFx extends Application {
    private final DecimalFormat amountFormat = new DecimalFormat("#");
    private final DecimalFormat priceParseFormat = new DecimalFormat("#.0");
    private ObservableList<GroceryListItem> data = FXCollections.observableArrayList();

    private Window fileChooserOwnerWindow;
    private FileChooser fileChooser = new FileChooser();

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        launch(args);
    }

    private TextFormatter<?> getFormatter(DecimalFormat format) {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty())
                return change;

            var parsePos = new ParsePosition(0);
            var obj = format.parse(change.getControlNewText(), parsePos);
            if (obj == null || parsePos.getIndex() < change.getControlNewText().length())
                return null;

            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Grocify");

        var extFilter = new FileChooser.ExtensionFilter("JSON file (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooserOwnerWindow = primaryStage;

        var menuBar = buildMenuBar();
        menuBar.setUseSystemMenuBar(true);

        var table = buildGroceryList();
        var addBox = buildNewItemBox();
        addBox.prefWidthProperty().bind(primaryStage.widthProperty());

        var root = new VBox();
        root.setSpacing(5);
        root.setPadding(Insets.EMPTY);

        var content = new VBox();
        content.setSpacing(5);
        content.setPadding(new Insets(10, 5, 5, 10));

        root.getChildren().addAll(menuBar, content);
        content.getChildren().addAll(table, addBox);

        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        primaryStage.setScene(new Scene(root, 480, 640));
        primaryStage.show();
    }

    private MenuBar buildMenuBar() {
        var menuBar = new MenuBar();

        final var fileMenu = new Menu("File");

        var menuOpen = new MenuItem("Open…");
        var menuSaveAs = new MenuItem("Save As…");

        menuOpen.setOnAction(e -> {
            var file = fileChooser.showOpenDialog(fileChooserOwnerWindow);
            if (file != null) {
                data.clear();
                loadFile(file);
            }
        });

        menuSaveAs.setOnAction(e -> {
            var file = fileChooser.showSaveDialog(fileChooserOwnerWindow);
            if (file != null) {
                saveToFile(file);
            }
        });

        fileMenu.getItems().addAll(menuOpen, menuSaveAs);

        menuBar.getMenus().addAll(fileMenu);

        return menuBar;
    }

    private TableView<GroceryListItem> buildGroceryList() {
        TableView<GroceryListItem> table = new TableView<>();

        table.setEditable(true);
        table.setItems(data);

        // Disable focus border on table
        table.setStyle("-fx-background-color: -fx-box-border, -fx-control-inner-background; -fx-background-insets: 0, 1;");

        var nameCol = new TableColumn<GroceryListItem, String>("Name");
        nameCol.setEditable(true);
        nameCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(tc -> new EditableCell<>() {
            @Override
            protected String fromString(String str) {
                return str;
            }
        });
        nameCol.setOnEditCommit(cee -> cee.getTableView().getItems().get(cee.getTablePosition().getRow()).setName(cee.getNewValue()));

        var amountCol = new TableColumn<GroceryListItem, Integer>("Amount");
        amountCol.setEditable(true);
        amountCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(tc -> new EditableCell<>(getFormatter(amountFormat)) {
            @Override
            protected Integer fromString(String str) {
                return str.isBlank() ? null : Integer.valueOf(str);
            }
        });
        amountCol.setOnEditCommit(cee -> cee.getTableView().getItems().get(cee.getTablePosition().getRow()).setAmount(cee.getNewValue()));

        var priceCol = new TableColumn<GroceryListItem, BigDecimal>("Price per Unit");
        priceCol.setEditable(true);
        priceCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        priceCol.setCellFactory(tc -> new EditableCell<>(getFormatter(priceParseFormat)) {
            @Override
            protected BigDecimal fromString(String str) {
                return str.isBlank() ? null : new BigDecimal(str);
            }
        });
        priceCol.setOnEditCommit(cee -> cee.getTableView().getItems().get(cee.getTablePosition().getRow()).setPricePerUnit(cee.getNewValue()));

        table.getColumns().add(nameCol);
        table.getColumns().add(amountCol);
        table.getColumns().add(priceCol);

        table.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.DELETE) {
                var idx = table.getSelectionModel().getSelectedIndex();
                if (idx >= 0) {
                    data.remove(idx);
                }
            }
        });

        table.setOnDragOver(e -> {
            if (e.getGestureSource() != table && e.getDragboard().hasFiles()) {
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

        table.setOnDragDropped(e -> {
            var db = e.getDragboard();
            var success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = loadFile(db.getFiles().get(0));
            }
            e.setDropCompleted(success);
            e.consume();
        });

        return table;
    }

    private Pane buildNewItemBox() {
        var addBox = new HBox();

        var addName = new TextField();
        var addAmount = new TextField();
        var addPrice = new TextField();
        var addButton = new Button("Add");

        addName.setPromptText("Name");
        addAmount.setPromptText("Amount");
        addPrice.setPromptText("Price per Unit");

        addName.prefWidthProperty().bind(addBox.prefWidthProperty().multiply(0.4));
        addAmount.prefWidthProperty().bind(addBox.prefWidthProperty().multiply(0.2));
        addPrice.prefWidthProperty().bind(addBox.prefWidthProperty().multiply(0.2));
        addButton.prefWidthProperty().bind(addBox.prefWidthProperty().multiply(0.2));

        addAmount.setTextFormatter(getFormatter(amountFormat));
        addPrice.setTextFormatter(getFormatter(priceParseFormat));

        addButton.setOnAction(e -> {
            var name = addName.getText();
            if (name.isBlank()) {
                return;
            }

            Integer amount = null;
            try {
                amount = amountFormat.parse(addAmount.getText()).intValue();
            } catch (ParseException ex) {
                // Should never come here if TextFormatter works right
            }
            var price = addPrice.getLength() > 0 ? new BigDecimal(addPrice.getText()) : null;
            data.add(new GroceryListItem(name, amount, price));

            addName.clear();
            addAmount.clear();
            addPrice.clear();
        });

        addBox.getChildren().addAll(addName, addAmount, addPrice, addButton);
        addBox.setSpacing(3);

        return addBox;
    }

    private boolean loadFile(File file) {
        try {
            var res = new JSONReader().parse(Files.readAllBytes(file.toPath()));
            for (var e : res.getArray()) {
                var o = e.getObject();
                var name = o.get("name").getString();
                Integer amount = null;
                var num = o.get("amount").getNumber();
                if (num != null)
                    amount = num.intValue();
                var price = (BigDecimal) o.get("price").getNumber();
                data.add(new GroceryListItem(name, amount, price));
            }
            return true;
        } catch (IOException | JSONParseException | JSONTypeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error reading file");
            alert.setContentText(e.getMessage());
            alert.show();
            return false;
        }
    }

    private void saveToFile(File file) {
        var arr = new JSONArray();
        data.forEach(e -> arr.add(new JSONObject()
            .put("name", e.getName())
            .put("amount", e.getAmount())
            .put("price", e.getPricePerUnit())));

        try (var writer = new PrintWriter(file)) {
            writer.println(arr.toJSONString(new JSONWriterOptions()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error writing file");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}
