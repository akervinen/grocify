package me.aleksi.grocify;

import java.math.BigDecimal;

/**
 * <p>GroceryListItem class.</p>
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class GroceryListItem {
    private String name;
    private Integer amount;
    private BigDecimal pricePerUnit;

    /**
     * <p>Constructor for GroceryListItem.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public GroceryListItem(String name) {
        this.name = name;
    }

    /**
     * <p>Constructor for GroceryListItem.</p>
     *
     * @param name         a {@link java.lang.String} object.
     * @param amount       a {@link java.lang.Integer} object.
     * @param pricePerUnit a {@link java.math.BigDecimal} object.
     */
    public GroceryListItem(String name, Integer amount, BigDecimal pricePerUnit) {
        this.name = name;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
    }

    /**
     * Check if item is completely blank.
     *
     * Used to determine if a row should be deleted after being edited.
     *
     * @return true if all properties of item are blank or null.
     */
    public boolean isEmpty() {
        return (name == null || name.isEmpty()) && getAmount() == null && getPricePerUnit() == null;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Getter for the field <code>amount</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * <p>Setter for the field <code>amount</code>.</p>
     *
     * @param amount a int.
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * <p>Getter for the field <code>pricePerUnit</code>.</p>
     *
     * @return a {@link java.math.BigDecimal} object.
     */
    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    /**
     * <p>Setter for the field <code>pricePerUnit</code>.</p>
     *
     * @param pricePerUnit a {@link java.math.BigDecimal} object.
     */
    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
