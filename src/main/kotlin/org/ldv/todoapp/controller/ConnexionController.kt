package org.ldv.todoapp.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ConnexionController {

    @GetMapping("/login")
    fun connexion(): String {
        return "connexion"
    }
}