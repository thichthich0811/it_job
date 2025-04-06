package com.nvt.it_job.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvt.it_job.domain.Job;
import com.nvt.it_job.domain.Role;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.domain.response.job.ResCreateJobDTO;
import com.nvt.it_job.domain.response.job.ResUpdateJobDTO;
import com.nvt.it_job.service.RoleService;
import com.nvt.it_job.util.annotation.ApiMessage;
import com.nvt.it_job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @ApiMessage("Create a roles")
    public ResponseEntity<Role> create(@Valid @RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.roleService.handleCreateRole(role));
    }

    @PutMapping("/roles")
    @ApiMessage("Update a role")
    public ResponseEntity<Role> update(@Valid @RequestBody Role role) throws IdInvalidException {
        Role currentRole = this.roleService.fetchRoleById(role.getId());
        if (currentRole == null) {
            throw new IdInvalidException("Role not found");
        }

        return ResponseEntity.ok()
                .body(this.roleService.handleUpdateRole(role));
    }

    @GetMapping("/roles/{id}")
    @ApiMessage("Get a role by id")
    public ResponseEntity<Role> getRole(@PathVariable("id") long id) throws IdInvalidException {
        Role currentRole = this.roleService.fetchRoleById(id);
        if (currentRole == null) {
            throw new IdInvalidException("Role not found");
        }

        return ResponseEntity.ok().body(currentRole);
    }

    @GetMapping("/roles")
    @ApiMessage("Get roles with pagination")
    public ResponseEntity<ResultPaginationDTO> getAllJob(
            @Filter Specification<Role> spec,
            Pageable pageable) {

        return ResponseEntity.ok().body(this.roleService.fetchAll(spec, pageable));
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Delete a role by id")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        Role currentRole = this.roleService.fetchRoleById(id);
        if (currentRole == null) {
            throw new IdInvalidException("Role not found");
        }
        this.roleService.delete(id);
        return ResponseEntity.ok().body(null);
    }

}
