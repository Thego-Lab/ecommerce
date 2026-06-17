package com.ecommerce.dto.request;

public class UpdateCartItemRequest {
    private Integer quantity;
    private Integer checked;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getChecked() { return checked; }
    public void setChecked(Integer checked) { this.checked = checked; }
}
