package com.nvt.it_job.controller;

import org.springframework.web.bind.annotation.RestController;

import com.nvt.it_job.domain.Company;
import com.nvt.it_job.domain.User;
import com.nvt.it_job.domain.response.ResCreateUserDTO;
import com.nvt.it_job.domain.response.ResUpdateUserDTO;
import com.nvt.it_job.domain.response.ResUserDTO;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.service.UserService;
import com.nvt.it_job.util.annotation.ApiMessage;
import com.nvt.it_job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("create user")
    public ResponseEntity<ResCreateUserDTO> crateUsser(@Valid @RequestBody User user) throws IdInvalidException {
        if (userService.isEmailExist(user.getEmail())) {
            throw new IdInvalidException("Email " + user.getEmail() + " đã tồn tại, vui lòng chọn email khác");
        }
        String hashPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPass);
        User newUser = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(newUser));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by id")
    public ResponseEntity<ResUserDTO> fetchUserById(@PathVariable("id") long id) throws IdInvalidException {
        if (userService.handleFetchUserById(id) == null) {
            throw new IdInvalidException("User id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(userService.convertToResUserDTO(userService.handleFetchUserById(id)));
    }

    @GetMapping("/users")
    @ApiMessage("Fetch All User")
    public ResponseEntity<ResultPaginationDTO> fetchAllUser(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.handleFetchAllUser(spec, pageable));
    }

    @PutMapping("/users")
    @ApiMessage("Update user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user) throws IdInvalidException {
        User newUser = this.userService.handleUpdateUser(user);
        if (newUser == null) {
            throw new IdInvalidException("User id = " + user.getId() + " không tồn tại");
        }
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(newUser));
    }

    @DeleteMapping("users/{id}")
    @ApiMessage("Delete User By Id")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        if (userService.handleFetchUserById(id) == null) {
            throw new IdInvalidException("User id = " + id + " không tồn tại");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

}
