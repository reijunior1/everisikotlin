package br.com.blupay.smesp.citizens

import br.com.blupay.blubasemodules.core.exceptions.BaseExceptionType
import br.com.blupay.blubasemodules.core.exceptions.BaseRuntimeException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class CitizenPermissionException(message: String? = null) : BaseRuntimeException(
    message = message ?: "Permission denied",
    errors = linkedMapOf(),
    type = BaseExceptionType.INVALID_REQUEST_ERROR,
    category = "PERMISSION_DENIED"
)