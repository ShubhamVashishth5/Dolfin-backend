package com.shubhamvashishth.DolFin.controller

import com.shubhamvashishth.DolFin.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepository: UserRepository
) {


    @GetMapping("/{username}")
    fun doesUserExist(@PathVariable username: String) : Boolean {
        return userRepository.findByUsername(username) != null
    }

}