package no.nav.paw.arbeidssoekerregisteret.api.oppslag.services

import no.nav.paw.arbeidssoekerregisteret.api.oppslag.models.OpplysningerOmArbeidssoekerResponse
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.repositories.OpplysningerOmArbeidssoekerRepository
import no.nav.paw.arbeidssokerregisteret.api.v4.OpplysningerOmArbeidssoeker
import java.util.*

class OpplysningerOmArbeidssoekerService(private val opplysningerOmArbeidssoekerRepository: OpplysningerOmArbeidssoekerRepository) {
    fun hentOpplysningerOmArbeidssoeker(periodeId: UUID): List<OpplysningerOmArbeidssoekerResponse> = opplysningerOmArbeidssoekerRepository.hentOpplysningerOmArbeidssoeker(periodeId)

    fun lagreBatch(batch: Sequence<OpplysningerOmArbeidssoeker>) = opplysningerOmArbeidssoekerRepository.storeBatch(batch)
}
