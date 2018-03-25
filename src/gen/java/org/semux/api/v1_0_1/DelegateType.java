package org.semux.api.v1_0_1;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DelegateType   {
  
  private @Valid String address = null;
  private @Valid String name = null;
  private @Valid Long registeredAt = null;
  private @Valid Long votes = null;
  private @Valid Long blocksForged = null;
  private @Valid Long turnsHit = null;
  private @Valid Long turnsMissed = null;

  /**
   **/
  public DelegateType address(String address) {
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
  public DelegateType name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public DelegateType registeredAt(Long registeredAt) {
    this.registeredAt = registeredAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("registeredAt")
  public Long getRegisteredAt() {
    return registeredAt;
  }
  public void setRegisteredAt(Long registeredAt) {
    this.registeredAt = registeredAt;
  }

  /**
   **/
  public DelegateType votes(Long votes) {
    this.votes = votes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("votes")
  public Long getVotes() {
    return votes;
  }
  public void setVotes(Long votes) {
    this.votes = votes;
  }

  /**
   **/
  public DelegateType blocksForged(Long blocksForged) {
    this.blocksForged = blocksForged;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("blocksForged")
  public Long getBlocksForged() {
    return blocksForged;
  }
  public void setBlocksForged(Long blocksForged) {
    this.blocksForged = blocksForged;
  }

  /**
   **/
  public DelegateType turnsHit(Long turnsHit) {
    this.turnsHit = turnsHit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("turnsHit")
  public Long getTurnsHit() {
    return turnsHit;
  }
  public void setTurnsHit(Long turnsHit) {
    this.turnsHit = turnsHit;
  }

  /**
   **/
  public DelegateType turnsMissed(Long turnsMissed) {
    this.turnsMissed = turnsMissed;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("turnsMissed")
  public Long getTurnsMissed() {
    return turnsMissed;
  }
  public void setTurnsMissed(Long turnsMissed) {
    this.turnsMissed = turnsMissed;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelegateType delegateType = (DelegateType) o;
    return Objects.equals(address, delegateType.address) &&
        Objects.equals(name, delegateType.name) &&
        Objects.equals(registeredAt, delegateType.registeredAt) &&
        Objects.equals(votes, delegateType.votes) &&
        Objects.equals(blocksForged, delegateType.blocksForged) &&
        Objects.equals(turnsHit, delegateType.turnsHit) &&
        Objects.equals(turnsMissed, delegateType.turnsMissed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, name, registeredAt, votes, blocksForged, turnsHit, turnsMissed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DelegateType {\n");
    
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    registeredAt: ").append(toIndentedString(registeredAt)).append("\n");
    sb.append("    votes: ").append(toIndentedString(votes)).append("\n");
    sb.append("    blocksForged: ").append(toIndentedString(blocksForged)).append("\n");
    sb.append("    turnsHit: ").append(toIndentedString(turnsHit)).append("\n");
    sb.append("    turnsMissed: ").append(toIndentedString(turnsMissed)).append("\n");
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

