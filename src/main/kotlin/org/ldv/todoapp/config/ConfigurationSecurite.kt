
package org.ldv.todoapp.config

import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class ConfigurationSecurite(
    private val utilisateurRepository: UtilisateurRepository
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .authorizeHttpRequests { acces ->

                // Accès publics
                acces.requestMatchers(
                    "/",
                    "/css/**",
                    "/fonts/**",
                    "/images/**",
                    "/vendor/**"
                ).permitAll()

                // Accès réservé aux administrateurs
                acces.requestMatchers("/admin/**").access { authentification, _ ->

                    val compte = authentification.get().principal as? CompteAuthentifie

                    // Le rôle en base peut changer pendant la session.
                    val role = compte?.let {
                        utilisateurRepository.findByIdAvecRole(it.idUtilisateur)?.role?.nom
                    }

                    AuthorizationDecision(role == "ROLE_ADMIN")
                }

                // Accès réservé aux utilisateurs connectés
                acces.requestMatchers(
                    "/mes-infos",
                    "/mes-taches/**"
                ).authenticated()

                // Toutes les autres routes nécessitent une authentification
                acces.anyRequest().authenticated()
            }
            .formLogin { connexion ->
                connexion.loginPage("/login").permitAll()
            }
            .logout { deconnexion ->
                deconnexion.logoutSuccessUrl("/login?logout")
            }

        return http.build()
    }
}
