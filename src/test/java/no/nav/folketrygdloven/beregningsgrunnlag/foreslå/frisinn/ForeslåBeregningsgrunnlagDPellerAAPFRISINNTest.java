package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

class ForeslåBeregningsgrunnlagDPellerAAPFRISINNTest {

    public static final LocalDate STP = LocalDate.of(2020, 4, 1);
    public static final BigDecimal DAGER_I_ET_ÅR = BigDecimal.valueOf(260);

    @Test
    void ett_meldekort_med_full_utbetaling() {
        // Arrange
        BeregningsgrunnlagPrStatus aapStatus = lagAAP();
        Periode p = Periode.of(STP, STP.plusMonths(1));
        BeregningsgrunnlagPeriode periode = lagPeriodeMedStatus(aapStatus, p);
        BigDecimal dagsats = BigDecimal.valueOf(100);
        BigDecimal utbetalingsgrad = BigDecimal.valueOf(200);
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(p, dagsats, utbetalingsgrad);
        byggBG(periode, inntektsgrunnlag);

        // Act
        kjørRegel(periode);

        // Assert
        assertThat(aapStatus.getBeregnetPrÅr()).isCloseTo(dagsats.multiply(DAGER_I_ET_ÅR), Percentage.withPercentage(0.00001));
    }

    @Test
    void ett_meldekort_med_full_utbetaling_deler_av_perioden() {
        // Arrange
        BeregningsgrunnlagPrStatus aapStatus = lagAAP();
        Periode beregnigsgrunnlagPeriode = Periode.of(STP, STP.plusMonths(1).minusDays(1));
        BeregningsgrunnlagPeriode periode = lagPeriodeMedStatus(aapStatus, beregnigsgrunnlagPeriode);
        BigDecimal dagsats = BigDecimal.valueOf(100);
        BigDecimal utbetalingsgrad = BigDecimal.valueOf(200);
        Periode p = Periode.of(STP, STP.plusDays(14));
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(p, dagsats, utbetalingsgrad);
        byggBG(periode, inntektsgrunnlag);

        // Act
        kjørRegel(periode);

        // Assert
        BigDecimal effektivDagsats = BigDecimal.valueOf(50);
        assertThat(aapStatus.getBeregnetPrÅr()).isCloseTo(effektivDagsats.multiply(DAGER_I_ET_ÅR), Percentage.withPercentage(0.00001));
    }

    @Test
    void to_meldekort_med_lik_dagsats_full_utbetaling_ulike_perioder() {
        // Arrange
        BeregningsgrunnlagPrStatus aapStatus = lagAAP();
        Periode p = Periode.of(STP, STP.plusMonths(1));
        BeregningsgrunnlagPeriode periode = lagPeriodeMedStatus(aapStatus, p);
        BigDecimal dagsats = BigDecimal.valueOf(100);
        BigDecimal utbetalingsgrad = BigDecimal.valueOf(200);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Periode p1 = Periode.of(STP, STP.plusDays(10));
        Periode p2 = Periode.of(STP.plusDays(11), STP.plusMonths(2));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p1, dagsats, utbetalingsgrad));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p2, dagsats, utbetalingsgrad));

        byggBG(periode, inntektsgrunnlag);

        // Act
        kjørRegel(periode);

        // Assert
        assertThat(aapStatus.getBeregnetPrÅr()).isCloseTo(dagsats.multiply(DAGER_I_ET_ÅR), Percentage.withPercentage(0.00001));
    }

    @Test
    void to_meldekort_med_ulik_dagsats_full_utbetaling_lik_periode() {
        // Arrange
        BeregningsgrunnlagPrStatus aapStatus = lagAAP();
        Periode p = Periode.of(STP, STP.plusMonths(1).minusDays(1));
        BeregningsgrunnlagPeriode periode = lagPeriodeMedStatus(aapStatus, p);
        BigDecimal dagsats1 = BigDecimal.valueOf(100);
        BigDecimal dagsats2 = BigDecimal.valueOf(300);
        BigDecimal utbetalingsgrad = BigDecimal.valueOf(200);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Periode p1 = Periode.of(STP, STP.plusDays(14));
        Periode p2 = Periode.of(STP.plusDays(15), STP.plusMonths(2));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p1, dagsats1, utbetalingsgrad));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p2, dagsats2, utbetalingsgrad));
        byggBG(periode, inntektsgrunnlag);

        // Act
        kjørRegel(periode);

        // Assert
        BigDecimal gjennomsnittligDagsats = BigDecimal.valueOf(200);
        assertThat(aapStatus.getBeregnetPrÅr()).isCloseTo(gjennomsnittligDagsats.multiply(DAGER_I_ET_ÅR), Percentage.withPercentage(0.00001));
    }

    @Test
    void to_meldekort_med_ulik_dagsats_delvis_og_full_utbetaling_lik_periode() {
        // Arrange
        BeregningsgrunnlagPrStatus aapStatus = lagAAP();
        Periode p = Periode.of(STP, STP.plusMonths(1).minusDays(1));
        BeregningsgrunnlagPeriode periode = lagPeriodeMedStatus(aapStatus, p);
        BigDecimal dagsats1 = BigDecimal.valueOf(100);
        BigDecimal dagsats2 = BigDecimal.valueOf(300);
        BigDecimal utbetalingsgrad1 = BigDecimal.valueOf(200);
        BigDecimal utbetalingsgrad2 = BigDecimal.valueOf(100);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Periode p1 = Periode.of(STP, STP.plusDays(14));
        Periode p2 = Periode.of(STP.plusDays(15), STP.plusMonths(2));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p1, dagsats1, utbetalingsgrad1));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p2, dagsats2, utbetalingsgrad2));

        byggBG(periode, inntektsgrunnlag);

        // Act
        kjørRegel(periode);

        // Assert
        BigDecimal gjennomsnittligDagsats = BigDecimal.valueOf(125);
        assertThat(aapStatus.getBeregnetPrÅr()).isCloseTo(gjennomsnittligDagsats.multiply(DAGER_I_ET_ÅR), Percentage.withPercentage(0.00001));
    }

    @Test
    void to_meldekort_med_ulik_dagsats_delvis_utbetaling_lik_periode() {
        // Arrange
        BeregningsgrunnlagPrStatus aapStatus = lagAAP();
        Periode p = Periode.of(STP, STP.plusMonths(1).minusDays(1));
        BeregningsgrunnlagPeriode periode = lagPeriodeMedStatus(aapStatus, p);
        BigDecimal dagsats1 = BigDecimal.valueOf(100);
        BigDecimal dagsats2 = BigDecimal.valueOf(300);
        BigDecimal utbetalingsgrad1 = BigDecimal.valueOf(100);
        BigDecimal utbetalingsgrad2 = BigDecimal.valueOf(100);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Periode p1 = Periode.of(STP, STP.plusDays(14));
        Periode p2 = Periode.of(STP.plusDays(15), STP.plusMonths(2));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p1, dagsats1, utbetalingsgrad1));
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p2, dagsats2, utbetalingsgrad2));

        byggBG(periode, inntektsgrunnlag);

        // Act
        kjørRegel(periode);

        // Assert
        BigDecimal gjennomsnittligDagsats = BigDecimal.valueOf(100);
        assertThat(aapStatus.getBeregnetPrÅr()).isCloseTo(gjennomsnittligDagsats.multiply(DAGER_I_ET_ÅR), Percentage.withPercentage(0.00001));
    }

    private BeregningsgrunnlagPrStatus lagAAP() {
        return BeregningsgrunnlagPrStatus.builder()
                .medAndelNr(1L)
                .medAktivitetStatus(AktivitetStatus.AAP)
                .build();
    }

    private BeregningsgrunnlagPeriode lagPeriodeMedStatus(BeregningsgrunnlagPrStatus aapStatus, Periode p) {
        return BeregningsgrunnlagPeriode.builder()
                .medPeriode(p)
                .medBeregningsgrunnlagPrStatus(aapStatus)
                .build();
    }

    private Inntektsgrunnlag lagInntektsgrunnlag(Periode p, BigDecimal dagsats, BigDecimal utbetalingsgrad) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeInntekt(p, dagsats, utbetalingsgrad));
        return inntektsgrunnlag;
    }

    private Periodeinntekt lagPeriodeInntekt(Periode p, BigDecimal dagsats, BigDecimal utbetalingsgrad) {
        return Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medPeriode(p)
            .medInntekt(dagsats)
            .medUtbetalingsgrad(utbetalingsgrad)
            .build();
    }

    private Beregningsgrunnlag byggBG(BeregningsgrunnlagPeriode periode, Inntektsgrunnlag inntektsgrunnlag) {
        return Beregningsgrunnlag.builder()
            .medAktivitetStatuser(List.of(
                new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null),
                new AktivitetStatusMedHjemmel(AktivitetStatus.AAP, null)))
            .medInntektsgrunnlag(inntektsgrunnlag)
            .medBeregningsgrunnlagPeriode(periode)
            .medSkjæringstidspunkt(STP)
            .medGrunnbeløp(BigDecimal.TEN)
            .medGrunnbeløpSatser(List.of(new Grunnbeløp(STP.minusMonths(12), STP, 10L, 10L)))
            .build();
    }

    private Evaluation kjørRegel(BeregningsgrunnlagPeriode periode) {
        return new ForeslåBeregningsgrunnlagDPellerAAPFRISINN().evaluate(periode);
    }
}
