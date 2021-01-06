package br.com.blupay.smesp.transactions

import br.com.blupay.blubasemodules.core.extensions.username
import br.com.blupay.smesp.core.resources.transactions.api.Cashin
import br.com.blupay.smesp.core.resources.transactions.api.PaymentByPin
import br.com.blupay.smesp.core.resources.transactions.models.CashinRequest
import br.com.blupay.smesp.core.resources.transactions.models.CashinResponse
import br.com.blupay.smesp.core.resources.transactions.models.PaymentByPinRequest
import br.com.blupay.smesp.core.resources.transactions.models.PaymentResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.annotation.security.RolesAllowed

@RestController
class TransactionController(
        private val service: TransactionService
) : PaymentByPin.Controller, Cashin.Controller {

    @RolesAllowed("ROLE_SELLER")
    override fun paymentByPin(requestBody: PaymentByPinRequest, auth: JwtAuthenticationToken): Mono<ResponseEntity<PaymentResponse>> {
        return service
                .paymentByPin(
                        auth.token.tokenValue,
                        auth.username,
                        requestBody.debtor,
                        requestBody.amount,
                        requestBody.pin
                )
                .map {
                    ResponseEntity.status(HttpStatus.CREATED).body(it)
                }
    }

    @RolesAllowed("ROLE_ADMIN")
    override fun cashin(requestBody: CashinRequest, auth: JwtAuthenticationToken): Mono<ResponseEntity<CashinResponse>> {
        return service
                .cashin(
                        auth.token.tokenValue,
                        auth.username,
                        requestBody.amount
                )
                .map {
                    ResponseEntity.status(HttpStatus.CREATED).body(it)
                }
    }
}
