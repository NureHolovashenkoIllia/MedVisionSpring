package ua.nure.holovashenko.medvisionspring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.ChangeUserRoleRequest;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.service.UserService;

import java.util.List;

/**
 * Контролер для адміністрування користувачів системи (тільки для ADMIN).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Отримати користувача за його ID.
     * Доступно лише адміністратору.
     *
     * @param id ID користувача
     * @return Деталі користувача або 404, якщо не знайдено
     */
    @Operation(summary = "Отримати користувача за ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувача знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено"),
            @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Отримати список усіх користувачів у системі.
     * Доступно лише адміністратору.
     *
     * @return Список користувачів
     */
    @Operation(summary = "Отримати список усіх користувачів")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список користувачів повернуто"),
            @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Видалити користувача за ID.
     * Доступно лише адміністратору.
     *
     * @param id ID користувача
     * @return 204 No Content при успішному видаленні або 404, якщо не знайдено
     */
    @Operation(summary = "Видалити користувача за ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Користувача видалено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено"),
            @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Змінити роль користувача.
     *
     * @param id      ідентифікатор користувача
     * @param request запит з новою роллю
     */
    @Operation(summary = "Змінити роль користувача", description = "Видаляє стару роль (якщо пацієнт/лікар) і додає нову")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Роль змінено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable Long id,
            @RequestBody ChangeUserRoleRequest request
    ) {
        userService.changeUserRole(id, request.getNewRole());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
