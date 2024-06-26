package no.nav.paw.arbeidssoekerregisteret.api.oppslag.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.models.ArbeidssoekerperiodeRequest
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.models.Identitetsnummer
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.models.OpplysningerOmArbeidssoekerRequest
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.models.ProfileringRequest
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.services.ArbeidssoekerperiodeService
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.services.AutorisasjonService
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.services.OpplysningerOmArbeidssoekerService
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.services.ProfileringService
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.utils.getNavAnsattFromToken
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.utils.getPidClaim
import no.nav.paw.arbeidssoekerregisteret.api.oppslag.utils.logger
import java.util.*

fun Route.oppslagRoutes(
    autorisasjonService: AutorisasjonService,
    arbeidssoekerperiodeService: ArbeidssoekerperiodeService,
    opplysningerOmArbeidssoekerService: OpplysningerOmArbeidssoekerService,
    profileringService: ProfileringService
) {
    route("/api/v1") {
        authenticate("tokenx") {
            route("/arbeidssoekerperioder") {
                get {
                    logger.info("Henter arbeidssøkerperioder for bruker")

                    val identitetsnummer = call.getPidClaim()

                    val arbeidssoekerperioder = arbeidssoekerperiodeService.hentArbeidssoekerperioder(identitetsnummer)

                    logger.info("Hentet arbeidssøkerperioder for bruker")

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
            route("/opplysninger-om-arbeidssoeker/{periodeId}") {
                get {
                    val periodeId = UUID.fromString(call.parameters["periodeId"])
                    val identitetsnummer = call.getPidClaim()

                    val periodeIdTilhoererIdentitetsnummer = call.verifyPeriodeId(periodeId, identitetsnummer, arbeidssoekerperiodeService)
                    if (!periodeIdTilhoererIdentitetsnummer) return@get

                    logger.info("Henter opplysninger-om-arbeidssøker for bruker med periodeId: $periodeId")

                    val opplysningerOmArbeidssoeker = opplysningerOmArbeidssoekerService.hentOpplysningerOmArbeidssoeker(periodeId)

                    logger.info("Hentet opplysninger-om-arbeidssøker for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, opplysningerOmArbeidssoeker)
                }
            }
            route("/profilering/{periodeId}") {
                get {
                    val periodeId = UUID.fromString(call.parameters["periodeId"])
                    val identitetsnummer = call.getPidClaim()

                    val periodeIdTilhoererIdentitetsnummer = call.verifyPeriodeId(periodeId, identitetsnummer, arbeidssoekerperiodeService)
                    if (!periodeIdTilhoererIdentitetsnummer) return@get

                    logger.info("Henter profilering for bruker med periodeId: $periodeId")

                    val profilering = profileringService.hentProfileringForArbeidssoekerMedPeriodeId(periodeId)

                    logger.info("Hentet profilering for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, profilering)
                }
            }
        }
        authenticate("azure") {
            route("/veileder/arbeidssoekerperioder") {
                post {
                    logger.info("Veileder henter arbeidssøkerperioder for bruker")

                    val (identitetsnummer) = call.receive<ArbeidssoekerperiodeRequest>()

                    val harTilgang = call.verifyAccessFromToken(autorisasjonService, Identitetsnummer(identitetsnummer))
                    if (!harTilgang) return@post

                    val arbeidssoekerperioder = arbeidssoekerperiodeService.hentArbeidssoekerperioder(Identitetsnummer(identitetsnummer))

                    logger.info("Veileder hentet arbeidssøkerperioder for bruker")

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
            route("/veileder/opplysninger-om-arbeidssoeker") {
                post {
                    val (identitetsnummer, periodeId) = call.receive<OpplysningerOmArbeidssoekerRequest>()

                    logger.info("Veileder henter opplysninger-om-arbeidssøker for bruker med periodeId: $periodeId")

                    val harTilgang = call.verifyAccessFromToken(autorisasjonService, Identitetsnummer(identitetsnummer))
                    if (!harTilgang) return@post

                    val periodeIdTilhoererIdentitetsnummer = call.verifyPeriodeId(periodeId, Identitetsnummer(identitetsnummer), arbeidssoekerperiodeService)
                    if (!periodeIdTilhoererIdentitetsnummer) return@post

                    val opplysningerOmArbeidssoeker = opplysningerOmArbeidssoekerService.hentOpplysningerOmArbeidssoeker(periodeId)

                    logger.info("Veileder hentet opplysninger-om-arbeidssøker for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, opplysningerOmArbeidssoeker)
                }
            }
            route("/veileder/profilering") {
                post {
                    val (identitetsnummer, periodeId) = call.receive<ProfileringRequest>()

                    logger.info("Veileder henter profilering for bruker med periodeId: $periodeId")

                    val harTilgang = call.verifyAccessFromToken(autorisasjonService, Identitetsnummer(identitetsnummer))
                    if (!harTilgang) return@post

                    val periodeIdTilhoererIdentitetsnummer = call.verifyPeriodeId(periodeId, Identitetsnummer(identitetsnummer), arbeidssoekerperiodeService)
                    if (!periodeIdTilhoererIdentitetsnummer) return@post

                    val profilering = profileringService.hentProfileringForArbeidssoekerMedPeriodeId(periodeId)

                    logger.info("Veileder hentet profilering for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, profilering)
                }
            }
        }
    }
}

suspend fun ApplicationCall.verifyAccessFromToken(
    autorisasjonService: AutorisasjonService,
    identitetsnummer: Identitetsnummer
): Boolean {
    val navAnsatt =
        getNavAnsattFromToken()
            ?: return true

    logger.info("Sjekker om NAV-ansatt har tilgang til bruker")
    return autorisasjonService.verifiserTilgangTilBruker(navAnsatt, identitetsnummer).also { harTilgang ->
        if (!harTilgang) {
            logger.warn("NAV-ansatt har ikke tilgang til bruker")
            respondText(status = HttpStatusCode.Forbidden, text = HttpStatusCode.Forbidden.description)
        }
    }
}

suspend fun ApplicationCall.verifyPeriodeId(
    periodeId: UUID,
    identitetsnummer: Identitetsnummer,
    arbeidssoekerperiodeService: ArbeidssoekerperiodeService
): Boolean {
    val periodeIdTilhoererIdentitetsnummer = arbeidssoekerperiodeService.periodeIdTilhoererIdentitetsnummer(periodeId, identitetsnummer)
    if (!periodeIdTilhoererIdentitetsnummer) {
        respondText(status = HttpStatusCode.Forbidden, text = "PeriodeId tilhører ikke bruker: $periodeId")
        return false
    }
    return true
}
