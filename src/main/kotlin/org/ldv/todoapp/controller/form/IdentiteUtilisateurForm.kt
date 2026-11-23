package org.ldv.todoapp.controller.form

import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.validation.BindingResult

open class IdentiteUtilisateurForm {
    var nom: String = ""
    var prenom: String = ""
    var email: String = ""
}

internal fun IdentiteUtilisateurForm.validerIdentite(
    erreurs: BindingResult,
    utilisateurRepository: UtilisateurRepository,
    id: Long? = null
) {
    for ((champ, valeur) in listOf("nom" to nom, "prenom" to prenom)) {
        if (valeur.isBlank()) {
            erreurs.rejectValue(champ, "$champ.obligatoire", "Ce champ est obligatoire.")
        } else if (valeur.length > 100) {
            erreurs.rejectValue(champ, "$champ.longueur", "100 caractères maximum.")
        }
    }

    val adresse = email.trim()
    if (adresse.length > 255 || !Regex("^[^\\s@]+@[^\\s@]+$").matches(adresse)) {
        erreurs.rejectValue(
            "email",
            "email.invalide",
            "Saisissez une adresse électronique valide (255 caractères maximum)."
        )
    } else {
        val existant = utilisateurRepository.findByEmail(adresse)
        if (existant != null && existant.idUtilisateur != id) {
            erreurs.rejectValue(
                "email",
                "email.unique",
                "Cette adresse électronique est déjà utilisée."
            )
        }
    }
}