package me.aleksi.grocify;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

/**
 * <p>Abstract EditableCell class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public abstract class EditableCell<T> extends TableCell<GroceryListItem, T> {
    private TextField textField;
    private TextFormatter<?> textFormatter;

    /**
     * <p>Constructor for EditableCell.</p>
     */
    public EditableCell() {
        super();
        setEditable(true);
    }

    /**
     * <p>Constructor for EditableCell.</p>
     *
     * @param textFormatter a {@link javafx.scene.control.TextFormatter} object.
     */
    public EditableCell(TextFormatter<?> textFormatter) {
        super();
        setEditable(true);
        this.textFormatter = textFormatter;
    }

    /**
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
     * {@inheritDoc}
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
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(fromString(textField.getText()));
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        textField.setTextFormatter(textFormatter);
    }

    /**
     * <p>fromString.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a T object.
     */
    protected abstract T fromString(String str);

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
