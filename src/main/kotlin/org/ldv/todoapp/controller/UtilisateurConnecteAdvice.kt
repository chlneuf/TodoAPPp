
package org.ldv.todoapp.controller

import jakarta.servlet.http.HttpServletRequest
import org.ldv.todoapp.config.CompteAuthentifie
import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class UtilisateurConnecteAdvice(
    private val utilisateurRepository: UtilisateurRepository
) {
    @ModelAttribute
    fun ajouterUtilisateurActif(
        @AuthenticationPrincipal compte: CompteAuthentifie?,
        requete: HttpServletRequest,
        model: Model
    ) {
        val utilisateur = compte?.let {
            utilisateurRepository.findByIdAvecRole(it.idUtilisateur)
        }

        requete.setAttribute("utilisateurActif", utilisateur)
        model.addAttribute("utilisateurActif", utilisateur)
    }
}
