// feeds-dto/src/main/java/com/olh/feeds/dto/request/folder/FolderSourceRequest.java
package com.olh.feeds.dto.request.folder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class FolderSourceRequest {

    @NotNull(message = "{folder.source.source_id.required}")
    private Long sourceId;
}