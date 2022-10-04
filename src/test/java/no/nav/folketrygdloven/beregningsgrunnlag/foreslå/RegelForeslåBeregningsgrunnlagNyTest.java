package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekterPrStatus;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settOppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter.getRegelResultat;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.F_14_7;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå.RegelFortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelForeslåBeregningsgrunnlagNyTest {

	private LocalDate skjæringstidspunkt;
	private String orgnr;
	private Arbeidsforhold arbeidsforhold;

	@BeforeEach
	public void setup() {
		skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
		orgnr = "987";
		arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
	}

	@Test
	public void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() { // NOSONAR
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	public void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() { // NOSONAR
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	public void skalBeregneGrunnlagAGVedKombinasjonATFLogSN() { // NOSONAR
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

		BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
		verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
		double beløpSN = ((4.0d * GRUNNBELØP_2017) - (12 * månedsinntekt.doubleValue())); // Differanse siden SN > ATFL: SN - ATFL
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, beløpSN, 4.0d * GRUNNBELØP_2017);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, beløpSN + 12 * månedsinntekt.doubleValue());
	}

	@Test
	public void skalBeregneGrunnlagAGVedKombinasjonATFLogSNHvorATFLStørreEnnSNMedAvkorting() { // NOSONAR
		// ATFL > 6G, SN < ATFL: ATFL blir avkortet til 6G og SN blir satt til 0.
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 4, 6), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

		BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		double forventetPGI = 5.0d * GRUNNBELØP_2017;
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0, forventetPGI);
	}

	@Test
	public void BeregningsgrunnlagKombinasjonATFLStørreEnnSNMedAvkorting() { // NOSONAR
		// SN > 6G, SN > ATFL: Både ATFL og SN blir avkortet.
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(7, 8, 6), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

		BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		double forventetATFL = 12 * månedsinntekt.doubleValue();
		double forventetPGI = 593015.333333;
		double forventetSN = forventetPGI - forventetATFL;
		verifiserBeregningsgrunnlagBeregnet(grunnlag, forventetATFL + forventetSN);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, forventetATFL);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, forventetSN, forventetPGI);
	}

	@Test
	public void skalBeregneGrunnlagMedInntektsmeldingMedNaturalYtelser() {
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
		BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
		BigDecimal naturalytelse = BigDecimal.valueOf(2000);
		LocalDate naturalytelseOpphørFom = skjæringstidspunkt;
		Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt, refusjonskrav, naturalytelse, naturalytelseOpphørFom);
		opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(30000));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);
		RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");


		assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(BigDecimal.valueOf(24000));
		assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(400);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	public void skalBeregneGrunnlagForTilstøtendeYtelseDagpenger() { // NOSONAR
		// Arrange
		BigDecimal dagsats = BigDecimal.valueOf(716);
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.medMåned(skjæringstidspunkt)
				.medInntekt(dagsats)
				.medUtbetalingsfaktor(BigDecimal.ZERO)
				.build());
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.DP));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, 260 * dagsats.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 260 * dagsats.doubleValue());
	}

	@Test
	public void skalBeregneGrunnlagForKombinasjonSNOgDagpenger() { // NOSONAR
		// Arrange
		BigDecimal utbetalingsfaktor = BigDecimal.valueOf(0.75);
		BigDecimal dagsats = BigDecimal.valueOf(900);
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 5, 5), Inntektskilde.SIGRUN);
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.medMåned(skjæringstidspunkt)
				.medInntekt(dagsats)
				.medUtbetalingsfaktor(utbetalingsfaktor)
				.build());
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				List.of(AktivitetStatus.SN, AktivitetStatus.DP));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		double expectedbruttoDP = dagsats.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
		double expectedPGIsnitt = 5.0 * GRUNNBELØP_2017;
		double expectedBruttoSN = expectedPGIsnitt - expectedbruttoDP;
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, expectedbruttoDP);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoDP + expectedBruttoSN);
	}


	@Test
	public void skalBeregneGrunnlagForKombinasjonATFL_SNOgAAP() { // NOSONAR
		// Arrange
		BigDecimal utbetalingsfaktor = new BigDecimal("1");
		BigDecimal dagsatsAAP = BigDecimal.valueOf(700);
		BigDecimal månedsinntektATFL = BigDecimal.valueOf(20000);
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(6, 6, 6), Inntektskilde.SIGRUN);

		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING).medArbeidsgiver(arbeidsforhold)
				.medInntekt(månedsinntektATFL).medMåned(skjæringstidspunkt).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP).medUtbetalingsfaktor(utbetalingsfaktor)
				.medInntekt(dagsatsAAP).medMåned(skjæringstidspunkt).build());
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP), Collections.singletonList(arbeidsforhold),
				Collections.singletonList(månedsinntektATFL.multiply(BigDecimal.valueOf(12))));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		double expectedbruttoAAP = dagsatsAAP.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
		double expectedPGIsnitt = 6.0 * GRUNNBELØP_2017;
		double expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
		double expectedBruttoSN = expectedPGIsnitt - expectedbruttoAAP - expectedBruttoATFL;
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, expectedbruttoAAP);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, expectedBruttoATFL);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
	}


	@Test
	public void skalTesteNyoppstartetFrilanser() {
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(25000));
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(Arbeidsforhold.frilansArbeidsforhold()));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(BigDecimal.valueOf(300000))
				.build();

		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

		// Assert
		RegelResultat regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
		LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
		assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
	}

	@Test
	public void skalTesteArbeidsforholdInntektSattAvSaksbehandlerNårIkkeInntektsmelding() {
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
		opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(18000));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(BigDecimal.valueOf(200000))
				.build();

		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

		// Assert
		RegelResultat regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
		LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
		assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
		assertThat(beregningsgrunnlag.getSammenligningsGrunnlag()).isNotNull();
		assertThat(beregningsgrunnlag.getSammenligningsgrunnlagPrStatus()).isNotEmpty();
	}

	@Test
	public void skalTesteKjøringAvKunYtelse() {
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, new Inntektsgrunnlag(),
				Collections.singletonList(AktivitetStatus.KUN_YTELSE));
		Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(false));
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrStatus prStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA);
		BeregningsgrunnlagPrStatus.builder(prStatus).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(100000));
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

		// Assert
		RegelResultat regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		assertThat(grunnlag.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100000));
		assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}


	@Test
	public void skalTåleUkjentStatustype() {
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true);
		leggtilStatus(beregningsgrunnlag, AktivitetStatus.UDEFINERT);
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	public void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT() {
		// arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
		// før skjæringstidspunktet (beregningsperioden)
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(3), List.of(månedsinntekt, månedsinntekt, månedsinntekt),
				Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
				Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));

		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

		String sporing = EvaluationSerializer.asJson(evaluation);
		assertThat(sporing).isNotBlank();
		//SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
		assertThat(grunnlag.getBruttoPrÅr().compareTo(BigDecimal.ZERO)).isZero();
		// skal beregningsperioden settes til de tre siste månedene før skjæringstidspunktet for beregning
		LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
		LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0)
				.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
	}

	@Test
	public void skalIkkeSetteAksjonspunktForATMedVarierendeInntekterNårRefusjonLikBeregnetOgOmsorgspenger() {
		// Arrange
		BigDecimal månedsinntektGammel = BigDecimal.valueOf(GRUNNBELØP_2017 / 12);
		BigDecimal månedsinntektNy = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(GRUNNBELØP_2017 / 2);
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		List<BigDecimal> månedsinntekter = List.of(månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel,
				månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
		BeregningsgrunnlagPeriode grunnlag = settOppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, AktivitetStatus.ATFL,
				List.of(arbeidsforhold), Collections.singletonList(refusjonskravPrÅr), true, refusjonskravPrÅr).getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused")
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");
		assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).isEmpty();
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_9_8_8_28);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
	}

	@Test
	public void skalSetteAksjonspunktForATMedVarierendeInntekterNårRefusjonMindreEnnBeregnetOgOmsorgspenger() {
		// Arrange
		BigDecimal månedsinntektGammel = BigDecimal.valueOf(GRUNNBELØP_2017 / 12);
		BigDecimal månedsinntektNy = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(GRUNNBELØP_2017 / 4);
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		List<BigDecimal> månedsinntekter = List.of(månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel,
				månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
		BeregningsgrunnlagPeriode grunnlag = settOppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, AktivitetStatus.ATFL,
				List.of(arbeidsforhold), Collections.singletonList(refusjonskravPrÅr), true, refusjonskravPrÅr).getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused")
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");
		assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_MED_AVVIKSVURDERING, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_9_9_8_28_8_30);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
	}

	@Test
	public void skalSetteAksjonspunktForATMedVarierendeInntekterNårRefusjonLikBeregnetOgIkkeOmsorgspenger() {
		// Arrange
		BigDecimal månedsinntektGammel = BigDecimal.valueOf(GRUNNBELØP_2017 / 12);
		BigDecimal månedsinntektNy = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(GRUNNBELØP_2017 / 2);
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		List<BigDecimal> månedsinntekter = List.of(månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel,
				månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
		BeregningsgrunnlagPeriode grunnlag = settOppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, AktivitetStatus.ATFL,
				List.of(arbeidsforhold), Collections.singletonList(refusjonskravPrÅr), false, refusjonskravPrÅr).getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused")
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");
		assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_30);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
	}

	@Test
	public void skalBeregneMilitærKombinertMedNæringOgArbeid() { // NOSONAR
		// Arrange
		BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
		BigDecimal refusjonskrav = BigDecimal.valueOf(4.0d * GSNITT_2017 / 12);
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

		BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN, AktivitetStatus.MS),
				singletonList(arbeidsforhold), singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

		// Act
		Evaluation evaluation = new RegelForeslåBeregningsgrunnlagNy(grunnlag).evaluer(grunnlag);
		Evaluation evaluation2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.MS, 0);
	}

	private void verifiserBeregningsgrunnlagHjemmel(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus aktivitetStatus,
	                                                BeregningsgrunnlagHjemmel hjemmel) {
		assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(aktivitetStatus).getHjemmel()).isEqualTo(hjemmel);
	}

	private void leggtilStatus(Beregningsgrunnlag beregningsgrunnlag, AktivitetStatus aktivitetStatus) {
		BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPeriode.builder(periode)
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(aktivitetStatus)
						.medAndelNr(periode.getBeregningsgrunnlagPrStatus().size() + 1L)
						.build())
				.build();
		Beregningsgrunnlag.builder(beregningsgrunnlag).medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(aktivitetStatus, null))).build();
	}
}
