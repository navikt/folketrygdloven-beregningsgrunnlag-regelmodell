package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2015;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2016;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekterForOppgittÅrene;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntektForOppgittÅrene;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserRegelmerknad;
import static no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

class RegelBeregningsgrunnlagSNTest {

    private LocalDate skjæringstidspunkt;

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    }

    @Test
    void skalBeregneGrunnlagSNForNormalInntektUnder6G() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneGrunnlagSNForNormalInntektOver12G() {
        // Arrange
        //PGI >= 12Gsnitt: Bidrag til beregningsgrunnlaget = 8
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(14.0d, 15.0d, 16.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G = 8 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 8 * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneGrunnlagSNForNormalInntektMellom6Gog12G() {
        // Arrange
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(8.0d, 9.0d, 10.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 655438);
    }

    @Test
    void skalBeregneGrunnlagSNForSterktVarierendeInntekt() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        //PGI >= 12Gsnitt: Bidrag til beregningsgrunnlaget = 8
        List<BigDecimal> årsinntekter = List.of(BigDecimal.valueOf(9 * GSNITT_2015), BigDecimal.valueOf(GSNITT_2016), BigDecimal.valueOf(2 * GSNITT_2017));
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekter, Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 312113.3333);
    }

    @Test
    void skalBeregneAvvikVedVarigEndring() {
        // Arrange
        //PGI >= 12Gsnitt: Bidrag til beregningsgrunnlaget = 8
        List<BigDecimal> årsinntekter = List.of(BigDecimal.valueOf(12 * GSNITT_2015), BigDecimal.valueOf(12 * GSNITT_2016), BigDecimal.valueOf(12 * GSNITT_2017));
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekter, Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(GRUNNBELØP_2017 * 1.245 * 12)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G = 8 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 8 * GRUNNBELØP_2017);
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
	    var sgPrStatus = grunnlag.getSammenligningsGrunnlagForType(SammenligningGrunnlagType.SN).orElseThrow();
	    assertThat(sg).isNotNull();
        assertThat(sg.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(867.5));
	    assertThat(sgPrStatus.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(867.5));


    }

    @Test
    void skalGiRegelmerknadVedAvvikStørreEnn25Prosent() {
        // Arrange
        List<BigDecimal> årsinntekter = List.of(BigDecimal.valueOf(7 * GSNITT_2015), BigDecimal.valueOf(8 * GSNITT_2016), BigDecimal.valueOf(9 * GSNITT_2017));
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekter, Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(GRUNNBELØP_2017*12)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5039");
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 624226.6667);
	    SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
	    assertThat(sg).isNotNull();
        assertThat(sg.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    @Test
    void skalBeregneGrunnlagSNNårBareToFerdiglignedeÅrForeligger() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            List.of(
                BigDecimal.valueOf(2.0d * GSNITT_2016),
                BigDecimal.valueOf(4.0d * GSNITT_2017)
            ), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 2.0d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneGrunnlagSNiKombinasjonMedDagpenger() {
        // Arrange
        BigDecimal bruttoDP = BigDecimal.valueOf(155500);
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 6.0d, 7.0d),
            Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.SN, AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        double actualBruttoSN = 540996.4434041 - bruttoDP.doubleValue();
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 540996.4434041);
    }

    @Test
    void skalBeregneGrunnlagSNiKombinasjonMedAAP() {
        // Arrange
        BigDecimal bruttoAAP = BigDecimal.valueOf(158400);
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(30000*12)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.SN, AktivitetStatus.AAP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP)).medBeregnetPrÅr(bruttoAAP).build();
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        double actualBruttoSN = 4.0d * GRUNNBELØP_2017 - bruttoAAP.doubleValue() ;
	    SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
	    assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        int oppgittSN = 30000 * 12;
	    assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(oppgittSN+bruttoAAP.doubleValue() ));
	    assertThat(sg.getAvvikPromilleUtenAvrunding().setScale(0, RoundingMode.HALF_UP)).isEqualByComparingTo(BigDecimal.valueOf(384));

	    verifiserRegelmerknad(regelResultat, "5039");
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneGrunnlagSNiKombinasjonMedDP() {
        // Arrange
        BigDecimal bruttoDP = BigDecimal.valueOf(118560);
        BigDecimal oppgittÅrsInntektSN = BigDecimal.valueOf(240000);
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(oppgittÅrsInntektSN), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.SN, AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

	    SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
	    double actualBruttoSN = 4.0d * GRUNNBELØP_2017 - bruttoDP.doubleValue();
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        int oppgittSN = 20000 * 12;
	    assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(oppgittSN+bruttoDP.doubleValue()));
	    assertThat(sg.getAvvikPromilleUtenAvrunding().setScale(0, RoundingMode.HALF_UP)).isEqualByComparingTo(BigDecimal.valueOf(43));
	    //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneGrunnlagSNmedSigrunInntekterSomEr0() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(0.0d, 0.0d, 0.0d), Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(120000)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");


	    SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
	    assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
	    assertThat(sg.getRapportertPrÅr().doubleValue()).isEqualTo(10000 * 12);
	    assertThat(sg.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(1000));
	    verifiserRegelmerknad(regelResultat, "5039");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 0, 0);
    }

    @Test
    void skalGiRegelmerknadForSNSomErNyIArbeidslivet() {
        //Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(6d, 3d, 0.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)).medErNyIArbeidslivet(true);

        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");

        // Assert
	    assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getBeregnetPrÅr()).isNull();
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getPgiListe()).hasSize(3);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getGjennomsnittligPGI()).isNotNull();
        verifiserRegelmerknad(regelResultat, "5049");
    }

    @Test
    void skalBeregneGrunnlagNårToÅreneErFerdiglignet() {
        // Arrange
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekterForOppgittÅrene(
            årsinntektForOppgittÅrene(8d, 2017, 2015), Inntektskilde.SIGRUN, 2017, 2015);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 416151.111132);
    }

    @Test
    void skalBeregneGrunnlagNårEttÅrErFerdiglignet() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekterForOppgittÅrene(
            årsinntektForOppgittÅrene(4d, 2016), Inntektskilde.SIGRUN, 2016);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt.minusYears(1), 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 124845.3333);
    }

    @Test
    void skalBeregneGrunnlagNårDetFinnesIngenFerdiglignetÅr() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt.minusYears(1), 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 0d);
    }

    @Test
    void skalBeregneGrunnlagNårDetFinnesIngenFerdiglignetÅrOgNyoppstartetSN() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
            .medMåned(skjæringstidspunkt.minusMonths(1))
            .medInntekt(BigDecimal.valueOf(250000))
            .build());
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt.minusYears(1), 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 0d);
	    SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
	    assertThat(sg.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(1000));

    }

    @Test
    void skalIkkeBeregneGrunnlagNårAlleredeFastsattAvSaksbehandler() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus status = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrStatus.builder(status).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(33333));
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getGjennomsnittligPGI()).isEqualByComparingTo(BigDecimal.valueOf(4.0d * GRUNNBELØP_2017));
	    assertThat(beregningsgrunnlag.getSammenligningsgrunnlagPrStatus()).isEmpty();
        assertThat(bgpsa.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(33333));
    }

    @Test
    void skalIkkeBeregneBruttoNårGrunnlagErBesteberegnet() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));

        Beregningsgrunnlag besteberegnetGrunnlag = Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(true)).build();
        BeregningsgrunnlagPeriode grunnlag = besteberegnetGrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus status = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrStatus.builder(status).medFastsattAvSaksbehandler(false).medBeregnetPrÅr(BigDecimal.valueOf(33333));
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getGjennomsnittligPGI()).isEqualByComparingTo(BigDecimal.valueOf(4.0d * GRUNNBELØP_2017));
	    assertThat(beregningsgrunnlag.getSammenligningsgrunnlagPrStatus()).isEmpty();
	    assertThat(bgpsa.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(33333));
    }

    @Test
    void skalGjøreAvviksberegningSelvOmFastsattAvSaksbehandlerOgIkkeBesteberegnet() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(GRUNNBELØP_2017 * 1.245 * 12)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));

        Beregningsgrunnlag besteberegnetGrunnlag = Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(false)).build();
        BeregningsgrunnlagPeriode grunnlag = besteberegnetGrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus status = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrStatus.builder(status).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(33333));
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getGjennomsnittligPGI()).isEqualByComparingTo(BigDecimal.valueOf(4.0d * GRUNNBELØP_2017));
	    assertThat(besteberegnetGrunnlag.getSammenligningsgrunnlagPrStatus()).isNotEmpty();
	    assertThat(bgpsa.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(33333));
    }

}
