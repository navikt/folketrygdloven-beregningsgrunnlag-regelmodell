package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2019;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilSøknadsinntekt;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlagFRISINN.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlagFRISINN.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlagFRISINN.verifiserBeregningsperiode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

class RegelForeslåBeregningsgrunnlagFRISINNTest {

    private LocalDate skjæringstidspunkt;
    private String orgnr;
    private Arbeidsforhold arbeidsforhold;
    private FrisinnGrunnlag frisinnGrunnlag;

    @BeforeEach
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);
        orgnr = "987";
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
        frisinnGrunnlag = lagFrisinnGrunnlag();
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        beregningsgrunnlag = Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(frisinnGrunnlag).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        beregningsgrunnlag = Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(frisinnGrunnlag).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedKombinasjonATFLogSN() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();

        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(
            skjæringstidspunkt,
            inntektsgrunnlag,
            List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold),
            Collections.emptyList(),
            Optional.of(frisinnGrunnlag)
        ).getBeregningsgrunnlagPerioder().get(0);

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        LocalDate FRISINN_FOM = LocalDate.of(2017, 1, 1);
        LocalDate FRISINN_TOM = LocalDate.of(2019, 12, 31);
        Periode beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.KORONALOVEN_3, grunnlag, beregningsperiode);
        double beløpSN = GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, beløpSN);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, beløpSN + 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedKombinasjonATFLogSNHvorATFLStørreEnnSNMedAvkorting() {
        // ATFL > 6G, SN < ATFL: ATFL blir avkortet til 6G og SN blir satt til 0.
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        BigDecimal inntektSn = BigDecimal.valueOf(GSNITT_2019).multiply(BigDecimal.valueOf(2));
        leggTilSøknadsinntekt(inntektsgrunnlag, inntektSn);

        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.emptyList(), Optional.of(frisinnGrunnlag)).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue() + inntektSn.intValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, inntektSn.intValue());
    }

    @Test
    public void skalBeregneGrunnlagForKombinasjonSNOgDagpenger() {
        // Arrange
        BigDecimal utbetalingsgrad = new BigDecimal("150");
        BigDecimal dagsats = BigDecimal.valueOf(900);
        Inntektsgrunnlag inntektsgrunnlag = lagSnInntekter(5);
        leggTilYtelseMånederFør(utbetalingsgrad, dagsats, inntektsgrunnlag, LocalDate.of(2020, 4, 1), 38);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.SN, AktivitetStatus.DP), Optional.of(frisinnGrunnlag));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Endre på periode slik at den varer en måned og ikkje går til uendelig
        BeregningsgrunnlagPeriode.builder(grunnlag)
            .medPeriode(Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30)));

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoDP = dagsats.doubleValue() * 260 * utbetalingsgrad.intValue() / 200;
        double expectedBruttoSN = 5.0 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.DP, expectedbruttoDP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, expectedBruttoSN);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoDP + expectedBruttoSN);
    }


    @Test
    public void skalBeregneGrunnlagForKombinasjonATFL_SNOgAAP_ytelse_36_måneder() {
        // Arrange
        BigDecimal utbetalingsgrad = new BigDecimal("100");
        BigDecimal dagsatsAAP = BigDecimal.valueOf(700);
        BigDecimal månedsinntektATFL = BigDecimal.valueOf(20000);
        Inntektsgrunnlag inntektsgrunnlag = lagSnInntekter(6);
        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntektATFL);
        leggTilMånedsinntekter(inntektsgrunnlag, LocalDate.of(2020, 5, 1), månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilYtelseMånederFør(utbetalingsgrad, dagsatsAAP, inntektsgrunnlag, LocalDate.of(2020, 4, 1), 38);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP), Collections.singletonList(arbeidsforhold),
            Collections.emptyList(), Optional.of(frisinnGrunnlag));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Endre på periode slik at den varer en måned og ikkje går til uendelig
        BeregningsgrunnlagPeriode.builder(grunnlag)
            .medPeriode(Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30)));

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoAAP = dagsatsAAP.doubleValue() * 260 * utbetalingsgrad.intValue() / 200;
        double expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
        double expectedBruttoSN = 6.0 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.AAP, expectedbruttoAAP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, expectedBruttoATFL);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, expectedBruttoSN);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
    }

    @Test
    public void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT() {
        // arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
        // før skjæringstidspunktet (beregningsperioden)
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);

        List<BigDecimal> månedsinntekter18 = Collections.nCopies(12, månedsinntekt);
        List<BigDecimal> månedsinntekter19 = Collections.nCopies(12, BigDecimal.ZERO);

        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(12), månedsinntekter18,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter19,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.emptyList(), Optional.of(frisinnGrunnlag));

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);

        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotBlank();
        //SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
        assertThat(grunnlag.getBruttoPrÅr().compareTo(BigDecimal.ZERO)).isZero();
    }

    @Test
    public void skalIkkeTaMedÅrsinntekterUnder3kvartG() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();

        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.SN),
            Collections.emptyList(), Collections.emptyList(), Optional.of(frisinnGrunnlag)).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        LocalDate FRISINN_FOM = LocalDate.of(2017, 1, 1);
        LocalDate FRISINN_TOM = LocalDate.of(2019, 12, 31);
        Periode beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.KORONALOVEN_3, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, GSNITT_2019);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, GSNITT_2019);
    }

    private Inntektsgrunnlag lagSnInntekter(int g2019) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019).multiply(BigDecimal.valueOf(g2019)));
        return inntektsgrunnlag;
    }

    private void leggTilYtelseMånederFør(BigDecimal utbetalingsgrad, BigDecimal dagsats, Inntektsgrunnlag inntektsgrunnlag, LocalDate dato, int månederFør) {
        for (int i = 0; i < månederFør; i++) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
                .medMåned(dato.minusMonths(i))
                .medInntekt(dagsats)
                .medUtbetalingsgrad(utbetalingsgrad)
                .build());
        }
    }

    private static List<FrisinnPeriode> lagFrisinnperioder(Periode periode) {
        return Collections.singletonList(new FrisinnPeriode(periode, true, true));
    }

    private FrisinnGrunnlag lagFrisinnGrunnlag() {
        return new FrisinnGrunnlag(lagFrisinnperioder(Periode.of(skjæringstidspunkt, null)), List.of(Periode.of(skjæringstidspunkt, null)), skjæringstidspunkt);
    }
}
