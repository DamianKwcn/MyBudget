package com.mybudget.common.event.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDefaultCreateEvent {
    private String keycloakSub;
    private String username;
}
