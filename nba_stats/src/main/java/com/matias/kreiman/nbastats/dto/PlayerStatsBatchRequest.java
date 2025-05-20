package com.matias.kreiman.nbastats.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PlayerStatsBatchRequest {

    @NotEmpty
    @Valid
    private List<PlayerStatDTO> stats;
}