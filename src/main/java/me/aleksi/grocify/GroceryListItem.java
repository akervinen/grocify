package me.aleksi.grocify;

import java.math.BigDecimal;

/**
 * Grocery list item containing simple data.
 *
 * @author Aleksi Kervinen
 * @version 1.0-SNAPSHOT
 */
public class GroceryListItem {
    private String name;
    private Integer amount;
    private BigDecimal pricePerUnit;

    /**
     * Create new grocery list item with given name and no quantity or price.
     *
     * @param name item name
     */
    public GroceryListItem(String name) {
        this.name = name;
    }

    /**
     * Create new grocery list item with given name, quantity and price.
     *
     * @param name         item name
     * @param amount       item amount
     * @param pricePerUnit item price per unit
     */
    public GroceryListItem(String name, Integer amount, BigDecimal pricePerUnit) {
        this.name = name;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
    }

    /**
     * Check if item is completely blank.
     *
     * <p>Used to determine if a row should be deleted after being edited.</p>
     *
     * @return true if all properties of item are blank or null.
     */
    public boolean isEmpty() {
        return (name == null || name.isEmpty()) && getAmount() == null && getPricePerUnit() == null;
    }

    /**
     * Get item name.
     *
     * @return item name
     */
    public String getName() {
        return name;
    }

    /**
     * Set item name.
     *
     * @param name new item name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get item amount, or null if none is set.
     *
     * @return item amount or null
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Set item amount, can be null.
     *
     * @param amount new item amount, or null
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * Get item price per unit or null if none is set.
     *
     * @return item price per unit, or null
     */
    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    /**
     * Set item price per unit, can be null.
     *
     * @param pricePerUnit new price per unit, or null
     */
    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
