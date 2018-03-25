package org.semux.api.v1_0_1;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class AccountType   {
  
  private @Valid String address = null;
  private @Valid Long available = null;
  private @Valid Long locked = null;
  private @Valid Long nonce = null;
  private @Valid Integer transactionCount = null;

  /**
   **/
  public AccountType address(String address) {
    this.address = address;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   **/
  public AccountType available(Long available) {
    this.available = available;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("available")
  public Long getAvailable() {
    return available;
  }
  public void setAvailable(Long available) {
    this.available = available;
  }

  /**
   **/
  public AccountType locked(Long locked) {
    this.locked = locked;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("locked")
  public Long getLocked() {
    return locked;
  }
  public void setLocked(Long locked) {
    this.locked = locked;
  }

  /**
   **/
  public AccountType nonce(Long nonce) {
    this.nonce = nonce;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("nonce")
  public Long getNonce() {
    return nonce;
  }
  public void setNonce(Long nonce) {
    this.nonce = nonce;
  }

  /**
   **/
  public AccountType transactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("transactionCount")
  public Integer getTransactionCount() {
    return transactionCount;
  }
  public void setTransactionCount(Integer transactionCount) {
    this.transactionCount = transactionCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountType accountType = (AccountType) o;
    return Objects.equals(address, accountType.address) &&
        Objects.equals(available, accountType.available) &&
        Objects.equals(locked, accountType.locked) &&
        Objects.equals(nonce, accountType.nonce) &&
        Objects.equals(transactionCount, accountType.transactionCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, available, locked, nonce, transactionCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountType {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    available: ").append(toIndentedString(available)).append("\n");
    sb.append("    locked: ").append(toIndentedString(locked)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
    sb.append("    transactionCount: ").append(toIndentedString(transactionCount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

