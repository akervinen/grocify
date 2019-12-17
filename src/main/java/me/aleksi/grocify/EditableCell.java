package me.aleksi.grocify;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

/**
 * TableCell that can be edited.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public abstract class EditableCell<T> extends TableCell<GroceryListItem, T> {
    private TextField textField;
    private TextFormatter<?> textFormatter;

    /**
     * Create a new EditableCell.
     */
    public EditableCell() {
        super();
        setEditable(true);
    }

    /**
     * Create a new EditableCell with given {@link TextFormatter} to validate input.
     *
     * @param textFormatter a {@link javafx.scene.control.TextFormatter} for input validation
     */
    public EditableCell(TextFormatter<?> textFormatter) {
        this();
        this.textFormatter = textFormatter;
    }

    /**
     * Called when editing starts, creates a text field and focuses it.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }

        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        textField.requestFocus();
        textField.selectAll();
    }

    /**
     * Cancel editing, hides text field.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getString());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        textField.setText(getString());
    }

    /**
     * Update backing item with new data. Also updates text field.
     *
     * @param item new item for cell
     * @param empty whether cell has no item
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText("");
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setTextFormatter(textFormatter);
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(fromString(textField.getText()));
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        textField.focusedProperty().addListener((value, oldVal, newVal) -> {
            if (oldVal && !newVal) {
                commitEdit(fromString(textField.getText()));
            }
        });
    }

    /**
     * Create the T object from an input string.
     *
     * @param str user inputted string
     * @return a T object from given string
     */
    protected abstract T fromString(String str);

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
