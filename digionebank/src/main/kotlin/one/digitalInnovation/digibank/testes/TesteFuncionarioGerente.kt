package one.digitalInnovation.digibank.testes

import one.digitalInnovation.digibank.Funcionario
import one.digitalInnovation.digibank.Gerente

fun main(){
    val reinaldoCouto = Gerente("Reinaldo Couto", "21122233344",2000.0)
    ImprimeRelatorioFuncionario.imprime(reinaldoCouto)

    }