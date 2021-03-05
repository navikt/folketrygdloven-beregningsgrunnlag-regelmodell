package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;

public interface AndelEndring {

    AktivitetStatusV2 getAktivitetStatus();

    List<Gradering> getGraderinger();

    boolean erNyAktivitet(PeriodeModell input, LocalDate periodeFom);

    List<Refusjonskrav> getGyldigeRefusjonskrav();

    Arbeidsforhold getArbeidsforhold();

	boolean filterForEksisterendeAktiviteter();

	boolean filterForNyeAktiviteter(LocalDate skjæringstidspunkt, LocalDate periodeFom);

	EksisterendeAndel mapForEksisterendeAktiviteter(LocalDate fom);

	SplittetAndel mapForNyeAktiviteter(LocalDate periodeFom);
}
