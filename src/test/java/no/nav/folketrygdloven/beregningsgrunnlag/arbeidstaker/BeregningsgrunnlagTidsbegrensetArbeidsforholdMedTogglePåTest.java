package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdUtenInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekterPrStatus;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponentenPrAktivitet;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlagPrAktivitet;
import static no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter.getRegelResultat;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserRegelmerknad;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class BeregningsgrunnlagTidsbegrensetArbeidsforholdMedTogglePåTest {

    private final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    @Test
    public void skalBeregneGrunnlagUtenInntektsmeldingN1N3MedTidsbegrensetArbeidsforhold() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        LocalDate skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        Beregningsgrunnlag beregningsgrunnlag = togglePå(opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt2, månedsinntekt,
            null, true, 1, List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)));

        Inntektsgrunnlag inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), List.of(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, AktivitetStatus.FL);
        opprettSammenligningsgrunnlagPrAktivitet(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt, AktivitetStatus.FL);
        leggTilArbeidsforholdUtenInntektsmelding(grunnlag, skjæringstidspunkt2, BigDecimal.ZERO, null, Arbeidsforhold.anonymtArbeidsforhold(Aktivitet.ARBEIDSTAKERINNTEKT));

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5038");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalBeregneGrunnlagUtenInntektsmeldingN1N3MedTidsbegrensetArbeidsforholdSammenfallerMedBortfaltNaturalytelse() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        LocalDate skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        Beregningsgrunnlag beregningsgrunnlag = togglePå(opprettBeregningsgrunnlagFraInntektskomponentenPrAktivitet(skjæringstidspunkt2.minusMonths(2),
            månedsinntekt, null, true, 1, List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT), AktivitetStatus.FL));

        Inntektsgrunnlag inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), List.of(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, AktivitetStatus.FL);
        opprettSammenligningsgrunnlagPrAktivitet(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt, AktivitetStatus.FL);
        leggTilArbeidsforholdUtenInntektsmelding(grunnlag, skjæringstidspunkt2, BigDecimal.ZERO, null, Arbeidsforhold.anonymtArbeidsforhold(Aktivitet.ARBEIDSTAKERINNTEKT));

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5038");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedNullFrilansInntektSisteTreMånederOgTidsbegrensetArbeidsforhold() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.ZERO;
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektSammenligning = BigDecimal.valueOf(5000);
        Beregningsgrunnlag beregningsgrunnlag = togglePå(opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, true, Collections.singletonList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektSammenligning);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5038");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedNullFrilansInntektSisteTreMånederOgTidsbegrensetArbeidsforholdSammenfallendeMedNaturalytelse() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.ZERO;
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektSammenligning = BigDecimal.valueOf(5000);
        Beregningsgrunnlag beregningsgrunnlag = togglePå(opprettBeregningsgrunnlagFraInntektskomponentenPrAktivitet(skjæringstidspunkt.minusMonths(1),
            månedsinntekt, refusjonskrav, true, 12, List.of(PeriodeÅrsak.NATURALYTELSE_BORTFALT, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET),
            AktivitetStatus.FL));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        opprettSammenligningsgrunnlagPrAktivitet(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt.minusMonths(1), månedsinntektSammenligning, AktivitetStatus.FL);
        leggTilArbeidsforholdUtenInntektsmelding(grunnlag, skjæringstidspunkt, månedsinntekt, null, Arbeidsforhold.anonymtArbeidsforhold(Aktivitet.ARBEIDSTAKERINNTEKT));
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5038");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedAvvikVedLønnsøkningOgTidsbegrensetArbeidsforhold() { // NOSONAR
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        BigDecimal refusjonskrav = månedsinntekt;
        Beregningsgrunnlag beregningsgrunnlag = togglePå(opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, false, Collections.singletonList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        BigDecimal månedsinntektFraSaksbehandler = BigDecimal.valueOf(35000);  // 40% avvik, dvs > 25% avvik
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        verifiserRegelmerknad(regelResultat, "5038");

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedAvvikVedLønnsøkningOgTidsbegrensetArbeidsforholdSammenfallerMedBortfaltNaturalytelse() { // NOSONAR
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        BigDecimal refusjonskrav = månedsinntekt;
        Beregningsgrunnlag beregningsgrunnlag = togglePå(opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, false, List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, PeriodeÅrsak.NATURALYTELSE_BORTFALT)));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        BigDecimal månedsinntektFraSaksbehandler = BigDecimal.valueOf(35000);  // 40% avvik, dvs > 25% avvik
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        verifiserRegelmerknad(regelResultat, "5038");

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    private Beregningsgrunnlag togglePå(Beregningsgrunnlag bg) {
        return Beregningsgrunnlag.builder(bg).medSplitteATFLToggleVerdi(true).build();
    }
}
