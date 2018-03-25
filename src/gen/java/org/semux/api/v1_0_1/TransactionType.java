package org.semux.api.v1_0_1;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TransactionType   {
  
  private @Valid Long blockNumber = null;
  private @Valid String hash = null;
  private @Valid String type = null;
  private @Valid String from = null;
  private @Valid String to = null;
  private @Valid Long value = null;
  private @Valid Long fee = null;
  private @Valid Long nonce = null;
  private @Valid Long timestamp = null;
  private @Valid String data = null;

  /**
   **/
  public TransactionType blockNumber(Long blockNumber) {
    this.blockNumber = blockNumber;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("blockNumber")
  public Long getBlockNumber() {
    return blockNumber;
  }
  public void setBlockNumber(Long blockNumber) {
    this.blockNumber = blockNumber;
  }

  /**
   **/
  public TransactionType hash(String hash) {
    this.hash = hash;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("hash")
  public String getHash() {
    return hash;
  }
  public void setHash(String hash) {
    this.hash = hash;
  }

  /**
   **/
  public TransactionType type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public TransactionType from(String from) {
    this.from = from;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("from")
  public String getFrom() {
    return from;
  }
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   **/
  public TransactionType to(String to) {
    this.to = to;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("to")
  public String getTo() {
    return to;
  }
  public void setTo(String to) {
    this.to = to;
  }

  /**
   **/
  public TransactionType value(Long value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public Long getValue() {
    return value;
  }
  public void setValue(Long value) {
    this.value = value;
  }

  /**
   **/
  public TransactionType fee(Long fee) {
    this.fee = fee;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("fee")
  public Long getFee() {
    return fee;
  }
  public void setFee(Long fee) {
    this.fee = fee;
  }

  /**
   **/
  public TransactionType nonce(Long nonce) {
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
  public TransactionType timestamp(Long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("timestamp")
  public Long getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   **/
  public TransactionType data(String data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("data")
  public String getData() {
    return data;
  }
  public void setData(String data) {
    this.data = data;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionType transactionType = (TransactionType) o;
    return Objects.equals(blockNumber, transactionType.blockNumber) &&
        Objects.equals(hash, transactionType.hash) &&
        Objects.equals(type, transactionType.type) &&
        Objects.equals(from, transactionType.from) &&
        Objects.equals(to, transactionType.to) &&
        Objects.equals(value, transactionType.value) &&
        Objects.equals(fee, transactionType.fee) &&
        Objects.equals(nonce, transactionType.nonce) &&
        Objects.equals(timestamp, transactionType.timestamp) &&
        Objects.equals(data, transactionType.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockNumber, hash, type, from, to, value, fee, nonce, timestamp, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionType {\n");
    
    sb.append("    blockNumber: ").append(toIndentedString(blockNumber)).append("\n");
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    from: ").append(toIndentedString(from)).append("\n");
    sb.append("    to: ").append(toIndentedString(to)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    fee: ").append(toIndentedString(fee)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

