package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.militar;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter.getRegelResultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class RegelForeslåBeregningsgrunnlagMilitærTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, 2, 1);
    private static final BigDecimal GRUNNBELØP_2018 = BigDecimal.valueOf(96883);
    private static final BigDecimal ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP = BigDecimal.valueOf(3);
    private static final BigDecimal BELØP_MILITÆR_HAR_KRAV_PÅ_FP = GRUNNBELØP_2018.multiply(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP);

    @Test
    public void skalTesteAtMSBlirSattTil3GNårIkkeFastsattAvSaksbehandler() {
        //Arrange
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.MS));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BELØP_MILITÆR_HAR_KRAV_PÅ_FP);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalTesteAtMSBlirSattTil3GNårSaksbehandlerHarFastsattMindreEnn3G() {
        //Arrange
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.MS));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).medAntallGMilitærHarKravPå(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP.intValue()).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.valueOf(250_000))
            .medFastsattAvSaksbehandler(true)
            .build();
        //Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BELØP_MILITÆR_HAR_KRAV_PÅ_FP);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalTesteAtMSBlirSattTilDetSaksbehandlerHarFastsattNårMerEnn3G() {
        //Arrange
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.MS));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.valueOf(420_000))
            .medFastsattAvSaksbehandler(true)
            .build();
        //Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(420_000));
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalTesteAtMSFårDifferansenOver0Mellom3GogBruttoPåGrunnlaget() {
        //Arrange
        BigDecimal snInntekt = BigDecimal.valueOf(76783);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Arrays.asList(AktivitetStatus.MS, AktivitetStatus.SN));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).medAntallGMilitærHarKravPå(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP.intValue()).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN))
            .medBeregnetPrÅr(snInntekt)
            .build();
        //Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BigDecimal diffMellom3GOgATFLInntekt = BELØP_MILITÆR_HAR_KRAV_PÅ_FP.subtract(snInntekt);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(diffMellom3GOgATFLInntekt);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalTesteAtMSIkkeFårPengerNårSamletBruttoOver3G() {
        //Arrange
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Arrays.asList(AktivitetStatus.MS, AktivitetStatus.SN, AktivitetStatus.DP));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).medAntallGMilitærHarKravPå(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP.intValue()).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN))
            .medBeregnetPrÅr(BigDecimal.valueOf(200_000))
            .build();
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP))
            .medBeregnetPrÅr(BigDecimal.valueOf(150_000))
            .build();
        //Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

}
