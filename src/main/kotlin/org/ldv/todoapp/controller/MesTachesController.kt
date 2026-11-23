
package org.ldv.todoapp.controller

import org.ldv.todoapp.controller.form.TacheForm
import org.ldv.todoapp.model.entity.Tache
import org.ldv.todoapp.model.entity.Utilisateur
import org.ldv.todoapp.model.repository.TacheRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class MesTachesController(
    private val tacheRepository: TacheRepository
) {

    // 1. Afficher les tâches de l'utilisateur connecté
    @GetMapping("/mes-taches")
    fun mesTaches(
        model: Model,
        @RequestAttribute("utilisateurActif", required = false)
        utilisateurActif: Utilisateur?
    ): String {

        val utilisateurId = utilisateurActif?.idUtilisateur

        val taches = if (utilisateurId != null) {
            tacheRepository.findByUtilisateur_IdUtilisateur(utilisateurId)
        } else {
            emptyList()
        }

        model.addAttribute("taches", taches)

        return "mes-taches"
    }

    // 2. Afficher le formulaire de création
    @GetMapping("/mes-taches/creer")
    fun formulaireCreation(
        model: Model,
        @RequestAttribute("utilisateurActif", required = false)
        utilisateurActif: Utilisateur?,
        redirectAttributes: RedirectAttributes
    ): String {

        if (utilisateurActif == null) {
            redirectAttributes.addFlashAttribute(
                "messageErreur",
                "Connectez-vous avant de créer une tâche."
            )

            return "redirect:/mes-taches"
        }

        model.addAttribute("tacheForm", TacheForm())

        return "mes-taches/creer"
    }

    // 3. Enregistrer une nouvelle tâche
    @PostMapping("/mes-taches/creer")
    fun creerTache(
        @ModelAttribute("tacheForm") formulaire: TacheForm,
        bindingResult: BindingResult,
        @RequestAttribute("utilisateurActif", required = false)
        utilisateurActif: Utilisateur?,
        redirectAttributes: RedirectAttributes
    ): String {

        // Vérification du titre
        if (formulaire.titre.isBlank()) {
            bindingResult.rejectValue(
                "titre",
                "titre.obligatoire",
                "Le titre est obligatoire."
            )
        } else if (formulaire.titre.length > 255) {
            bindingResult.rejectValue(
                "titre",
                "titre.longueur",
                "Le titre ne doit pas dépasser 255 caractères."
            )
        }

        // Vérification des erreurs
        if (bindingResult.hasErrors()) {
            return "mes-taches/creer"
        }

        // Vérification de l'utilisateur connecté
        if (utilisateurActif == null) {
            redirectAttributes.addFlashAttribute(
                "messageErreur",
                "Connectez-vous avant de créer une tâche."
            )

            return "redirect:/mes-taches"
        }

        // Création de la tâche
        val tache = Tache(
            titre = formulaire.titre.trim(),
            description = formulaire.description,
            dateEcheance = formulaire.dateEcheance,
            utilisateur = utilisateurActif
        )

        // Enregistrement dans la base de données
        tacheRepository.save(tache)

        // Message de confirmation
        redirectAttributes.addFlashAttribute(
            "messageSucces",
            "Votre tâche a été créée avec succès."
        )

        return "redirect:/mes-taches"
    }
}
