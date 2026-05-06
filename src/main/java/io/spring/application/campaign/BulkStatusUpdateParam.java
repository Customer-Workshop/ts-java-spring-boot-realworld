package io.spring.application.campaign;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("bulk")
@NoArgsConstructor
@AllArgsConstructor
public class BulkStatusUpdateParam {
  private List<String> campaignIds;
  private String status;
}
