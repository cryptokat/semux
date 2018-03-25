package org.semux.api.v1_0_1;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class PeerType   {
  
  private @Valid String ip = null;
  private @Valid Integer port = null;
  private @Valid Integer networkVersion = null;
  private @Valid String clientId = null;
  private @Valid String peerId = null;
  private @Valid Long latestBlockNumber = null;
  private @Valid Long latency = null;
  private @Valid List<String> capabilities = new ArrayList<String>();

  /**
   **/
  public PeerType ip(String ip) {
    this.ip = ip;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ip")
  public String getIp() {
    return ip;
  }
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   **/
  public PeerType port(Integer port) {
    this.port = port;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("port")
  public Integer getPort() {
    return port;
  }
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   **/
  public PeerType networkVersion(Integer networkVersion) {
    this.networkVersion = networkVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("networkVersion")
  public Integer getNetworkVersion() {
    return networkVersion;
  }
  public void setNetworkVersion(Integer networkVersion) {
    this.networkVersion = networkVersion;
  }

  /**
   **/
  public PeerType clientId(String clientId) {
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
  public PeerType peerId(String peerId) {
    this.peerId = peerId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("peerId")
  public String getPeerId() {
    return peerId;
  }
  public void setPeerId(String peerId) {
    this.peerId = peerId;
  }

  /**
   **/
  public PeerType latestBlockNumber(Long latestBlockNumber) {
    this.latestBlockNumber = latestBlockNumber;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latestBlockNumber")
  public Long getLatestBlockNumber() {
    return latestBlockNumber;
  }
  public void setLatestBlockNumber(Long latestBlockNumber) {
    this.latestBlockNumber = latestBlockNumber;
  }

  /**
   **/
  public PeerType latency(Long latency) {
    this.latency = latency;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latency")
  public Long getLatency() {
    return latency;
  }
  public void setLatency(Long latency) {
    this.latency = latency;
  }

  /**
   **/
  public PeerType capabilities(List<String> capabilities) {
    this.capabilities = capabilities;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("capabilities")
  public List<String> getCapabilities() {
    return capabilities;
  }
  public void setCapabilities(List<String> capabilities) {
    this.capabilities = capabilities;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeerType peerType = (PeerType) o;
    return Objects.equals(ip, peerType.ip) &&
        Objects.equals(port, peerType.port) &&
        Objects.equals(networkVersion, peerType.networkVersion) &&
        Objects.equals(clientId, peerType.clientId) &&
        Objects.equals(peerId, peerType.peerId) &&
        Objects.equals(latestBlockNumber, peerType.latestBlockNumber) &&
        Objects.equals(latency, peerType.latency) &&
        Objects.equals(capabilities, peerType.capabilities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port, networkVersion, clientId, peerId, latestBlockNumber, latency, capabilities);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeerType {\n");
    
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    networkVersion: ").append(toIndentedString(networkVersion)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    peerId: ").append(toIndentedString(peerId)).append("\n");
    sb.append("    latestBlockNumber: ").append(toIndentedString(latestBlockNumber)).append("\n");
    sb.append("    latency: ").append(toIndentedString(latency)).append("\n");
    sb.append("    capabilities: ").append(toIndentedString(capabilities)).append("\n");
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

