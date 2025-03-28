package com.nvt.it_job.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.nvt.it_job.domain.Permission;
import com.nvt.it_job.domain.Role;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.repository.PermissionRepository;
import com.nvt.it_job.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role handleCreateRole(Role role) {
        // check permission
        if (role.getPermissions() != null) {
            List<Long> reqPermission = role.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermission = this.permissionRepository.findByIdIn(reqPermission);
            role.setPermissions(dbPermission);
        }
        // create role
        return this.roleRepository.save(role);

    }

    public Role fetchRoleById(long id) {
        return this.roleRepository.findById(id).isPresent() ? this.roleRepository.findById(id).get() : null;
    }

    public Role handleUpdateRole(Role r) {
        Role roleDB = this.fetchRoleById(r.getId());
        // check permissions
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }

        roleDB.setName(r.getName());
        roleDB.setDescription(r.getDescription());
        roleDB.setActive(r.isActive());
        roleDB.setPermissions(r.getPermissions());
        roleDB = this.roleRepository.save(roleDB);
        return roleDB;
    }

    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageUser = this.roleRepository.findAll(spec, pageable);

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

}
