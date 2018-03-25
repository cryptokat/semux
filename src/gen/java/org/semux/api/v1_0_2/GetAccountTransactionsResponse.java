package org.semux.api.v1_0_2;

import java.util.ArrayList;
import java.util.List;
import org.semux.api.v1_0_2.ApiHandlerResponse;
import org.semux.api.v1_0_2.TransactionType;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class GetAccountTransactionsResponse extends ApiHandlerResponse  {
  
  private @Valid List<TransactionType> result = new ArrayList<TransactionType>();

  /**
   **/
  public GetAccountTransactionsResponse result(List<TransactionType> result) {
    this.result = result;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("result")
  public List<TransactionType> getResult() {
    return result;
  }
  public void setResult(List<TransactionType> result) {
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
    GetAccountTransactionsResponse getAccountTransactionsResponse = (GetAccountTransactionsResponse) o;
    return Objects.equals(result, getAccountTransactionsResponse.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetAccountTransactionsResponse {\n");
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

