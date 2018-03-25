package org.semux.api.v1_0_1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.semux.api.v1_0_1.ApiHandlerResponse;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class GetVotesResponse extends ApiHandlerResponse  {
  
  private @Valid Map<String, Long> result = new HashMap<String, Long>();

  /**
   **/
  public GetVotesResponse result(Map<String, Long> result) {
    this.result = result;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("result")
  public Map<String, Long> getResult() {
    return result;
  }
  public void setResult(Map<String, Long> result) {
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
    GetVotesResponse getVotesResponse = (GetVotesResponse) o;
    return Objects.equals(result, getVotesResponse.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetVotesResponse {\n");
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

