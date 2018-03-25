package org.semux.api.v1_0_2;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TransactionLimitsType   {
  
  private @Valid String maxTransactionDataSize = null;
  private @Valid String minTransactionFee = null;
  private @Valid String minDelegateBurnAmount = null;

  /**
   **/
  public TransactionLimitsType maxTransactionDataSize(String maxTransactionDataSize) {
    this.maxTransactionDataSize = maxTransactionDataSize;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("maxTransactionDataSize")
 @Pattern(regexp="^\\d+$")  public String getMaxTransactionDataSize() {
    return maxTransactionDataSize;
  }
  public void setMaxTransactionDataSize(String maxTransactionDataSize) {
    this.maxTransactionDataSize = maxTransactionDataSize;
  }

  /**
   **/
  public TransactionLimitsType minTransactionFee(String minTransactionFee) {
    this.minTransactionFee = minTransactionFee;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("minTransactionFee")
 @Pattern(regexp="^\\d+$")  public String getMinTransactionFee() {
    return minTransactionFee;
  }
  public void setMinTransactionFee(String minTransactionFee) {
    this.minTransactionFee = minTransactionFee;
  }

  /**
   **/
  public TransactionLimitsType minDelegateBurnAmount(String minDelegateBurnAmount) {
    this.minDelegateBurnAmount = minDelegateBurnAmount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("minDelegateBurnAmount")
 @Pattern(regexp="^\\d+$")  public String getMinDelegateBurnAmount() {
    return minDelegateBurnAmount;
  }
  public void setMinDelegateBurnAmount(String minDelegateBurnAmount) {
    this.minDelegateBurnAmount = minDelegateBurnAmount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionLimitsType transactionLimitsType = (TransactionLimitsType) o;
    return Objects.equals(maxTransactionDataSize, transactionLimitsType.maxTransactionDataSize) &&
        Objects.equals(minTransactionFee, transactionLimitsType.minTransactionFee) &&
        Objects.equals(minDelegateBurnAmount, transactionLimitsType.minDelegateBurnAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxTransactionDataSize, minTransactionFee, minDelegateBurnAmount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionLimitsType {\n");
    
    sb.append("    maxTransactionDataSize: ").append(toIndentedString(maxTransactionDataSize)).append("\n");
    sb.append("    minTransactionFee: ").append(toIndentedString(minTransactionFee)).append("\n");
    sb.append("    minDelegateBurnAmount: ").append(toIndentedString(minDelegateBurnAmount)).append("\n");
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

