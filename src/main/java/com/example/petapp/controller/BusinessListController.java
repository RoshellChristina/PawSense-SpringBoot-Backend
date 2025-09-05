package com.example.petapp.controller;

import com.example.petapp.dto.UserDTO;
import com.example.petapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class BusinessListController {

    private final UserService userService;

    @Autowired
    public BusinessListController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/businesses?businessType=...&limit=20&offset=0
     */
    @GetMapping("/businesses")
    public List<UserDTO> listBusinesses(
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        return userService.listBusinesses(businessType, limit, offset);
    }

    /**
     * GET /api/users/business-types
     */
    @GetMapping("/users/business-types")
    public List<String> businessTypes() {
        return userService.getBusinessTypes();
    }
}
