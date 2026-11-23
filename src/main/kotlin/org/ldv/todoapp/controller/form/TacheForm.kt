package org.ldv.todoapp.controller.form
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
class TacheForm {
    var titre: String = ""
    var description: String = ""
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var dateEcheance: LocalDate? = null

}