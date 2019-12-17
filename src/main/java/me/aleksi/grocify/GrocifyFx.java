package me.aleksi.grocify;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
import java.util.Optional;

/**
 * <p>GrocifyFx class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class GrocifyFx extends Application {
    static final DecimalFormat FORMAT_AMOUNT = new DecimalFormat("#");
    static final DecimalFormat FORMAT_PRICE = new DecimalFormat("#.0");

    private Window fileChooserOwnerWindow;
    private FileChooser fileChooser = new FileChooser();

    private TabPane tabPane = new TabPane();
    private GroceryList currentList;

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        launch(args);
    }

    static TextFormatter<?> getFormatter(DecimalFormat format) {
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

        var addBox = buildNewItemBox();
        addBox.prefWidthProperty().bind(primaryStage.widthProperty());

        tabPane.getStyleClass().add("floating");
        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldVal, newVal) -> {
            if (newVal == null) {
                currentList = null;
            } else {
                currentList = (GroceryList) newVal.getContent();
            }
            addBox.getChildren().get(addBox.getChildren().size() - 1).setDisable(newVal == null);
        });

        addTab("Untitled");

        var root = new VBox();

        var content = new VBox();
        content.setSpacing(5);
        content.setPadding(new Insets(0, 5, 10, 5));

        root.getChildren().addAll(menuBar, content);
        content.getChildren().addAll(tabPane, addBox);

        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        var scene = new Scene(root, 480, 640);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> {
            var dirty = false;
            for (var tab : tabPane.getTabs()) {
                if (((GroceryList) tab.getContent()).isDirty()) {
                    dirty = true;
                    break;
                }
            }

            if (dirty && !confirmCloseApp()) {
                e.consume();
            }
        });

        primaryStage.show();
    }

    private Tab addTab(String name) {
        var list = buildGroceryList();
        list.setName(name);

        var tab = new Tab(name, list);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        list.dirtyProperty().addListener((ov, oldVal, newVal) -> {
            if (newVal) {
                tab.setText("*" + list.getName());
            } else {
                tab.setText(list.getName());
            }
        });

        tab.setOnCloseRequest(e -> {
            if (list.isDirty()) {
                if (!confirmCloseTab()) {
                    e.consume();
                }
            }
        });

        return tab;
    }

    private MenuBar buildMenuBar() {
        var menuBar = new MenuBar();

        final var fileMenu = new Menu("_File");

        var menuNew = new MenuItem("_New");
        var menuOpen = new MenuItem("_Open…");
        var menuSave = new MenuItem("_Save");
        var menuSaveAs = new MenuItem("Save _As…");

        menuNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

        menuNew.setOnAction(e -> actionFileNew());
        menuOpen.setOnAction(e -> actionFileOpen());
        menuSave.setOnAction(e -> actionFileSave());
        menuSaveAs.setOnAction(e -> actionFileSaveAs());
        fileMenu.getItems().addAll(menuNew, menuOpen, menuSave, menuSaveAs);

        final var helpMenu = new Menu("_Help");

        var menuHelp = new MenuItem("_Help");
        var menuAbout = new MenuItem("_About");

        helpMenu.getItems().addAll(menuHelp, menuAbout);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    private GroceryList buildGroceryList() {
        var list = new GroceryList();

        list.setOnDragDropped(e -> {
            var db = e.getDragboard();
            var success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = loadFile(db.getFiles().get(0));
            }
            e.setDropCompleted(success);
            e.consume();
        });

        return list;
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

        addAmount.setTextFormatter(getFormatter(FORMAT_AMOUNT));
        addPrice.setTextFormatter(getFormatter(FORMAT_PRICE));

        addButton.setOnAction(e -> {
            var name = addName.getText();
            if (name.isBlank()) {
                return;
            }

            Integer amount = null;
            try {
                amount = FORMAT_AMOUNT.parse(addAmount.getText()).intValue();
            } catch (ParseException ex) {
                // Should never come here if TextFormatter works right
            }
            var price = addPrice.getLength() > 0 ? new BigDecimal(addPrice.getText()) : null;
            currentList.getItems().add(new GroceryListItem(name, amount, price));
            currentList.setDirty(true);

            addName.clear();
            addAmount.clear();
            addPrice.clear();
        });

        addBox.getChildren().addAll(addName, addAmount, addPrice, addButton);
        addBox.setSpacing(3);

        return addBox;
    }

    private void actionFileNew() {
        addTab("Untitled");
    }

    private void actionFileOpen() {
        var file = fileChooser.showOpenDialog(fileChooserOwnerWindow);
        if (file != null) {
            loadFile(file);
        }
    }

    private void actionFileSave() {
        if (currentList == null) return;

        var file = currentList.getFile();
        if (file == null) {
            actionFileSaveAs();
            return;
        }
        if (saveToFile(file)) {
            currentList.setDirty(false);
        }
    }

    private void actionFileSaveAs() {
        if (currentList == null) return;

        fileChooser.setInitialFileName(currentList.getName());
        if (currentList.getFile() != null) {
            fileChooser.setInitialDirectory(currentList.getFile().getParentFile());
        }
        var file = fileChooser.showSaveDialog(fileChooserOwnerWindow);
        if (file != null) {
            if (saveToFile(file)) {
                currentList.setFile(file);
                var name = getBaseName(file);
                currentList.setName(name);
                currentList.setDirty(false);
                tabPane.getSelectionModel().getSelectedItem().setText(name);
            }
        }
    }

    private boolean loadFile(File file) {
        var listName = getBaseName(file);
        var list = (GroceryList) addTab(listName).getContent();

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
                list.getItems().add(new GroceryListItem(name, amount, price));
            }
            list.setName(listName);
            list.setFile(file);
            list.setDirty(false);
            return true;
        } catch (IOException | JSONParseException | JSONTypeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Grocify");
            alert.setHeaderText("Error reading file");
            alert.setContentText(e.getMessage());
            alert.show();
            return false;
        }
    }

    private boolean saveToFile(File file) {
        var arr = new JSONArray();
        currentList.getItems().forEach(e -> arr.add(new JSONObject()
            .put("name", e.getName())
            .put("amount", e.getAmount())
            .put("price", e.getPricePerUnit())));

        try (var writer = new PrintWriter(file)) {
            writer.println(arr.toJSONString(new JSONWriterOptions()));
            return true;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Grocify");
            alert.setHeaderText("Error writing file");
            alert.setContentText(e.getMessage());
            alert.show();
            return false;
        }
    }

    private String getBaseName(File file) {
        var fullName = file.getName();
        var lastDot = fullName.lastIndexOf('.');

        if (lastDot != -1) {
            return fullName.substring(0, lastDot);
        }
        return fullName;
    }

    private boolean confirmCloseApp() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Grocify");
        alert.setHeaderText("You have unsaved changes. Are you sure you want to quit?");
        alert.setContentText("Your changes will be lost if you don't save them.");

        ButtonType btnClose = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnClose, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnClose;
    }

    private boolean confirmCloseTab() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Grocify");
        alert.setHeaderText("Do you want to save the changes you made to " + currentList.getName() + "?");
        alert.setContentText("Your changes will be lost if you don't save them.");

        ButtonType btnSave = new ButtonType("Save");
        ButtonType btnNoSave = new ButtonType("Don't save");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSave, btnNoSave, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSave) {
            actionFileSave();
            return true;
        } else {
            return result.isPresent() && result.get() == btnNoSave;
        }
    }
}
