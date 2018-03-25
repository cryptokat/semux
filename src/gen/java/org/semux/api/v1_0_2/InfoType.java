package org.semux.api.v1_0_2;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class InfoType   {
  
  private @Valid String clientId = null;
  private @Valid String coinbase = null;
  private @Valid String latestBlockNumber = null;
  private @Valid String latestBlockHash = null;
  private @Valid String activePeers = null;
  private @Valid String pendingTransactions = null;

  /**
   **/
  public InfoType clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   **/
  public InfoType coinbase(String coinbase) {
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
  public InfoType latestBlockNumber(String latestBlockNumber) {
    this.latestBlockNumber = latestBlockNumber;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latestBlockNumber")
 @Pattern(regexp="^\\d+$")  public String getLatestBlockNumber() {
    return latestBlockNumber;
  }
  public void setLatestBlockNumber(String latestBlockNumber) {
    this.latestBlockNumber = latestBlockNumber;
  }

  /**
   **/
  public InfoType latestBlockHash(String latestBlockHash) {
    this.latestBlockHash = latestBlockHash;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latestBlockHash")
  public String getLatestBlockHash() {
    return latestBlockHash;
  }
  public void setLatestBlockHash(String latestBlockHash) {
    this.latestBlockHash = latestBlockHash;
  }

  /**
   **/
  public InfoType activePeers(String activePeers) {
    this.activePeers = activePeers;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("activePeers")
 @Pattern(regexp="^\\d+$")  public String getActivePeers() {
    return activePeers;
  }
  public void setActivePeers(String activePeers) {
    this.activePeers = activePeers;
  }

  /**
   **/
  public InfoType pendingTransactions(String pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("pendingTransactions")
 @Pattern(regexp="^\\d+$")  public String getPendingTransactions() {
    return pendingTransactions;
  }
  public void setPendingTransactions(String pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InfoType infoType = (InfoType) o;
    return Objects.equals(clientId, infoType.clientId) &&
        Objects.equals(coinbase, infoType.coinbase) &&
        Objects.equals(latestBlockNumber, infoType.latestBlockNumber) &&
        Objects.equals(latestBlockHash, infoType.latestBlockHash) &&
        Objects.equals(activePeers, infoType.activePeers) &&
        Objects.equals(pendingTransactions, infoType.pendingTransactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, coinbase, latestBlockNumber, latestBlockHash, activePeers, pendingTransactions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InfoType {\n");
    
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    coinbase: ").append(toIndentedString(coinbase)).append("\n");
    sb.append("    latestBlockNumber: ").append(toIndentedString(latestBlockNumber)).append("\n");
    sb.append("    latestBlockHash: ").append(toIndentedString(latestBlockHash)).append("\n");
    sb.append("    activePeers: ").append(toIndentedString(activePeers)).append("\n");
    sb.append("    pendingTransactions: ").append(toIndentedString(pendingTransactions)).append("\n");
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

