package br.com.blupay.smesp.core.providers.identity

import br.com.blupay.blubasemodules.identity.people.PersonCredentials
import br.com.blupay.blubasemodules.identity.people.PersonSearch
import br.com.blupay.blubasemodules.identity.validations.ValidationCheckStatus
import br.com.blupay.blubasemodules.shared.validations.enums.ValidationType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentityProvider(
        private val identityClient: IdentityClient
) {
    private val logger: Logger = LoggerFactory.getLogger(IdentityProvider::class.java)

    fun createPersonCredentials(token: String, personId: UUID, body: PersonCredentials.Request): Boolean {
        logger.info("Creating credentials for person $personId, into groups ${body.groups}")
        return identityClient.createPersonCredentials(token, personId, body).block()
                ?: throw IdentityException("Credentials could not be created")
    }

    fun peopleSearch(token: String, query: PersonSearch.Query): List<PersonSearch.Response> {
        logger.info("Getting data from Identity by Register (CPF or CNPJ)")
        val personList = identityClient.peopleSearch(token, query)?.block()
                ?: throw IdentityException("Couldn't get a list of people")
        return personList.data
    }

    fun verifyRules(token: String, typeRules: List<ValidationType>, personId: UUID? = null): Mono<ValidationCheckStatus.ValidationRuleResponse> {
        logger.info("Getting validations status rules")
        return identityClient.verifyRules(token, typeRules, personId)
    }
}