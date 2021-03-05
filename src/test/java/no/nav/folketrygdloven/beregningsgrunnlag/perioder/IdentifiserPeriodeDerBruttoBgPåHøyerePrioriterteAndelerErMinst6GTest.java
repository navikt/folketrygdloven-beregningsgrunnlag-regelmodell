package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6GTest {

    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90_000);
    private static final BigDecimal MÅNEDSBELØP_2G = BigDecimal.valueOf(15000);
    private static final BigDecimal MÅNEDSBELØP_5_3G = BigDecimal.valueOf(40_000);
    private static final BigDecimal MÅNEDSBELØP_OVER_6G = BigDecimal.valueOf(70_000);
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.FEBRUARY, 14);

    @Test
    public void frilansOver6GSNGraderer() {
        // Arrange
        PeriodisertBruttoBeregningsgrunnlag periodertBg = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.FL)
                .medBruttoPrÅr(MÅNEDSBELØP_OVER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_5_3G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE), BigDecimal.valueOf(50));
        AndelGraderingImpl andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.SN)
            .medGraderinger(List.of(gradering))
            .build();
        PeriodeModellGradering input = PeriodeModellGradering.builder()
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodertBg))
            .medAndelGraderinger(List.of(andelGradering))
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        // Act
        Optional<LocalDate> resultatOpt = IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G.vurder(input, andelGradering, gradering.getPeriode());

        // Assert
        assertThat(resultatOpt).hasValueSatisfying(resultat ->
            assertThat(resultat).isEqualTo(gradering.getFom()));
    }

    @Test
    public void naturalytelseBortfallerEtterSNGraderes() {
        // Arrange
        Periode p1 = Periode.of(SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.APRIL, 21));
        Periode p2 = Periode.of(LocalDate.of(2019, Month.APRIL, 22), DateUtil.TIDENES_ENDE);

        PeriodisertBruttoBeregningsgrunnlag bgp1 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(p1)
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("A"))
                .medBruttoPrÅr(MÅNEDSBELØP_5_3G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_2G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();
        PeriodisertBruttoBeregningsgrunnlag bgp2 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(p2)
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("A"))
                .medBruttoPrÅr(MÅNEDSBELØP_OVER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_2G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE), BigDecimal.valueOf(50));
        AndelGraderingImpl andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.SN)
            .medGraderinger(List.of(gradering))
            .build();
	    PeriodeModellGradering input = PeriodeModellGradering.builder()
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(bgp1, bgp2))
            .medAndelGraderinger(List.of(andelGradering))
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        // Act
        Optional<LocalDate> resultatOpt = IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G.vurder(input, andelGradering, gradering.getPeriode());

        // Assert
        assertThat(resultatOpt).hasValueSatisfying(resultat ->
            assertThat(resultat).isEqualTo(p2.getFom()));
    }

    @Test
    public void naturalytelseBortfallerEtterSNGraderingSlutter() {
        // Arrange
        Periode p1 = Periode.of(SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.APRIL, 21));
        Periode p2 = Periode.of(LocalDate.of(2019, Month.APRIL, 22), DateUtil.TIDENES_ENDE);

        PeriodisertBruttoBeregningsgrunnlag bgp1 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(p1)
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("A"))
                .medBruttoPrÅr(MÅNEDSBELØP_5_3G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_2G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();
        PeriodisertBruttoBeregningsgrunnlag bgp2 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(p2)
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("A"))
                .medBruttoPrÅr(MÅNEDSBELØP_OVER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_2G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.APRIL, 21)), BigDecimal.valueOf(50));
        AndelGraderingImpl andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.SN)
            .medGraderinger(List.of(gradering))
            .build();
	    PeriodeModellGradering input = PeriodeModellGradering.builder()
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(bgp1, bgp2))
            .medAndelGraderinger(List.of(andelGradering))
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        // Act
        Optional<LocalDate> resultatOpt = IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G.vurder(input, andelGradering, gradering.getPeriode());

        // Assert
        assertThat(resultatOpt).isEmpty();
    }
}
