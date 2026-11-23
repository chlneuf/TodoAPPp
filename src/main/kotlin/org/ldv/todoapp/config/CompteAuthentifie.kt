
package org.ldv.todoapp.config

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

class CompteUtilisateur(
    val idUtilisateur: Long,
    email: String,
    empreinte: String,
    role: String
) : User(email, empreinte, listOf(SimpleGrantedAuthority(role)))

