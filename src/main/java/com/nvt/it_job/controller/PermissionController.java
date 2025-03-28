package com.nvt.it_job.controller;

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

import com.nvt.it_job.domain.Permission;
import com.nvt.it_job.domain.Skill;
import com.nvt.it_job.domain.response.ResUserDTO;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.service.PermissionService;
import com.nvt.it_job.util.annotation.ApiMessage;
import com.nvt.it_job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    @ApiMessage("Create permission")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission)
            throws IdInvalidException {
        if (this.permissionService.isPermissionExist(permission)) {
            throw new IdInvalidException("Permission đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.handleCreateSkill(permission));
    }

    @PutMapping("/permissions")
    @ApiMessage("Update permission")
    public ResponseEntity<Permission> updatePermission(@RequestBody Permission permission) throws IdInvalidException {
        Permission currentPermission = this.permissionService.fetchPermissionById(permission.getId());
        if (currentPermission == null) {
            throw new IdInvalidException("Permission id = " + permission.getId() + " không tồn tại");
        }
        if (this.permissionService.isPermissionExist(permission)) {
            if (this.permissionService.isSameName(permission)) {
                throw new IdInvalidException("Permission đã tồn tại.");
            }
        }
        return ResponseEntity.ok().body(this.permissionService.handleUpdateSkill(permission));
    }

    @GetMapping("/permissions")
    @ApiMessage("fetch all permissions")
    public ResponseEntity<ResultPaginationDTO> getAllPermission(
            @Filter Specification<Permission> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(
                this.permissionService.fetchAllPermissions(spec, pageable));
    }

    @GetMapping("/permissions/{id}")
    @ApiMessage("Fetch permission by id")
    public ResponseEntity<Permission> fetchpermissionsById(@PathVariable("id") long id) throws IdInvalidException {
        if (permissionService.fetchPermissionById(id) == null) {
            throw new IdInvalidException("Permission id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok().body(permissionService.fetchPermissionById(id));
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete a permission")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        // check id
        if (permissionService.fetchPermissionById(id) == null) {
            throw new IdInvalidException("Permission id = " + id + " không tồn tại");
        }
        this.permissionService.delete(id);
        return ResponseEntity.ok().body(null);
    }
}
