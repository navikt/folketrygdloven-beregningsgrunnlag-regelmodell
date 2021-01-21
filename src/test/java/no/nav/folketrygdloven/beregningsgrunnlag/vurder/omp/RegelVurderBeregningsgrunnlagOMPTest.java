package no.nav.folketrygdloven.beregningsgrunnlag.vurder.omp;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.AvslagUnderEnHalvG;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;

class RegelVurderBeregningsgrunnlagOMPTest {

	public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

	@Test
	public void skal_ikke_vurdere_vilkår_om_kun_utbetaling_til_arbeidsgiver() {

		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.valueOf(20_000));


		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.AT)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medArbeidsforhold(Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
										.medArbeidsforholdId("123467890").build())
								.medUtbetalingsprosent(BigDecimal.valueOf(100))
								.medBeregnetPrÅr(BigDecimal.valueOf(20_000)).build()).build())
				.build();

		Beregningsgrunnlag.builder().medBeregningsgrunnlagPeriode(periode)
				.medYtelsesSpesifiktGrunnlag(omsorgspengerGrunnlag)
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.medGrunnbeløp(BigDecimal.valueOf(100_000))
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.AT, BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON)))
				.medInntektsgrunnlag(new Inntektsgrunnlag())
				.build();

		Evaluation evaluation = evaluer(periode);

		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
		assertThat(evaluation.getOutcome()).isNull();
	}

	@Test
	public void skal_vurdere_vilkår_om_utbetaling_til_bruker() {

		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.valueOf(20_000));


		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.AT)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medArbeidsforhold(Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
										.medArbeidsforholdId("123467890").build())
								.medUtbetalingsprosent(BigDecimal.valueOf(100))
								.medBeregnetPrÅr(BigDecimal.valueOf(30_000)).build()).build())
				.build();

		Beregningsgrunnlag.builder().medBeregningsgrunnlagPeriode(periode)
				.medYtelsesSpesifiktGrunnlag(omsorgspengerGrunnlag)
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.medGrunnbeløp(BigDecimal.valueOf(100_000))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(100_000))
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.AT, BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON)))
				.medInntektsgrunnlag(new Inntektsgrunnlag())
				.build();

		Evaluation evaluation = evaluer(periode);

		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}

	private Evaluation evaluer(BeregningsgrunnlagPeriode periode) {
		return new RegelVurderBeregningsgrunnlagOMP(periode).getSpecification().evaluate(periode);
	}

}