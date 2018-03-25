package org.semux.api.v1_0_1;

import java.util.ArrayList;
import java.util.List;
import org.semux.api.v1_0_1.ApiHandlerResponse;
import org.semux.api.v1_0_1.PeerType;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class GetPeersResponse extends ApiHandlerResponse  {
  
  private @Valid List<PeerType> result = new ArrayList<PeerType>();

  /**
   **/
  public GetPeersResponse result(List<PeerType> result) {
    this.result = result;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("result")
  public List<PeerType> getResult() {
    return result;
  }
  public void setResult(List<PeerType> result) {
    this.result = result;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetPeersResponse getPeersResponse = (GetPeersResponse) o;
    return Objects.equals(result, getPeersResponse.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetPeersResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

