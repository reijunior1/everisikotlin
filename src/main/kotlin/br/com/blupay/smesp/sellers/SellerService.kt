package br.com.blupay.smesp.sellers

import br.com.blupay.blubasemodules.core.models.AuthCredentials
import br.com.blupay.blubasemodules.identity.people.PersonCredentials
import br.com.blupay.blubasemodules.identity.people.PersonSearch
import br.com.blupay.smesp.core.drivers.EncoderManager
import br.com.blupay.smesp.core.providers.identity.IdentityProvider
import br.com.blupay.smesp.core.providers.token.wallet.IssueWallet
import br.com.blupay.smesp.core.resources.sellers.api.SellerBankAccount
import br.com.blupay.smesp.core.resources.sellers.exceptions.SellerException
import br.com.blupay.smesp.core.resources.sellers.exceptions.SellerNotFoundException
import br.com.blupay.smesp.core.resources.sellers.models.BankResponse
import br.com.blupay.smesp.core.resources.sellers.models.SellerResponse
import br.com.blupay.smesp.core.resources.shared.enums.OnboardFlow
import br.com.blupay.smesp.core.resources.shared.enums.OnboardFlow.VALIDATION
import br.com.blupay.smesp.core.resources.shared.enums.UserGroups
import br.com.blupay.smesp.core.resources.shared.enums.UserTypes.SELLER
import br.com.blupay.smesp.core.resources.shared.models.PasswordRequest
import br.com.blupay.smesp.core.services.JwsService
import br.com.blupay.smesp.core.services.OwnerService
import br.com.blupay.smesp.sellers.banks.BankAccount
import br.com.blupay.smesp.sellers.banks.BankAccountRepository
import br.com.blupay.smesp.token.TokenWalletService
import br.com.blupay.smesp.wallets.Wallet
import br.com.blupay.smesp.wallets.WalletRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID
import javax.security.auth.login.CredentialException

@Service
class SellerService(
    private val bankAccountRepository: BankAccountRepository,
    private val sellerRepository: SellerRepository,
    private val identityProvider: IdentityProvider,
    private val encoderManager: EncoderManager,
    private val tokenWalletService: TokenWalletService,
    private val walletRepository: WalletRepository,
    private val jwsService: JwsService,
    private val ownerService: OwnerService
) {

    fun createCredentials(sellerId: UUID, request: PasswordRequest, jwt: Jwt): SellerResponse {
        val token = jwt.tokenValue
        val seller = findById(sellerId)

        if (OnboardFlow.CREDENTIALS != seller.flow) {
            throw SellerException("This seller $sellerId already has a credential")
        }

        val credentialsCreated = identityProvider.createPersonCredentials(
                token, sellerId, PersonCredentials.Request(
                password = request.password,
                groups = listOf("${UserGroups.SELLERS}")
        )
        )

        if (!credentialsCreated) {
            throw CredentialException("Citizen $sellerId credentials were not created")
        }

        val updatedSeller = sellerRepository.save(seller.copy(flow = VALIDATION))
        return createSellerCryptResponse(updatedSeller)
    }

    fun findOneByCnpj(cnpj: String, jwt: Jwt): SellerResponse {

        val token = jwt.tokenValue
        val seller = sellerRepository.findByCnpj(cnpj)

        if (seller != null) {
            return createSellerCryptResponse(seller)
        }

        val sellerList = identityProvider.peopleSearch(token, PersonSearch.Query(register = cnpj))

        val person = sellerList.stream().findFirst().orElseThrow { throw SellerNotFoundException(cnpj) }
        val sellerSaved = sellerRepository.save(
                Seller(
                        id = person.id,
                        name = person.name,
                        cnpj = person.register,
                        email = person.email,
                        phone = person.phone,
                        banks = listOf()
                )
        )

        val pairKeys = jwsService.getKeyPairEncoded()

        val wallet = tokenWalletService.issueWallet(
                jwt.tokenValue,
                IssueWallet(sellerSaved.id.toString(), pairKeys.publicKey.toString(), Wallet.Role.RECEIVER)
        ).block()
        walletRepository.save(
                Wallet(
                        UUID.randomUUID(),
                        sellerSaved.id!!,
                        wallet?.id!!,
                        SELLER,
                        Wallet.Role.RECEIVER,
                        pairKeys.publicKey.toString(),
                        pairKeys.privateKey.toString()
                )
        )

        return createSellerCryptResponse(sellerSaved)

    }

    fun findOne(sellerId: UUID, auth: AuthCredentials): SellerResponse {
        ownerService.userOwns(auth, sellerId)

        val seller = findByCnpj(auth.username)
        val wallet = walletRepository.findByOwner(seller.id!!)
        return createSellerCryptResponse(seller, wallet?.id!!)
    }

    fun findById(sellerId: UUID) = sellerRepository.findByIdOrNull(sellerId)
            ?: throw SellerNotFoundException("$sellerId")

    fun findByCnpj(cnpj: String) = sellerRepository.findByCnpj(cnpj)
            ?: throw SellerNotFoundException(cnpj)

    fun createBankAccount(sellerId: UUID, requestBody: SellerBankAccount.Request): BankResponse? {
        val seller = findById(sellerId)
        val bankAccountSaved = bankAccountRepository.save(
            BankAccount(
                name = requestBody.name,
                cnpj = seller.cnpj,
                agency = requestBody.agency,
                account = requestBody.account,
                seller = seller,
                pix = requestBody.pix
        ))
        return createBankResponse(bankAccountSaved)
    }

    fun findBankAccounts(sellerId: UUID): List<BankResponse> {
        val banks = findById(sellerId).banks ?: listOf()
        return banks.map { createBankResponse(it) }
    }

    private fun createBankResponse(bankAccount: BankAccount) = BankResponse(
            bankAccount.id,
            bankAccount.name,
            bankAccount.cnpj,
            bankAccount.agency,
            bankAccount.account,
            bankAccount.pix
    )

    private fun createSellerCryptResponse(seller: Seller, walletId: UUID? = null): SellerResponse {
        return SellerResponse(
            id = seller.id!!,
            name = seller.name,
            cnpj = seller.cnpj,
            email = encoderManager.encrypt(seller.email),
            phone = encoderManager.encrypt(seller.phone),
            flow = seller.flow,
            walletId = walletId
        )
    }
}