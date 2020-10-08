package no.nav.folketrygdloven.skjæringstidspunkt.dok;

import java.time.LocalDate;
import java.util.UUID;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFP;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.ServiceArgument;

public class RegelmodellForDokumentasjon {

    protected static final AktivitetStatusModell regelmodelForeldrepenger = opprettRegelmodellFp();


    private RegelmodellForDokumentasjon() {
    }

    private static AktivitetStatusModell opprettRegelmodellFp() {
	    LocalDate mandag = LocalDate.of(2019, 10, 7);
	    LocalDate lørdag = LocalDate.of(2019, 10, 5);
	    LocalDate fredag = LocalDate.of(2019, 10, 4);
	    AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(lørdag, lørdag.plusMonths(2)), "1223", null);
	    AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(fredag.minusMonths(5), fredag), "1224", null);
	    AktivitetStatusModell regelmodell = new AktivitetStatusModellFP();
	    regelmodell.setSkjæringstidspunktForOpptjening(mandag);
	    regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
	    regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
    	return regelmodell;
    }

}
