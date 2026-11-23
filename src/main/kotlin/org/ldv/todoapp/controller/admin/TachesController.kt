
package org.ldv.todoapp.controller.admin

import org.ldv.todoapp.controller.form.TacheAdminForm
import org.ldv.todoapp.model.entity.Tache
import org.ldv.todoapp.model.entity.Utilisateur
import org.ldv.todoapp.model.repository.TacheRepository
import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class TachesController(
    private val tacheRepository: TacheRepository,
    private val utilisateurRepository: UtilisateurRepository
) {

    // Afficher toutes les tâches
    @GetMapping("/admin/taches")
    fun taches(model: Model): String {
        model.addAttribute("listeTaches", tacheRepository.findAllAvecUtilisateur())
        return "admin/taches"
    }

    // Afficher le formulaire de création
    @GetMapping("/admin/taches/creer")
    fun formulaireCreation(model: Model): String {
        model.addAttribute("tacheForm", TacheAdminForm())
        preparerFormulaire(model)
        return "admin/formulaire-tache"
    }

    // Enregistrer une nouvelle tâche
    @PostMapping("/admin/taches/creer")
    fun creerTache(
        @ModelAttribute("tacheForm") formulaire: TacheAdminForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val proprietaire = validerFormulaire(formulaire, bindingResult)

        if (bindingResult.hasErrors()) {
            preparerFormulaire(model)
            return "admin/formulaire-tache"
        }

        val tache = Tache(
            titre = formulaire.titre.trim(),
            description = formulaire.description.trim().ifBlank { null },
            dateEcheance = formulaire.dateEcheance,
            utilisateur = requireNotNull(proprietaire)
        )

        tacheRepository.save(tache)
        redirectAttributes.addFlashAttribute(
            "messageSucces",
            "La tâche a bien été créée."
        )
        return "redirect:/admin/taches"
    }

    // Afficher le formulaire de modification
    @GetMapping("/admin/taches/{id}/modifier")
    fun formulaireModification(
        @PathVariable("id") id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val tache = tacheRepository.findByIdAvecUtilisateur(id)
        if (tache == null) {
            return signalerTacheAbsente(redirectAttributes)
        }

        val formulaire = TacheAdminForm()
        formulaire.titre = tache.titre
        formulaire.description = tache.description ?: ""
        formulaire.dateEcheance = tache.dateEcheance
        formulaire.utilisateurId = tache.utilisateur?.idUtilisateur

        model.addAttribute("tacheForm", formulaire)
        preparerFormulaire(model, id)

        return "admin/formulaire-tache"
    }

    // Enregistrer les modifications d'une tâche
    @PostMapping("/admin/taches/{id}/modifier")
    fun modifierTache(
        @PathVariable("id") id: Long,
        @ModelAttribute("tacheForm") formulaire: TacheAdminForm,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val tache = tacheRepository.findByIdAvecUtilisateur(id)
        if (tache == null) {
            return signalerTacheAbsente(redirectAttributes)
        }

        val proprietaire = validerFormulaire(formulaire, bindingResult)

        if (bindingResult.hasErrors()) {
            preparerFormulaire(model, id)
            return "admin/formulaire-tache"
        }

        tache.titre = formulaire.titre.trim()
        tache.description = formulaire.description.trim().ifBlank { null }
        tache.dateEcheance = formulaire.dateEcheance
        tache.utilisateur = requireNotNull(proprietaire)

        tacheRepository.save(tache)

        redirectAttributes.addFlashAttribute(
            "messageSucces",
            "La tâche a bien été modifiée."
        )
        return "redirect:/admin/taches"
    }

    // Afficher la confirmation de suppression
    @GetMapping("/admin/taches/{id}/supprimer")
    fun confirmationSuppression(
        @PathVariable("id") id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val tache = tacheRepository.findByIdAvecUtilisateur(id)
        if (tache == null) {
            return signalerTacheAbsente(redirectAttributes)
        }

        model.addAttribute("tacheASupprimer", tache)
        return "admin/confirmation-suppression-tache"
    }

    // Supprimer une tâche
    @PostMapping("/admin/taches/{id}/supprimer")
    fun supprimerTache(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val tache = tacheRepository.findByIdAvecUtilisateur(id)
        if (tache == null) {
            return signalerTacheAbsente(redirectAttributes)
        }

        tacheRepository.delete(tache)

        redirectAttributes.addFlashAttribute(
            "messageSucces",
            "La tâche a bien été supprimée."
        )
        return "redirect:/admin/taches"
    }

    // Préparer les données nécessaires au formulaire
    private fun preparerFormulaire(model: Model, idTache: Long? = null) {
        model.addAttribute("listeUtilisateurs", utilisateurRepository.findAllAvecRole())
        model.addAttribute("idTache", idTache)
    }

    // Vérifier les données du formulaire
    private fun validerFormulaire(
        formulaire: TacheAdminForm,
        erreurs: BindingResult
    ): Utilisateur? {
        if (formulaire.titre.isBlank()) {
            erreurs.rejectValue(
                "titre",
                "titre.obligatoire",
                "Le titre est obligatoire."
            )
        } else if (formulaire.titre.length > 255) {
            erreurs.rejectValue(
                "titre",
                "titre.longueur",
                "Le titre ne doit pas dépasser 255 caractères."
            )
        }

        val utilisateur = formulaire.utilisateurId?.let {
            utilisateurRepository.findByIdAvecRole(it)
        }

        if (utilisateur == null) {
            erreurs.rejectValue(
                "utilisateurId",
                "utilisateur.obligatoire",
                "Sélectionnez un propriétaire valide."
            )
        }

        return utilisateur
    }

    // Signaler qu'une tâche demandée n'existe pas
    private fun signalerTacheAbsente(
        redirectAttributes: RedirectAttributes
    ): String {
        redirectAttributes.addFlashAttribute(
            "messageErreur",
            "La tâche demandée est introuvable."
        )
        return "redirect:/admin/taches"
    }
}
