package com.book.manager.util.ro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookCommentIn {

    @NotNull
    private Integer bookId;

    @NotBlank
    private String content;
}
