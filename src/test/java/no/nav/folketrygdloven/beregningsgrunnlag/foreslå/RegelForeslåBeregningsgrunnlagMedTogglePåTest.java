package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekterPrStatus;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlagPrAktivitet;
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
import static org.assertj.core.api.Assertions.within;

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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
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

public class RegelForeslåBeregningsgrunnlagMedTogglePåTest {

    private LocalDate skjæringstidspunkt;
    private String orgnr;
    private Arbeidsforhold arbeidsforhold;
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";

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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        opprettSammenligningsgrunnlagPrAktivitet(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(30000), AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");


        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(BigDecimal.valueOf(24000));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getAvvikPromille()).isEqualTo(400);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoAAP = dagsatsAAP.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
        double expectedPGIsnitt = 6.0 * GRUNNBELØP_2017;
        double expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
        double expectedBruttoSN = expectedPGIsnitt - expectedbruttoAAP - expectedBruttoATFL;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, F_14_7, AktivitetStatus.AAP, expectedbruttoAAP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, expectedBruttoATFL);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
    }


    @Test
    public void skalTesteNyoppstartetFrilanser() {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        opprettSammenligningsgrunnlagPrAktivitet(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(Arbeidsforhold.frilansArbeidsforhold()));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);
        BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(300000))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

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
        opprettSammenligningsgrunnlagPrAktivitet(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(18000), AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);
        BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(200000))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
        assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(beregningsgrunnlag.getSammenligningsGrunnlagPrAktivitetstatus(AktivitetStatus.AT)).isNotNull();
    }

    @Test
    public void skalTesteKjøringAvKunYtelse() {
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.KUN_YTELSE));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(false));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);
        BeregningsgrunnlagPrStatus prStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA);
        BeregningsgrunnlagPrStatus.builder(prStatus).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(100000));
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

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
        togglePå(grunnlag);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT(){
        // arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
        // før skjæringstidspunktet (beregningsperioden)
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(3), List.of(månedsinntekt, månedsinntekt, månedsinntekt),
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING,arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag =  settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

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
    public void skalBeregneGrunnlagNårKunATOgInntektsmeldingForeliggerOgIngenNaturalytelserOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekter = Collections.nCopies(13, månedsinntekt);
        List<BigDecimal> månedsinntekterInntektsmelding = Collections.nCopies(13, månedsinntektInntektsmelding);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);
        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_30);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getAvvikProsent().doubleValue()).isCloseTo(33.33, within(0.05));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getRapportertPrÅr()).isEqualTo(månedsinntekt.multiply(BigDecimal.valueOf(12)));
    }

    @Test
    public void skalIkkeBenytteSammenligningsgrunnlagNårSøkerHarMilitærstatusOgATOgTjenerMindreEnn3GOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntekt), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL, AktivitetStatus.MS),
            List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);
        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT)).isNull();
    }


    @Test
    public void skalBeregneGrunnlagNårFLMedVarierendeInntekterOgToggleErPå() {
        // Arrange
        BigDecimal lavInntekt = BigDecimal.valueOf(1000);
        BigDecimal høyInntekt = BigDecimal.valueOf(100_000);
        List<BigDecimal> månedsinntekter = Arrays.asList(lavInntekt, lavInntekt, lavInntekt, lavInntekt, høyInntekt, høyInntekt, høyInntekt, lavInntekt, lavInntekt, lavInntekt, lavInntekt, lavInntekt);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, AktivitetStatus.FL);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * lavInntekt.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_38);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * lavInntekt.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL).getAvvikProsent().doubleValue()).isCloseTo(96.1, within(0.1));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL).getRapportertPrÅr()).isEqualTo(lavInntekt.multiply(BigDecimal.valueOf(9)).add(høyInntekt.multiply(BigDecimal.valueOf(3))));
    }

    @Test
    public void skalBeregneGrunnlagNårFLOgATOgInntektsmeldingForeliggerOgIngenNaturalytelserOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Arbeidsforhold arbeidsforholdFrilans = Arbeidsforhold.frilansArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekter = Collections.nCopies(13, månedsinntekt);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntekt), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforholdFrilans, AktivitetStatus.FL);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforholdFrilans, AktivitetStatus.FL);

        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold, arbeidsforholdFrilans), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * 2 *månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_40);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * 2 * månedsinntekt.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getAvvikProsent().doubleValue()).isCloseTo(0, within(0.0001));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getRapportertPrÅr()).isEqualTo(månedsinntekt.multiply(BigDecimal.valueOf(12)));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL).getAvvikProsent().doubleValue()).isCloseTo(0, within(0.0001));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL).getRapportertPrÅr()).isEqualTo(månedsinntekt.multiply(BigDecimal.valueOf(12)));
    }

    @Test
    public void skalIkkeBenytteSammenligningsgrunnlagNårSøkerHarMilitærstatusOgFLOgTjenerMindreEnn3GOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.FL);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntekt), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL, AktivitetStatus.MS),
            List.of(arbeidsforhold), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL)).isNull();
    }

    @Test
    public void skalBeregneGrunnlagForATNårIngenInntektsmeldingOgIngenNaturalytelseOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekter = Collections.nCopies(13, månedsinntekt);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), List.of(refusjonskrav)).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 *månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_30);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getAvvikProsent().doubleValue()).isCloseTo(0, within(0.0001));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getRapportertPrÅr()).isEqualTo(månedsinntekt.multiply(BigDecimal.valueOf(12)));
    }

    @Test
    public void skalBeregneGrunnlagForATNårAvvikMellomBergenetOgSammenligningsgrunnlagOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2 * 1.5 );
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekter = Collections.nCopies(14, månedsinntekt);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.nCopies(3, månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), List.of(refusjonskrav)).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_30);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getAvvikProsent().doubleValue()).isCloseTo(50, within(0.0001));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getRapportertPrÅr()).isEqualTo((månedsinntekt.multiply(BigDecimal.valueOf(12))));
    }

    @Test
    public void skalIkkeFastsetteSammenlingningsgrunnlagNårArbeidstakerOgFrilansISammeOrganisasjonOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Arbeidsforhold arbeidsforholdFrilans = Arbeidsforhold.frilansArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekter = Collections.nCopies(13, månedsinntekt);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntekt), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforholdFrilans, AktivitetStatus.FL);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforholdFrilans, AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold, arbeidsforholdFrilans), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)).medFlOgAtISammeOrganisasjon(true).build();
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * 2 *månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_40);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * 2 * månedsinntekt.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT)).isNull();
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL)).isNull();
    }

    @Test
    public void skalBeregneSammenligningsgrunnlagOgSetteRiktigAvvikNårSNiKombinasjonMedFLOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntektFL = BigDecimal.valueOf(10000);
        BigDecimal bruttoPrÅrFL = BigDecimal.valueOf(120000);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d),
            Inntektskilde.SIGRUN);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(30000 * 12)), Inntektskilde.SØKNAD, null);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.nCopies(12,månedsinntektFL), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.FL);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.nCopies(12,månedsinntektFL), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        double actualBruttoSN = 4.0d * GRUNNBELØP_2017 - bruttoPrÅrFL.doubleValue();
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN)).isNotNull();
        int oppgittSN = 30000 * 12;
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN).getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(oppgittSN + bruttoPrÅrFL.doubleValue()));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN).getAvvikPromille()).isEqualTo(282);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_FRILANSER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntektFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_FRILANSER_OG_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneSammenligningsgrunnlagOgSetteRiktigAvvikNårSNiKombinasjonMedATOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntektAT = BigDecimal.valueOf(10000);
        BigDecimal bruttoPrÅrAt = BigDecimal.valueOf(120000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d),
            Inntektskilde.SIGRUN);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.anonymtArbeidsforhold(Aktivitet.ARBEIDSTAKERINNTEKT);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(30000 * 12)), Inntektskilde.SØKNAD, null);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.nCopies(12,månedsinntektAT), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektAT), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        double actualBruttoSN = 4.0d * GRUNNBELØP_2017 - bruttoPrÅrAt.doubleValue();
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN)).isNotNull();
        int oppgittSN = 30000 * 12;
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN).getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(oppgittSN + bruttoPrÅrAt.doubleValue()));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.SN).getAvvikPromille()).isEqualTo(282);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntektAT.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    public void skalReturnereAksjonspunktNårAvvikMellomBeregnetOgSammenligningsgrunnlagForBådeFLOgATUtenTidsbegrensetArbeidsforholdOgToggleErPå() {
        // Arrange
        BigDecimal månedsinntektSammenligning = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektBeregnet = BigDecimal.valueOf(GRUNNBELØP_2017 / 12);
        Arbeidsforhold arbeidsforholdFrilans = Arbeidsforhold.frilansArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<BigDecimal> månedsinntekterSammenligning = Collections.nCopies(13, månedsinntektSammenligning);
        List<BigDecimal> månedsinntekterBeregnet = Collections.nCopies(13, månedsinntektBeregnet);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekterSammenligning, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektBeregnet), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekterBeregnet, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforholdFrilans, AktivitetStatus.FL);
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekterSammenligning, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforholdFrilans, AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold, arbeidsforholdFrilans), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getMerknader().size()).isOne();
        assertThat(regelResultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_40);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * 2 * månedsinntektBeregnet.doubleValue());
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getAvvikProsent().doubleValue()).isCloseTo(100, within(0.0001));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT).getRapportertPrÅr()).isEqualTo((månedsinntektSammenligning.multiply(BigDecimal.valueOf(12))));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL).getAvvikProsent().doubleValue()).isCloseTo(100, within(0.0001));
        assertThat(grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL).getRapportertPrÅr()).isEqualTo(månedsinntektSammenligning.multiply(BigDecimal.valueOf(12)));
    }

    @Test
    public void skalIkkeVurdere25ProsentAvvikForATMedVarierendeInntekterNårRefusjonLikBeregnetOgRefusjonSkalSjekkesFørAvviksvurderingOgToggleErPå() {
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
        togglePå(grunnlag);

        // Act
        @SuppressWarnings("unused")
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getMerknader().size()).isZero();
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_9_8_8_28);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
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

    private void togglePå(BeregningsgrunnlagPeriode periode) {
        Beregningsgrunnlag.builder(periode.getBeregningsgrunnlag()).medSplitteATFLToggleVerdi(true).build();
    }

}
