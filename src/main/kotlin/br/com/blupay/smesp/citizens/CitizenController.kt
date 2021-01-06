package br.com.blupay.smesp.citizens

import br.com.blupay.blubasemodules.core.extensions.authCredentials
import br.com.blupay.smesp.core.resources.citizens.api.CitizenCreate
import br.com.blupay.smesp.core.resources.citizens.api.CitizenImport
import br.com.blupay.smesp.core.resources.citizens.api.CitizenRead
import br.com.blupay.smesp.core.resources.citizens.api.CitizenStatus
import br.com.blupay.smesp.core.resources.citizens.models.CitizenResponse
import br.com.blupay.smesp.core.resources.citizens.models.CitizenStatusResponse
import br.com.blupay.smesp.core.resources.shared.models.PasswordRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import java.util.UUID
import javax.annotation.security.RolesAllowed

@RestController
class CitizenController(
        val citizenService: CitizenService,
        private val citizenImportService: CitizenImportService
) : CitizenCreate.Controller,
    CitizenRead.Controller,
    CitizenImport.Controller,
    CitizenStatus.Controller {

    @RolesAllowed("ROLE_CITIZEN")
    override fun findOne(citizenId: UUID, auth: JwtAuthenticationToken): ResponseEntity<CitizenResponse> {
        val data = citizenService.findOne(citizenId, auth.authCredentials)
        return ResponseEntity.ok(data)
    }

    @RolesAllowed("ROLE_GUEST", "ROLE_CITIZEN")
    override fun findOneByCpf(cpf: String, auth: JwtAuthenticationToken): ResponseEntity<CitizenResponse> {
        val citizen = citizenService.findOneByCpf(cpf, auth.authCredentials)
        return ResponseEntity.ok(citizen)
    }

    @RolesAllowed("ROLE_GUEST", "ROLE_CITIZEN")
    override fun createCredentials(citizenId: UUID,
                                   request: PasswordRequest,
                                   auth: JwtAuthenticationToken): ResponseEntity<CitizenResponse> {
        val citizen = citizenService.createCredentials(citizenId, request, auth.authCredentials)
        return ResponseEntity.status(HttpStatus.CREATED).body(citizen)
    }

    override fun importCsv(file: MultipartFile) {
        citizenImportService.parseToModel(file)
    }

    @RolesAllowed("ROLE_CITIZEN")
    override fun checkStatus(citizenId: UUID, auth: JwtAuthenticationToken): Mono<ResponseEntity<CitizenStatusResponse>> {
        return citizenService.checkStatus(citizenId, auth.authCredentials)
            .map { response -> ResponseEntity.ok(response) }
    }
}