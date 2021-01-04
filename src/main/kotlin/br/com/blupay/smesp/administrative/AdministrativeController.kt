package br.com.blupay.smesp.administrative

import br.com.blupay.smesp.core.resources.administrative.api.AdministrativeCreate
import br.com.blupay.smesp.core.resources.administrative.models.WalletAdminResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.annotation.security.RolesAllowed

@RestController
class AdministrativeController(
        val service: AdministrativeService
) : AdministrativeCreate.Controller {

    @RolesAllowed("ROLE_ADMIN")
    override fun create(auth: JwtAuthenticationToken): Mono<ResponseEntity<List<WalletAdminResponse>>> {
        return service
                .create(auth.token.tokenValue)
                .map {
                    ResponseEntity.status(HttpStatus.CREATED).body(it)
                }
    }
}