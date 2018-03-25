package org.semux.api.v1_0_2;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DelegateType   {
  
  private @Valid String address = null;
  private @Valid String name = null;
  private @Valid String registeredAt = null;
  private @Valid String votes = null;
  private @Valid String blocksForged = null;
  private @Valid String turnsHit = null;
  private @Valid String turnsMissed = null;

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
  public DelegateType registeredAt(String registeredAt) {
    this.registeredAt = registeredAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("registeredAt")
 @Pattern(regexp="^\\d+$")  public String getRegisteredAt() {
    return registeredAt;
  }
  public void setRegisteredAt(String registeredAt) {
    this.registeredAt = registeredAt;
  }

  /**
   **/
  public DelegateType votes(String votes) {
    this.votes = votes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("votes")
 @Pattern(regexp="^\\d+$")  public String getVotes() {
    return votes;
  }
  public void setVotes(String votes) {
    this.votes = votes;
  }

  /**
   **/
  public DelegateType blocksForged(String blocksForged) {
    this.blocksForged = blocksForged;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("blocksForged")
 @Pattern(regexp="^\\d+$")  public String getBlocksForged() {
    return blocksForged;
  }
  public void setBlocksForged(String blocksForged) {
    this.blocksForged = blocksForged;
  }

  /**
   **/
  public DelegateType turnsHit(String turnsHit) {
    this.turnsHit = turnsHit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("turnsHit")
 @Pattern(regexp="^\\d+$")  public String getTurnsHit() {
    return turnsHit;
  }
  public void setTurnsHit(String turnsHit) {
    this.turnsHit = turnsHit;
  }

  /**
   **/
  public DelegateType turnsMissed(String turnsMissed) {
    this.turnsMissed = turnsMissed;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("turnsMissed")
 @Pattern(regexp="^\\d+$")  public String getTurnsMissed() {
    return turnsMissed;
  }
  public void setTurnsMissed(String turnsMissed) {
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

