package com.campuseatery.controller;

import com.campuseatery.model.DeliveryAddress;
import com.campuseatery.service.DeliveryAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class DeliveryAddressController {

    private final DeliveryAddressService addressService;

    @GetMapping
    public ResponseEntity<List<DeliveryAddress>> getAddresses(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @PostMapping
    public ResponseEntity<DeliveryAddress> addAddress(@AuthenticationPrincipal Jwt jwt, @RequestBody DeliveryAddress address) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(addressService.addAddress(userId, address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryAddress> updateAddress(@AuthenticationPrincipal Jwt jwt, 
                                                         @PathVariable String id, 
                                                         @RequestBody DeliveryAddress address) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(addressService.updateAddress(userId, id, address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String userId = jwt.getSubject();
        addressService.deleteAddress(userId, id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<Void> setDefaultAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        String userId = jwt.getSubject();
        addressService.setDefaultAddress(userId, id);
        return ResponseEntity.ok().build();
    }
}
