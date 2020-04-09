package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2016;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2018;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2019;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilSøknadsinntekt;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RegelForeslåBeregningsgrunnlagFRISINNTest {

    private LocalDate skjæringstidspunkt;
    private String orgnr;
    private Arbeidsforhold arbeidsforhold;

    @BeforeEach
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);
        orgnr = "987";
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalSetteBruttoForSNLikOppgittInntektATSN() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor2SisteÅr(5, 3), Inntektskilde.SIGRUN);


        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        LocalDate FRISINN_FOM = LocalDate.of(2017, 1, 1);
        LocalDate FRISINN_TOM = LocalDate.of(2019, 12, 31);
        Periode beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserSN(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, GSNITT_2019);
    }

    private void verifiserSN(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, long beløp) {
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        assertThat(bgpsa).isNotNull();
        if (hjemmel != null) {
            assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgpsa.getAktivitetStatus()).getHjemmel()).isEqualTo(hjemmel);
        }
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01));
    }

    @Test
    public void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT(){
        // arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
        // før skjæringstidspunktet (beregningsperioden)
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);

        List<BigDecimal> månedsinntekter18 = Collections.nCopies(12, månedsinntekt);
        List<BigDecimal> månedsinntekter19 = Collections.nCopies(12, BigDecimal.ZERO);

        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(12), månedsinntekter18,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter19,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING,arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag =  settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.emptyList());

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);

        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotBlank();
        //SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
        assertThat(grunnlag.getBruttoPrÅr().compareTo(BigDecimal.ZERO)).isZero();
    }

    private static List<BigDecimal> årsinntekterFor2SisteÅr(double pgi2, double pgi1) {
        return Arrays.asList(BigDecimal.valueOf(pgi2 * GSNITT_2017), BigDecimal.valueOf(pgi1 * GSNITT_2018), BigDecimal.valueOf(pgi1 * GSNITT_2016));
    }

}
