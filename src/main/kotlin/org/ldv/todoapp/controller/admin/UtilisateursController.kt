package org.ldv.todoapp.controller.admin

import org.ldv.todoapp.controller.form.UtilisateurForm
import org.ldv.todoapp.controller.form.validerIdentite
import org.ldv.todoapp.model.entity.Role
import org.ldv.todoapp.model.repository.RoleRepository
import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.ldv.todoapp.model.entity.Utilisateur

@Controller
class UtilisateursController(
    private val utilisateurRepository: UtilisateurRepository,
    private val roleRepository: RoleRepository
) {

    @GetMapping("/admin/utilisateurs")
    fun utilisateurs(model: Model): String {
        model.addAttribute(
            "listeUtilisateurs",
            utilisateurRepository.findAllAvecRole()
        )
        return "admin/utilisateurs"
    }

    @GetMapping("/admin/utilisateurs/creer")
    fun formulaireCreation(model: Model): String {
        model.addAttribute("utilisateurForm", UtilisateurForm())
        preparerFormulaire(model)
        return "admin/formulaire-utilisateur"
    }
    @PostMapping("/admin/utilisateurs/creer")
    fun creerUtilisateur(
        @ModelAttribute("utilisateurForm") formulaire: UtilisateurForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val role = validerFormulaire(formulaire, bindingResult)

        if (!bindingResult.hasErrors()) {
            val utilisateur = Utilisateur(
                nom = formulaire.nom.trim(),
                prenom = formulaire.prenom.trim(),
                email = formulaire.email.trim(),
                role = requireNotNull(role)
            )

            utilisateurRepository.save(utilisateur)

            redirectAttributes.addFlashAttribute(
                "messageSucces",
                "L'utilisateur a bien été créé."
            )
            return "redirect:/admin/utilisateurs"
        }

        preparerFormulaire(model)
        return "admin/formulaire-utilisateur"
    }
    private fun preparerFormulaire(model: Model, idUtilisateur: Long? = null) {
        model.addAttribute("listeRoles", roleRepository.findAll())
        model.addAttribute("idUtilisateur", idUtilisateur)
    }

    private fun validerFormulaire(
        formulaire: UtilisateurForm,
        erreurs: BindingResult,
        id: Long? = null
    ): Role? {
        formulaire.validerIdentite(erreurs, utilisateurRepository, id)

        val roleId = formulaire.roleId
        if (roleId == null) {
            erreurs.rejectValue("roleId", "role.obligatoire", "Sélectionnez un rôle.")
            return null
        }

        val role = roleRepository.findById(roleId).orElse(null)
        if (role == null) {
            erreurs.rejectValue("roleId", "role.invalide", "Le rôle sélectionné est invalide.")
            return null
        }

        return role
    }
}