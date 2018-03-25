package org.semux.api.v1_0_1;

import java.util.ArrayList;
import java.util.List;
import org.semux.api.v1_0_1.TransactionType;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class BlockType   {
  
  private @Valid String hash = null;
  private @Valid Long number = null;
  private @Valid Integer view = null;
  private @Valid String coinbase = null;
  private @Valid String parentHash = null;
  private @Valid Long timestamp = null;
  private @Valid String date = null;
  private @Valid String transactionsRoot = null;
  private @Valid String resultsRoot = null;
  private @Valid String stateRoot = null;
  private @Valid String data = null;
  private @Valid List<TransactionType> transactions = new ArrayList<TransactionType>();

  /**
   **/
  public BlockType hash(String hash) {
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
  public BlockType number(Long number) {
    this.number = number;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("number")
  public Long getNumber() {
    return number;
  }
  public void setNumber(Long number) {
    this.number = number;
  }

  /**
   **/
  public BlockType view(Integer view) {
    this.view = view;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("view")
  public Integer getView() {
    return view;
  }
  public void setView(Integer view) {
    this.view = view;
  }

  /**
   **/
  public BlockType coinbase(String coinbase) {
    this.coinbase = coinbase;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("coinbase")
  public String getCoinbase() {
    return coinbase;
  }
  public void setCoinbase(String coinbase) {
    this.coinbase = coinbase;
  }

  /**
   **/
  public BlockType parentHash(String parentHash) {
    this.parentHash = parentHash;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("parentHash")
  public String getParentHash() {
    return parentHash;
  }
  public void setParentHash(String parentHash) {
    this.parentHash = parentHash;
  }

  /**
   **/
  public BlockType timestamp(Long timestamp) {
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
  public BlockType date(String date) {
    this.date = date;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("date")
  public String getDate() {
    return date;
  }
  public void setDate(String date) {
    this.date = date;
  }

  /**
   **/
  public BlockType transactionsRoot(String transactionsRoot) {
    this.transactionsRoot = transactionsRoot;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("transactionsRoot")
  public String getTransactionsRoot() {
    return transactionsRoot;
  }
  public void setTransactionsRoot(String transactionsRoot) {
    this.transactionsRoot = transactionsRoot;
  }

  /**
   **/
  public BlockType resultsRoot(String resultsRoot) {
    this.resultsRoot = resultsRoot;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("resultsRoot")
  public String getResultsRoot() {
    return resultsRoot;
  }
  public void setResultsRoot(String resultsRoot) {
    this.resultsRoot = resultsRoot;
  }

  /**
   **/
  public BlockType stateRoot(String stateRoot) {
    this.stateRoot = stateRoot;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stateRoot")
  public String getStateRoot() {
    return stateRoot;
  }
  public void setStateRoot(String stateRoot) {
    this.stateRoot = stateRoot;
  }

  /**
   **/
  public BlockType data(String data) {
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

  /**
   **/
  public BlockType transactions(List<TransactionType> transactions) {
    this.transactions = transactions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("transactions")
  public List<TransactionType> getTransactions() {
    return transactions;
  }
  public void setTransactions(List<TransactionType> transactions) {
    this.transactions = transactions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlockType blockType = (BlockType) o;
    return Objects.equals(hash, blockType.hash) &&
        Objects.equals(number, blockType.number) &&
        Objects.equals(view, blockType.view) &&
        Objects.equals(coinbase, blockType.coinbase) &&
        Objects.equals(parentHash, blockType.parentHash) &&
        Objects.equals(timestamp, blockType.timestamp) &&
        Objects.equals(date, blockType.date) &&
        Objects.equals(transactionsRoot, blockType.transactionsRoot) &&
        Objects.equals(resultsRoot, blockType.resultsRoot) &&
        Objects.equals(stateRoot, blockType.stateRoot) &&
        Objects.equals(data, blockType.data) &&
        Objects.equals(transactions, blockType.transactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, number, view, coinbase, parentHash, timestamp, date, transactionsRoot, resultsRoot, stateRoot, data, transactions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockType {\n");
    
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    view: ").append(toIndentedString(view)).append("\n");
    sb.append("    coinbase: ").append(toIndentedString(coinbase)).append("\n");
    sb.append("    parentHash: ").append(toIndentedString(parentHash)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    transactionsRoot: ").append(toIndentedString(transactionsRoot)).append("\n");
    sb.append("    resultsRoot: ").append(toIndentedString(resultsRoot)).append("\n");
    sb.append("    stateRoot: ").append(toIndentedString(stateRoot)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
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

