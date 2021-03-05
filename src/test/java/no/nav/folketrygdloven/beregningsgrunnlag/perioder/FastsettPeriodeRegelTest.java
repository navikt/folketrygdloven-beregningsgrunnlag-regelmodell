package no.nav.folketrygdloven.beregningsgrunnlag.perioder;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelEndring;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellRefusjonOgNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

class FastsettPeriodeRegelTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final String ORGNR = "1234657";
    private static final String ORGNR2 = "89729572935";

    @Test
    void skalLageNyAndelForSVPForArbeidsforholdMedSøktYtelseFraSTP() {

        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
                .medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
        PeriodeModellUtbetalingsgrad inputMedGraderingFraStartForNyttArbeid = lagUtbetalingPeriodeInputMedEnAndelFraStart()
                .medEndringISøktYtelse(lagUtbetalingsgradFraSkjæringstidspunkt(arbeidsforhold2))
                .build();
        List<SplittetPeriode> perioder = new ArrayList<>();
        kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getNyeAndeler().size()).isEqualTo(1);
        assertThat(perioder.get(1).getNyeAndeler().size()).isEqualTo(0);
    }

    private void kjørRegel(PeriodeModell inputMedGraderingFraStartForNyttArbeid, List<SplittetPeriode> perioder) {
        new FastsettPeriodeRegel().evaluer(inputMedGraderingFraStartForNyttArbeid, perioder);
    }

    @Test
    void skalLageNyAndelIPeriodeEtterGraderingperiodeMedNyAndel() {
        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
            .medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
	    PeriodeModellGradering inputMedGraderingFraStartForNyttArbeid = lagGraderingPeriodeInputMedEnAndelFraStart()
			    .medAndelGraderinger(lagGraderingFraSkjæringstidspunkt(arbeidsforhold2))
            .build();
        List<SplittetPeriode> perioder = new ArrayList<>();
        kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
        assertThat(perioder.size()).isEqualTo(2);
        assertThat(perioder.get(0).getNyeAndeler().size()).isEqualTo(1);
        assertThat(perioder.get(1).getNyeAndeler().size()).isEqualTo(1);
    }

    private List<AndelEndring> lagGraderingFraSkjæringstidspunkt(Arbeidsforhold arbeidsforhold2) {
        return List.of(AndelGraderingImpl.builder().medAktivitetStatus(AktivitetStatusV2.AT).medArbeidsforhold(arbeidsforhold2).medGraderinger(List.of(new Gradering(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1)), BigDecimal.valueOf(50)))).build());
    }

	private List<AndelEndring> lagUtbetalingsgradFraSkjæringstidspunkt(Arbeidsforhold arbeidsforhold2) {
		return List.of(AndelUtbetalingsgrad.builder().medAktivitetStatus(AktivitetStatusV2.AT).medArbeidsforhold(arbeidsforhold2).medGraderinger(List.of(new Gradering(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1)), BigDecimal.valueOf(50)))).build());
	}

    private PeriodeModellGradering.Builder lagGraderingPeriodeInputMedEnAndelFraStart() {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
                .medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12))).medOrgnr(ORGNR).build();
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.AT).medBruttoPrÅr(BigDecimal.valueOf(500_000)).medArbeidsforhold(arbeidsforhold).build();
        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
                .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
                .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
                .build();
        return PeriodeModellGradering.builder()
                .medGrunnbeløp(BigDecimal.valueOf(90_000))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
                .medEksisterendePerioder(List.of(SplittetPeriode.builder()
                        .medPeriodeÅrsaker(List.of())
                        .medFørstePeriodeAndeler(List.of(EksisterendeAndel.builder().medArbeidsforhold(arbeidsforhold).medAndelNr(1L).build()))
                        .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
                        .build()));
    }


	private PeriodeModellUtbetalingsgrad.Builder lagUtbetalingPeriodeInputMedEnAndelFraStart() {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12))).medOrgnr(ORGNR).build();
		BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.AT).medBruttoPrÅr(BigDecimal.valueOf(500_000)).medArbeidsforhold(arbeidsforhold).build();
		PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
				.build();
		return PeriodeModellUtbetalingsgrad.builder()
				.medGrunnbeløp(BigDecimal.valueOf(90_000))
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
				.medEksisterendePerioder(List.of(SplittetPeriode.builder()
						.medPeriodeÅrsaker(List.of())
						.medFørstePeriodeAndeler(List.of(EksisterendeAndel.builder().medArbeidsforhold(arbeidsforhold).medAndelNr(1L).build()))
						.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
						.build()));
	}

}
