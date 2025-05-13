package com.mybudget.common.event.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriesAfterUserDeleteEvent {
    private String username;
}
