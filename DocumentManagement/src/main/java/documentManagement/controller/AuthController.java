package documentManagement.controller;

import documentManagement.model.Role;
import documentManagement.model.UserEntity;
import documentManagement.repository.RoleRepository;
import documentManagement.repository.UserRepository;
import documentManagement.request.LoginRequest;
import documentManagement.security.JwtTokenProvider;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("userForm", new LoginRequest()); // Attach an empty form
        return "login"; // Return the name of your login.html
    }
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userForm") LoginRequest registrationData, Model model) { // changed from Map<String,String> to LoginRequest
        String username = registrationData.getUsername();
        String password = registrationData.getPassword();
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username is already taken");
            return "login";
        }
        UserEntity user = new UserEntity(username, passwordEncoder.encode(password));
        // Assign default role (e.g., "ROLE_VIEWER")
        Role viewerRole = roleRepository.findByName("ROLE_VIEWER");
        if (viewerRole == null) {
            viewerRole = new Role("ROLE_VIEWER");
            roleRepository.save(viewerRole);
        }
        user.getRoles().add(viewerRole);
        userRepository.save(user);
        model.addAttribute("message", "User registered successfully, Please Login");
        return "login"; //stay in login page
    }
    @PostMapping("/login")
    public String loginUser(@ModelAttribute("userForm") LoginRequest loginRequest, Model model) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.createToken(loginRequest.getUsername());
            log.info("token generation on login: {}", token);
            System.out.println("token generation on login: {}");
            System.out.println(token);
            List<String> rolesFromToken = jwtTokenProvider.getRolesFromToken(token);
            UserEntity user = userRepository.findByUsername(loginRequest.getUsername()).get();
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            //instead of returning json, add attributes.
            model.addAttribute("token", token);
            model.addAttribute("username", loginRequest.getUsername());
            model.addAttribute("roles", roles);
            // Set the token as a JavaScript variable in the HTML
            //response.addHeader("Set-Cookie", "jwtToken=" + token + "; Path=/; HttpOnly; Secure; SameSite=Strict");
            return "admin_dashboard"; //redirect to admin dashboard
        } catch (Exception e){
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }
//    @PostMapping("/logout")
//    public String logoutUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        SecurityContextHolder.clearContext(); // Use clearContext()
//        // Redirect to the login page after successful logout
//        response.sendRedirect("/auth/login?logout=true");
//        return null; // Important: Return null for Spring MVC to handle the redirection
//    }


    @GetMapping("/register")  // Added this handler
    public String showRegisterPage(Model model) {
        model.addAttribute("userForm", new LoginRequest());
        return "register";
    }


    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @ResponseBody
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @ResponseBody
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * API to modify the user's role based on a provided roleId.
     * Accessible by ADMIN and EDITOR roles.
     */
    @PutMapping("/user/{id}/role") // New API endpoint for updating user role
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')") // Only ADMIN or EDITOR can modify roles
    @ResponseBody
    public ResponseEntity<UserEntity> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest request) { // Use a specific DTO for role update

        Optional<UserEntity> existingUserOptional = userRepository.findById(id);
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.notFound().build(); // User not found
        }

        UserEntity userToUpdate = existingUserOptional.get();

        Optional<Role> roleOptional = roleRepository.findById(request.getRoleId());
        if (roleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Role not found
        }

        Role newRole = roleOptional.get();
        Set<Role> newRolesSet = new HashSet<>();
        newRolesSet.add(newRole); // Assuming a user can have one primary role or you want to replace all roles with this one
        userToUpdate.setRoles(newRolesSet); // Update the user's roles

        UserEntity updatedUser = userRepository.save(userToUpdate);
        return ResponseEntity.ok(updatedUser);
    }

    // DTO for the Update User Role request body
    @Data // Lombok annotation for getters/setters, equals, hashCode, and toString
    @Getter
    @Setter
    public static class UpdateUserRoleRequest {
        private Long roleId;
    }

}