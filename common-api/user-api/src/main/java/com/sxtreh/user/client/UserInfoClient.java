package com.sxtreh.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("user-service")
public interface UserInfoClient {
    @GetMapping("/users/info/{userId}")
    Long getUserRemainSpaceInfo(@PathVariable("userId") Long userId);
    @PostMapping("/users/netdisk/spaces")
    void modifyUserNetDiskSpace(@RequestParam Long userId, @RequestParam Long increaseSpace, @RequestParam String type);

}
