// feeds-dto/src/main/java/com/olh/feeds/dto/request/folder/FolderRequest.java
package com.olh.feeds.dto.request.folder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class FolderRequest {

    @NotBlank(message = "{folder.name.required}")
    private String name;

    private String theme;
}