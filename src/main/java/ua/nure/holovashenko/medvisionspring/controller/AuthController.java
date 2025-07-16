package ua.nure.holovashenko.medvisionspring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Реєстрація пацієнта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішна реєстрація"),
            @ApiResponse(responseCode = "409", description = "Користувач з таким email вже існує")
    })
    @PostMapping("/register/patient")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody PatientRegisterRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @Operation(summary = "Реєстрація адміністратора системи")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішна реєстрація"),
            @ApiResponse(responseCode = "409", description = "Користувач з таким email вже існує")
    })
    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @Operation(summary = "Авторизація користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішна авторизація"),
            @ApiResponse(responseCode = "401", description = "Невірний email або пароль"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Отримання профілю авторизованого користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профіль знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено або відсутні розширені дані"),
            @ApiResponse(responseCode = "400", description = "Невідома роль користувача"),
            @ApiResponse(responseCode = "401", description = "Неавторизований запит")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(authService.getProfile(userDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("{\"Помилка при отриманні профілю\":\"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/edit/doctor")
    public ResponseEntity<?> editDoctorProfile(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody DoctorEditRequest request) {
        return ResponseEntity.ok(authService.editDoctorProfile(userDetails, request));
    }

    @PutMapping("/edit/patient")
    public ResponseEntity<?> editPatientProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody PatientEditRequest request) {
        return ResponseEntity.ok(authService.editPatientProfile(userDetails, request));
    }

}
