package br.com.blupay.smesp.core.providers.token.wallet

import java.util.UUID

data class WalletTokenResponse(
        val party: String,
        val id: UUID,
        val alias: String,
        val publicKey: String
)

data class WalletResponse(
        val wallet: WalletTokenResponse,
        val roles: List<WalletRole>
)

data class BalanceResponse(
        val balance: Long
)