// feeds-dto/src/main/java/com/olh/feeds/dto/request/folder/FolderSourceRequest.java
package com.olh.feeds.dto.request.folder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class FolderSourceRequest {

    @NotEmpty(message = "{folder.source.source_ids.required}")
    private List<Long> sourceIds;
}