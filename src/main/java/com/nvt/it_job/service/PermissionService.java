package com.nvt.it_job.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.nvt.it_job.domain.Permission;
import com.nvt.it_job.domain.Skill;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.repository.PermissionRepository;

import jakarta.validation.Valid;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission permission) {
        return permissionRepository.existsByModuleAndApiPathAndMethod(permission.getModule(), permission.getApiPath(),
                permission.getMethod());
    }

    public Permission handleCreateSkill(Permission permission) {
        return permissionRepository.save(permission);
    }

    public Permission fetchPermissionById(long id) {
        return permissionRepository.findById(id).isPresent() ? permissionRepository.findById(id).get() : null;
    }

    public Permission handleUpdateSkill(Permission permission) {
        Permission currentPemission = this.permissionRepository.findById(permission.getId()).get();
        currentPemission.setName(permission.getName());
        currentPemission.setApiPath(permission.getApiPath());
        currentPemission.setMethod(permission.getMethod());
        currentPemission.setModule(permission.getModule());
        return permissionRepository.save(currentPemission);
    }

    public ResultPaginationDTO fetchAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pageUser = this.permissionRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);

        rs.setResult(pageUser.getContent());

        return rs;
    }

    public void delete(long id) {
        Optional<Permission> peOptional = this.permissionRepository.findById(id);
        Permission currentPermission = peOptional.get();
        currentPermission.getRoles().forEach(role -> role.getPermissions().remove(currentPermission));

        // delete skill
        this.permissionRepository.delete(currentPermission);
    }

    public boolean isSameName(Permission p) {
        Permission permissionDB = this.fetchPermissionById(p.getId());
        if (permissionDB != null) {
            if (permissionDB.getName().equals(p.getName()))
                return true;
        }
        return false;
    }
}
