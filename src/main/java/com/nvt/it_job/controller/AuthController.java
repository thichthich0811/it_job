package com.nvt.it_job.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvt.it_job.domain.User;
import com.nvt.it_job.domain.request.ReqLoginDTO;
import com.nvt.it_job.domain.response.ResCreateUserDTO;
import com.nvt.it_job.domain.response.ResLoginDTO;
import com.nvt.it_job.service.UserService;
import com.nvt.it_job.util.SecurityUtil;
import com.nvt.it_job.util.annotation.ApiMessage;
import com.nvt.it_job.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;
        @Value("${nvt.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;
        private final PasswordEncoder passwordEncoder;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                        UserService userService, PasswordEncoder passwordEncoder) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
        }

        @PostMapping("/auth/login")
        @ApiMessage("login")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                // xác thực người dùng => cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                // set thông tin người dùng đăng nhập vào context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO resLoginDTO = new ResLoginDTO();
                User currentUserDB = this.userService.handleFetchUserByUsername(loginDTO.getUsername());
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                                currentUserDB.getEmail(),
                                currentUserDB.getName(),
                                currentUserDB.getRole());
                resLoginDTO.setUser(userLogin);
                // create access token
                String access_token = this.securityUtil.createAccessToken(authentication.getName(), resLoginDTO);
                resLoginDTO.setAccessToken(access_token);

                // create refresh token
                String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), resLoginDTO);

                this.userService.updateUserToken(refresh_token, loginDTO.getUsername());

                // set cookies
                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(resLoginDTO);
        }

        @GetMapping("/auth/account")
        @ApiMessage("Fetch account")
        public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                User currentUserDb = this.userService.handleFetchUserByUsername(email);
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUserDb != null) {
                        userLogin.setId(currentUserDb.getId());
                        userLogin.setEmail(currentUserDb.getEmail());
                        userLogin.setName(currentUserDb.getName());
                        userGetAccount.setUser(userLogin);
                }
                return ResponseEntity.ok().body(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Refresh Token")
        public ResponseEntity<ResLoginDTO> refreshToken(
                        @CookieValue(value = "refresh_token", defaultValue = "abc") String refreshToken)
                        throws IdInvalidException {
                if (refreshToken.equals("abc")) {
                        throw new IdInvalidException("refreshToken khong co trong cookie");
                }
                // check valid
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
                String email = decodedToken.getSubject();

                // check user
                User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
                if (currentUser == null) {
                        throw new IdInvalidException("Refresh token invalid");
                }

                ResLoginDTO resLoginDTO = new ResLoginDTO();
                User currentUserDB = this.userService.handleFetchUserByUsername(email);
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                                currentUserDB.getEmail(),
                                currentUserDB.getName(),
                                currentUser.getRole());
                resLoginDTO.setUser(userLogin);
                // create access token
                String access_token = this.securityUtil.createAccessToken(email, resLoginDTO);
                resLoginDTO.setAccessToken(access_token);

                // create refresh token
                String new_refresh_token = this.securityUtil.createRefreshToken(email, resLoginDTO);

                this.userService.updateUserToken(new_refresh_token, email);

                // set cookies
                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", new_refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(resLoginDTO);
        }

        @PostMapping("/auth/register")
        @ApiMessage("Register a new user")
        public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User postManUser)
                        throws IdInvalidException {
                boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
                if (isEmailExist) {
                        throw new IdInvalidException(
                                        "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
                }

                String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
                postManUser.setPassword(hashPassword);
                User ericUser = this.userService.handleCreateUser(postManUser);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(this.userService.convertToResCreateUserDTO(ericUser));
        }

        @PostMapping("/auth/logout")
        @ApiMessage("Logout")
        public ResponseEntity<Void> logout() throws IdInvalidException {
                String email = SecurityUtil.getCurrentUserLogin().get();
                if (email.equals("")) {
                        throw new IdInvalidException("Access Token khong hop le");
                }
                // update refresh token=null
                this.userService.updateUserToken(null, email);
                // delete refresh token
                ResponseCookie deleteCookies = ResponseCookie
                                .from("refresh_token", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteCookies.toString())
                                .body(null);
        }

}
