package com.nvt.it_job.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqLoginDTO {
    @NotBlank(message = "Username không được bỏ trống")
    private String username;
    @NotBlank(message = "password không được bỏ trống")
    private String password;
}
