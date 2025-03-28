package com.nvt.it_job.controller;

import java.util.List;

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

import com.nvt.it_job.domain.Company;
import com.nvt.it_job.domain.Skill;
import com.nvt.it_job.domain.User;
import com.nvt.it_job.domain.response.ResUpdateUserDTO;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.service.SkillService;
import com.nvt.it_job.util.annotation.ApiMessage;
import com.nvt.it_job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/skills")
    @ApiMessage("Create skills")
    public ResponseEntity<Skill> createSkill(@Valid @RequestBody Skill skill) throws IdInvalidException {
        if (this.skillService.isNameExist(skill.getName())) {
            throw new IdInvalidException("Skill " + skill.getName() + " đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.skillService.handleCreateSkill(skill));
    }

    @PutMapping("/skills")
    @ApiMessage("Update skill")
    public ResponseEntity<Skill> updateSkill(@RequestBody Skill skill) throws IdInvalidException {
        Skill currentSkill = this.skillService.fetchSkillById(skill.getId());
        if (currentSkill == null) {
            throw new IdInvalidException("Skill id = " + skill.getId() + " không tồn tại");
        }
        if (this.skillService.isNameExist(skill.getName())) {
            throw new IdInvalidException("Skill " + skill.getName() + " đã tồn tại");
        }
        currentSkill.setName(skill.getName());
        return ResponseEntity.ok().body(this.skillService.handleCreateSkill(currentSkill));
    }

    @GetMapping("/skills")
    @ApiMessage("fetch all skills")
    public ResponseEntity<ResultPaginationDTO> getAllSkill(
            @Filter Specification<Skill> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(
                this.skillService.fetchAllSkills(spec, pageable));
    }

    @DeleteMapping("/skills/{id}")
    @ApiMessage("Delete a skill")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        // check id
        Skill currentSkill = this.skillService.fetchSkillById(id);
        if (currentSkill == null) {
            throw new IdInvalidException("Skill id = " + id + " không tồn tại");
        }
        this.skillService.deleteSkill(id);
        return ResponseEntity.ok().body(null);
    }
}
